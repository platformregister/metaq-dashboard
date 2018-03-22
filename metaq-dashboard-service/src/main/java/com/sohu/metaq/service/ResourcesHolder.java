package com.sohu.metaq.service;

import com.sohu.metaq.model.ZkStatResult;
import com.taobao.metamorphosis.cluster.Cluster;
import com.taobao.metamorphosis.utils.MetaZookeeper;
import com.taobao.metamorphosis.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaofeiliu on 2015/3/6.
 */
public class ResourcesHolder {

    private final static Logger logger = LoggerFactory.getLogger(ResourcesHolder.class);

    //ZkClient
    public static final HashMap<String, ZkClient> zkClientCache = new HashMap<String, ZkClient>();

    //MetaZookeeper
    public static final HashMap<String, MetaZookeeper> metaZookeeperCache = new HashMap<String, MetaZookeeper>();

    private List<String> zkUrls;

    public List<String> getZkUrls() {
        return zkUrls;
    }

    public void setZkUrls(List<String> zkUrls) {
        this.zkUrls = zkUrls;
    }

    private int zkTimeout;

    public int getZkTimeout() {
        return zkTimeout;
    }

    public void setZkTimeout(int zkTimeout) {
        this.zkTimeout = zkTimeout;
    }

    public void init() {
        ZkUtils.ZKConfig zkConfig = new ZkUtils.ZKConfig();
        for(String zkUrl : zkUrls) {
            ZkClient zkClient = null;
            try {
                zkClient = new ZkClient(zkUrl, zkTimeout, zkTimeout, new ZkUtils.StringSerializer());
            } catch (Exception e) {
                logger.error("ZkClientHolder's init is error.",e);
            }
            zkClientCache.put(zkUrl,zkClient);

            if(zkClient != null) {
                MetaZookeeper metaZookeeper = new MetaZookeeper(zkClient, zkConfig.getZkRoot());
                metaZookeeperCache.put(zkUrl, metaZookeeper);
            }
        }
        GeckoHolder.init(zkUrls);
    }

    public static ZkClient getZkClient(String zkUrl) {
        return zkClientCache.get(zkUrl);
    }



    /*-------------------Helper-----------------------------------------------------------------*/

    public static Cluster getCluster(String zkUrl) {
        Cluster cluster = null;
        MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl);
        if(metaZookeeper != null) {
            cluster = metaZookeeper.getCluster();
        }
        return cluster;
    }

    //根据group名称、topic名称获取broker端对应的partition列表
    public static List<String> getPartitions(String zkUrl, String group, String topic) {
        ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
        MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl);
        List<String> partitions = null;
        try {
            partitions = zkClient.getChildren(metaZookeeper.consumersPath + "/" + group + "/offsets/" + topic);
        } catch (ZkNoNodeException e) {
            logger.error("ResourcesHolder's getPartitions is error.", e);
        }
        return partitions;
    }

    //根据group名称、topic名称、partition列表获取当前的offset
    public static String getCurrentOffset(String zkUrl, String group, String topic, String partition) {
        ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
        MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl);
        String rt = zkClient.readData(metaZookeeper.consumersPath + "/" + group + "/offsets/" + topic + "/" + partition, true);
        if (rt.indexOf("-") > 0) {
            String[] tmps = rt.split("-");
            if (tmps.length >= 2)
                return tmps[1];
            else
                return tmps[0];
        }
        else
            return rt;
    }

    //根据group名称、topic名称、partition列表获取当前的offset
    public static ZkStatResult getCurrentOffsetAndStat(String zkUrl, String group, String topic, String partition) {
        ZkStatResult zkStatResult = new ZkStatResult();

        ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
        MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl);

        Stat stat = new Stat();
        String rt = zkClient.readData(metaZookeeper.consumersPath + "/" + group + "/offsets/" + topic + "/" + partition, stat);

        String offset;
        if (rt.indexOf("-") > 0) {
            String[] tmps = rt.split("-");
            if (tmps.length >= 2)
                offset = tmps[1];
            else
                offset = tmps[0];
        }
        else
            offset = rt;

        zkStatResult.setOffset(offset);
        zkStatResult.setStat(stat);

        return  zkStatResult;
    }

    //根据group名称、topic名称、partition列表获取当前的owner
    public static String getCurrentOwner(String zkUrl, String group, String topic, String partition) {
        ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
        MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl);
        String rt = zkClient.readData(metaZookeeper.consumersPath + "/" + group + "/owners/" + topic + "/" + partition, true);
        return rt;
    }



}
