package com.sohu.metaq.web.job;

import com.sohu.metaq.service.FunctionHolder;
import com.sohu.metaq.service.ResourcesHolder;
import com.taobao.metamorphosis.utils.MetaZookeeper;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xiaofeiliu on 2015/4/2.
 */
public class GroupCleanJob {

    private final static Logger logger = LoggerFactory.getLogger(GroupCleanJob.class);

    private void deleteTopic(ZkClient zkClient,MetaZookeeper metaZookeeper,String group, String topic) {

        try {
            zkClient.deleteRecursive(metaZookeeper.consumersPath + "/" + group + "/owners/" + topic);
            zkClient.deleteRecursive(metaZookeeper.consumersPath + "/" + group + "/offsets/" + topic);
            List<String> topics = zkClient.getChildren(metaZookeeper.consumersPath + "/" + group + "/owners");
            if(topics == null || topics.size() == 0) {
                zkClient.deleteRecursive(metaZookeeper.consumersPath + "/" + group);
            }
        } catch (Exception e) {
            logger.error("GroupCleanJob's deleteTopic is error.",e);
        }

    }

    public void inactiveGroupClean() {
        logger.error("GroupCleanJob is running.");

        for(String zkUrl : ResourcesHolder.zkClientCache.keySet()) {
            ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
            MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl);
            List<String> groups  = new ArrayList<String>();
            try {
                groups = zkClient.getChildren(metaZookeeper.consumersPath);
            } catch (Exception e) {
                logger.error("GroupCleanJob's inactiveGroupClean is error when fetch groups",e);
            }
            for(String group : groups) {
                List<String> topics = new ArrayList<String>();
                try {
                    topics = zkClient.getChildren(metaZookeeper.consumersPath + "/" + group + "/owners");
                } catch (Exception e) {
                    logger.error("GroupCleanJob's inactiveGroupClean is error when fetch topics",e);
                }

                for(String topic : topics) {
                    final HashMap<String, String> max_min_offset = new HashMap<String, String>();
                    final HashMap<String, String> avg_size = new HashMap<String, String>();
                    boolean isOutOfDate = false;
                    long overstockNum = 0l;
                    try {
                        isOutOfDate = FunctionHolder.getOffsetInfo(zkUrl, group, topic, max_min_offset, avg_size, false);

                        //计算消息的平均大小
                        long avgSize = 1l;
                        for (String key : avg_size.keySet()) {
                            String[] values = avg_size.get(key).split("#");
                            if (values.length == 2 && Integer.parseInt(values[0]) != 0) {
                                avgSize = Long.parseLong(values[1]) / Long.parseLong(values[0]);
                            }
                        }

                        for (String key : max_min_offset.keySet()) {
                            String[] values = max_min_offset.get(key).split("#");
                            long overstockSize = Long.parseLong(values[1]) - Long.parseLong(values[2]);
                            overstockNum = overstockNum + overstockSize/avgSize;
                        }

                    } catch (Exception e) {
                        logger.error("GroupCleanJob's inactiveGroupClean is error when getOffsetInfo",e);
                    }

                    if(isOutOfDate && overstockNum > 1000) {
                        deleteTopic(zkClient,metaZookeeper,group,topic);
                        System.out.println("group:" + group + " topic:" + topic);
                        logger.error("GroupCleanJob is deleting group:" + group + " topic:" + topic);
                    }

                }

            }
        }

    }

    public static void main(String[] args) {
        List<String> zookeeperUrls = new ArrayList<String>();
        String zkUrl1 = "10.10.53.93:2181,10.10.53.20:2181,10.10.53.21:2181";
        String zkUrl2 = "10.10.78.95:2181,10.10.78.61:2181,10.10.77.208:2181,10.10.52.114:2181,10.10.52.115:2181";
        String zkUrl3 = "10.22.10.122:2181,10.22.10.134:2181,10.22.10.133:2181";
        //zookeeperUrls.add("");
        zookeeperUrls.add(zkUrl3);
        //zookeeperUrls.add("");
        ResourcesHolder resourcesHolder = new ResourcesHolder();
        resourcesHolder.setZkUrls(zookeeperUrls);
        resourcesHolder.setZkTimeout(60000);
        resourcesHolder.init();
        GroupCleanJob groupCleanJob = new GroupCleanJob();

        //test inactiveGroupClean()
        groupCleanJob.inactiveGroupClean();

/*        //test deleteTopic()
        ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl2);
        MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl2);
        groupCleanJob.deleteTopic(zkClient,metaZookeeper,"metaq-rank-vrs-service-test","vrs-topic");*/

        System.out.println("haha");



    }



}
