package com.sohu.metaq.monitor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by xiaofeiliu on 2015/6/12.
 */
public class MonitorConfig {

    private final static Logger logger = LoggerFactory.getLogger(MonitorConfig.class);

    private String[] mobileList;

    private String[] mailList;

    private String serverUrl;

    private String groupName;

    private String topicName;

    public static long defaultMaxOverstockSize = 500l;

    private long maxOverstockSize = 0l;

    public static long defaultProbeInterval = 900000l;

    private long probeInterval = 0l;


    public String[] getMobileList() {
        return mobileList;
    }

    public void setMobileList(String[] mobileList) {
        this.mobileList = mobileList;
    }

    public String[] getMailList() {
        return mailList;
    }

    public void setMailList(String[] mailList) {
        this.mailList = mailList;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public long getMaxOverstockSize() {
        return maxOverstockSize;
    }

    public void setMaxOverstockSize(long maxOverstockSize) {
        this.maxOverstockSize = maxOverstockSize;
    }

    public long getProbeInterval() {
        return probeInterval;
    }

    public void setProbeInterval(long probeInterval) {
        this.probeInterval = probeInterval;
    }
}
