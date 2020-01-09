package com.cameraforensics.dailymotion;

import com.sapher.youtubedl.YoutubeDLException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DailymotionConnectorTest {

    @Test
    public void search_by_tag() throws IOException {
        // given
        DailymotionConnector connector = new DailymotionConnector();

        // when
        DailymotionResponse response = connector.getVideosByTag("talk to me", 1, 10);

        // then
        assertNotNull(response);
        assertEquals(10, response.getList().size());
        assertEquals(10, response.getLimit());
        assertEquals(1, response.getPage());
        for (DailymotionResponseItem item : response.getList()) {
            assertNotNull(item);
        }
    }

    @Test
    public void search_by_owner_id() throws IOException {
        // given
        DailymotionConnector connector = new DailymotionConnector();
        final String ownerId = "x2bbrld";

        // when
        DailymotionResponse response = connector.getVideosByOwnerId(ownerId, 1, 10);

        // then
        assertNotNull(response);
        assertEquals(10, response.getList().size());
        assertEquals(10, response.getLimit());
        assertEquals(1, response.getPage());
        for (DailymotionResponseItem item : response.getList()) {
            assertNotNull(item);
            assertEquals(ownerId, item.getOwnerId());
        }
    }

    @Test
    @Ignore("Used for testing")
    public void can_download_video() throws IOException, YoutubeDLException {
        // given
        DailymotionConnector connector = new DailymotionConnector();
        final String ownerId = "x2bbrld";
        DailymotionResponse response = connector.getVideosByOwnerId(ownerId, 1, 1);
        DailymotionResponseItem video = response.getList().get(0);

        // when
        VideoContent videoContent = connector.downloadVideo(video, false);

        // then
        assertNotNull(videoContent);
        assertNotNull(videoContent.getTemporaryVideoFile());
        assertEquals(video.getOwnerId(), videoContent.getUser());
        assertEquals("mp4", videoContent.getType());
        assertEquals("1280x720", videoContent.getResolution());
        System.out.println(videoContent.getTemporaryVideoFile());
    }

}
