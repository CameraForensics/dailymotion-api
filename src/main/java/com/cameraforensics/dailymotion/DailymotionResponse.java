package com.cameraforensics.dailymotion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class DailymotionResponse {

    private int page;

    private int limit;

    private boolean explicit;

    private int total;

    @JsonProperty("has_more")
    private boolean hasMore;

    private List<DailymotionResponseItem> list = new ArrayList<>();

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isExplicit() {
        return explicit;
    }

    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public List<DailymotionResponseItem> getList() {
        return list;
    }

    public void setList(List<DailymotionResponseItem> list) {
        this.list = list;
    }
}
