package com.sohu.metaq.model;

import org.apache.zookeeper.data.Stat;

/**
 * Created by xiaofeiliu on 2015/4/7.
 */
public class ZkStatResult {

    private String offset;

    private Stat stat;

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public Stat getStat() {
        return stat;
    }

    public void setStat(Stat stat) {
        this.stat = stat;
    }
}
