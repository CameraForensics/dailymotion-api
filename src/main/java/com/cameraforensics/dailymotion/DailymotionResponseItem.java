package com.cameraforensics.dailymotion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DailymotionResponseItem {

    @JsonProperty("channel.id")
    private String channelId;

    @JsonProperty("owner.id")
    private String ownerId;

    private String url;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
