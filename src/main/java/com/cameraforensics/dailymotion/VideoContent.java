package com.cameraforensics.dailymotion;

import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class VideoContent {

    private File temporaryVideoFile;

    private String user;

    private String resolution;

    private Integer width;

    private Integer height;

    private String type;

    private FFmpegProbeResult probeMetadata;

    public VideoContent(final File temporaryVideoFile, final String user, final String resolution, final FFmpegProbeResult probeMetadata) {
        this.temporaryVideoFile = temporaryVideoFile;
        this.probeMetadata = probeMetadata;
        this.user = user;
        this.resolution = resolution;

        if (resolution != null) {
            String[] parts = resolution.split("x");
            if (parts.length == 2) {
                width = Integer.parseInt(parts[0]);
                height = Integer.parseInt(parts[1]);
            }
        }

        type = temporaryVideoFile.getName().substring(temporaryVideoFile.getName().lastIndexOf('.') + 1);

    }

    public File getTemporaryVideoFile() {
        return temporaryVideoFile;
    }

    public byte[] getContent() throws IOException {
        return FileUtils.readFileToByteArray(temporaryVideoFile);
    }

    public String getUser() {
        return user;
    }

    public String getResolution() {
        return resolution;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public String getType() {
        return type;
    }

    public FFmpegProbeResult getProbeMetadata() {
        return probeMetadata;
    }
}
