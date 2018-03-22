package com.sohu.metaq.service;

import com.sohu.metaq.model.StatsResult;
import com.sohu.metaq.model.ZkStatResult;
import com.taobao.metamorphosis.client.RemotingClientWrapper;
import com.taobao.metamorphosis.cluster.Broker;
import com.taobao.metamorphosis.cluster.Cluster;
import com.taobao.metamorphosis.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xiaofeiliu on 2015/7/29.
 */
public class FunctionHolder {

    private final static Logger logger = LoggerFactory.getLogger(FunctionHolder.class);

    public static long parseOffsetAsLong(String offsetString) {
        if (StringUtils.isBlank(offsetString)) {
            return -1;
        }

        String[] tmp = StringUtils.splitByWholeSeparator(offsetString, "-");

        try {

            if (tmp != null && tmp.length == 1) {
                return Long.parseLong(offsetString);
            }
            else if ((tmp != null && tmp.length == 2)) {
                return Long.parseLong(tmp[1]);
            }
            else {
                return -1;
            }
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    //返回值为isOutOfDate
    public static boolean getOffsetInfo(final String zkUrl, final String group, final String topic, final HashMap<String, String> max_min_offset, final HashMap<String, String> avg_size, boolean appendOwner) {

        boolean isOutOfDate = true;

        List<String> partitions = ResourcesHolder.getPartitions(zkUrl,group,topic);

        Cluster cluster = ResourcesHolder.getCluster(zkUrl);

        HashMap<String,Integer> uniqueIds = new HashMap<String, Integer>();


        if(partitions != null && partitions.size() > 0) {
            for(String partition : partitions) {
                String[] b_p = partition.split("-");

                //brokerId去重
                if(uniqueIds.containsKey(b_p[0])) {
                    continue;
                } else {
                    uniqueIds.put(b_p[0],1);
                }

                //broker连接串
                Broker tempBroker = cluster.getBrokerRandom(Integer.parseInt(b_p[0]));
                if(tempBroker == null) {
                    continue;
                }
                final String serverUrl = tempBroker.getZKString();
                RemotingClientWrapper remotingClientWrapper = GeckoHolder.remotingClientCache.get(serverUrl);

                try {

                    StatsResult statsResult = GeckoHolder.getStats(remotingClientWrapper, serverUrl, ConstantsHolder.statsOffsets, ConstantsHolder.messageTimeout);
                    if(statsResult.isSuccess()) {
                        try {
                            Utils.processEachLine(statsResult.getStatsInfo(), new Utils.Action() {
                                @Override
                                public void process(String line) {
                                    String[] tmp = StringUtils.splitByWholeSeparator(line, " ");
                                    if (tmp != null && tmp.length == 7) {
                                        if (topic.equals(tmp[0])) {
                                            max_min_offset.put(serverUrl + "#" + tmp[2], tmp[4] + "#" + tmp[6]);
                                            return;
                                        }
                                    }
                                }
                            });
                        }
                        catch (IOException e) {
                            logger.error("OffsetController's getAllOffset is error.",e);
                        }
                    }

                    StatsResult statsResult4AvgSize = GeckoHolder.getStats(remotingClientWrapper, serverUrl, ConstantsHolder.statsTopics, ConstantsHolder.messageTimeout);
                    if(statsResult4AvgSize.isSuccess()) {
                        try {
                            Utils.processEachLine(statsResult4AvgSize.getStatsInfo(), new Utils.Action() {
                                @Override
                                public void process(String line) {
                                    String[] tmp = StringUtils.splitByWholeSeparator(line, " ");
                                    if (tmp != null && tmp.length == 11) {
                                        if (topic.equals(tmp[0])) {
                                            avg_size.put(topic,tmp[4]+"#"+tmp[6]);
                                            return;
                                        }
                                    }
                                }
                            });
                        }
                        catch (IOException e) {
                            logger.error("OffsetController's getAvgSize is error.",e);
                        }
                    }

                } catch (Exception e) {
                    logger.error("OffsetController's getOffset is error.",e);
                }

            }

            for(String partition : partitions) {
                //String rt = ResourcesHolder.getCurrentOffset(zkUrl,group,topic,partition);
                ZkStatResult zkStatResult = ResourcesHolder.getCurrentOffsetAndStat(zkUrl,group,topic,partition);
                long currentOffset = parseOffsetAsLong(zkStatResult.getOffset());
                String[] b_p = partition.split("-");
                if(cluster.getBrokerRandom(Integer.parseInt(b_p[0])) == null) {
                    continue;
                }

                String mm;
                if(appendOwner) {
                    String owner = ResourcesHolder.getCurrentOwner(zkUrl,group,topic,partition);
                    mm = max_min_offset.get(cluster.getBrokerRandom(Integer.parseInt(b_p[0])).getZKString() + "#" + b_p[1]) + "#" +currentOffset + "#" + owner;
                } else {
                    mm = max_min_offset.get(cluster.getBrokerRandom(Integer.parseInt(b_p[0])).getZKString() + "#" + b_p[1]) + "#" +currentOffset;
                }

                max_min_offset.put(cluster.getBrokerRandom(Integer.parseInt(b_p[0])).getZKString() + "#" + b_p[1],mm);

                if(new Date().getTime() - zkStatResult.getStat().getMtime() < ConstantsHolder.outOfDate ) {
                    isOutOfDate = false;  //只要有一个分片上的offset在最近30天内更新过，则认为该group为活跃状态
                }



            }

        }

        return isOutOfDate;
    }






}
