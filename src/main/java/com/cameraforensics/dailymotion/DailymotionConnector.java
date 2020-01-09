package com.cameraforensics.dailymotion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import com.squareup.okhttp.*;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

public class DailymotionConnector {

    private static Logger log = LoggerFactory.getLogger(DailymotionConnector.class);

    private static final String API_ENDPOINT = "https://api.dailymotion.com/videos";

    private ObjectMapper objectMapper = new ObjectMapper();

    private void setFields(final Map<String, String> parameters, final int page, final int limit) {
        parameters.put("fields", "channel.id,owner.id,url");
        parameters.put("page", String.valueOf(Math.max(1, page)));
        parameters.put("limit", String.valueOf(Math.max(0, Math.min(100, limit))));
    }

    public VideoContent downloadVideo(final DailymotionResponseItem video, boolean includeProbeData) throws IOException, YoutubeDLException {
        String directory = System.getProperty("java.io.tmpdir");
        String videoId = video.getUrl().substring(video.getUrl().lastIndexOf('/') + 1);
        log.info("Writing video {} to {}", video.getUrl(), directory);

        YoutubeDLRequest request = new YoutubeDLRequest(video.getUrl(), directory);
        request.setOption("output", "%(id)s.%(uploader_id)s.%(resolution)s.%(ext)s");
        request.setOption("format", "bestvideo+bestaudio/mp4");
        request.setOption("force-ipv4");

        YoutubeDLResponse response = YoutubeDL.execute(request);

        File file = findDownloadedVideoFile(videoId, directory);
        log.info("Video downloaded to: {}. Extracting frame data...", file.getAbsolutePath());
        if (file != null && file.length() > 0) {

            FFmpegProbeResult probeResult = null;
            if (includeProbeData) {
                FFprobe ffprobe = new FFprobe();
                probeResult = ffprobe.probe(file.getAbsolutePath());
            }

            String resolution = null;
            String user = null;
            String pattern = videoId + "\\.(.*?)\\.([0-9]{1,4}x[0-9]{1,4})\\.(mkv|mp4)";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(file.getName());
            if (m.find()) {
                user = m.group(1);
                resolution = m.group(2);
            }
            else {
                log.warn("Couldn't match user and resolution in filename: {}", file.getName());
            }

            return new VideoContent(file, user, resolution, probeResult);
        }
        else {
            log.error("Downloaded video file does not appear to have any length. YouTubeDL Error: {} Exit Code: {}", response.getErr(), response.getExitCode());
            throw new YoutubeDLException("Video could not be downloaded into " + file.getAbsolutePath());
        }
    }

    private File findDownloadedVideoFile(final String videoId, final String directory) {
        List<String> possibleExtensions = Arrays.asList(".mp4", ".mkv");

        File[] files = new File(directory).listFiles((dir, name) -> {
            int extensionMarker = name.lastIndexOf('.');
            if (extensionMarker != -1) {
                String suffix = name.substring(extensionMarker);
                return name.startsWith(videoId) && possibleExtensions.contains(suffix);
            }
            return false;
        });
        log.debug("Found {} files matching {} with possible extension: ", files.length, videoId);
        if (files.length > 0) {
            return files[0];
        }

        return null;
    }

    public DailymotionResponse getVideosByTag(final String tag, final int page, final int limit) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("tags", tag);
        setFields(parameters, page, limit);

        String json = doGet(API_ENDPOINT, parameters);
        if (json != null && json.length() > 0) {
            return objectMapper.readValue(json, DailymotionResponse.class);
        }

        return null;
    }

    public DailymotionResponse getVideosByOwnerId(final String ownerId, final int page, final int limit) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("owners", ownerId);
        setFields(parameters, page, limit);

        String json = doGet(API_ENDPOINT, parameters);
        if (json != null && json.length() > 0) {
            return objectMapper.readValue(json, DailymotionResponse.class);
        }

        return null;
    }

    public DailymotionResponse searchVideos(final String search, final int page, final int limit) throws IOException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("search", search);
        setFields(parameters, page, limit);

        String json = doGet(API_ENDPOINT, parameters);
        if (json != null && json.length() > 0) {
            return objectMapper.readValue(json, DailymotionResponse.class);
        }

        return null;
    }

    private String processResponse(Response response) throws IOException {
        log.debug("Response: successful={} code={}", response.isSuccessful(), response.code());
        String json = null;
        if (response.isSuccessful() && response.code() >= 200 && response.code() < 300) {
            String contentEncoding = response.header("Content-Encoding");
            InputStream is;
            try {
                if (contentEncoding != null && !contentEncoding.contains("gzip")) {
                    is = response.body().byteStream();
                } else {
                    is = new GZIPInputStream(response.body().byteStream());
                }
                json = IOUtils.toString(is, "UTF-8");
            } catch (ZipException e) {
                if (e.getMessage().contains("Not in GZIP format")) {
                    is = response.body().byteStream();
                    json = IOUtils.toString(is, "UTF-8");
                }
            }
        }

        return json;
    }

    private String doGet(final String endpoint, final Map<String, String> parameters) throws IOException {

        HttpUrl.Builder httpBuilder = HttpUrl.parse(endpoint).newBuilder();
        if (parameters != null) {
            for(Map.Entry<String, String> param : parameters.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(),param.getValue());
            }
        }

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Host", "api.dailymotion.com")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Connection", "keep-alive")
                .build();

        Response response = client.newCall(request).execute();
        return processResponse(response);
    }

}
