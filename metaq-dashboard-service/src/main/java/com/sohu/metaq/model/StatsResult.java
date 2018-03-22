package com.sohu.metaq.model;

/**
 * Created by xiaofeiliu on 2015/3/13.
 */
public class StatsResult {

    private boolean success;

    private String statsInfo;

    private final String serverUrl;

    private Exception e;

    public StatsResult(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStatsInfo() {
        return statsInfo;
    }

    public void setStatsInfo(String statsInfo) {
        this.statsInfo = statsInfo;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public Exception getException() {
        return e;
    }

    public void setException(Exception e) {
        this.e = e;
    }
}
