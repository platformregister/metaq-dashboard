package com.sohu.metaq.service;

import com.taobao.gecko.core.util.OpaqueGenerator;
import com.taobao.gecko.service.RemotingClient;
import com.taobao.metamorphosis.client.*;
import com.taobao.metamorphosis.client.producer.MessageProducer;
import com.taobao.metamorphosis.cluster.Broker;
import com.taobao.metamorphosis.cluster.Cluster;
import com.taobao.metamorphosis.exception.MetaClientException;
import com.taobao.metamorphosis.exception.MetaOpeartionTimeoutException;
import com.taobao.metamorphosis.network.BooleanCommand;
import com.taobao.metamorphosis.network.HttpStatus;
import com.taobao.metamorphosis.network.StatsCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sohu.metaq.model.StatsResult;

/**
 * Created by xiaofeiliu on 2015/3/12.
 */
public class GeckoHolder {

    private final static Logger logger = LoggerFactory.getLogger(GeckoHolder.class);

    public static final HashMap<String,MetaMessageSessionFactory> messageSessionFactoryCache = new HashMap<String,MetaMessageSessionFactory>();

    public static final HashMap<String, RemotingClientWrapper> remotingClientCache = new HashMap<String, RemotingClientWrapper>();

    public static final HashMap<String , MessageProducer> messageProducerCache = new HashMap<String, MessageProducer>();

    public static void init(List<String> zkUrls) {

/*        for(String zkUrl : zkUrls) {
            try {
                MetaClientConfig metaClientConfig = new MetaClientConfig();
                ZkUtils.ZKConfig zkConfig = new ZkUtils.ZKConfig();
                zkConfig.zkConnect = zkUrl;
                metaClientConfig.setZkConfig(zkConfig);
                MetaMessageSessionFactory sessionFactory  = new MetaMessageSessionFactory(metaClientConfig);
                messageSessionFactoryCache.put(zkUrl, sessionFactory);
                remotingClientCache.put(zkUrl, sessionFactory.getRemotingClient());
                messageProducerCache.put(zkUrl, sessionFactory.createProducer());
            } catch (MetaClientException e) {
                logger.error("GeckoHolder is error.", e);
            }
        }*/

        for(String zkUrl : zkUrls) {
            Cluster cluster = ResourcesHolder.getCluster(zkUrl);
            ConcurrentHashMap<Integer,Set<Broker>> brokers = cluster.getBrokers();
            for(Integer brokerId : brokers.keySet()) {
                Set<Broker> masterAndSlave = brokers.get(brokerId);
                for(Broker broker : masterAndSlave) {
                    String serverUrl = broker.getZKString();
                    try {
                        MetaClientConfig metaClientConfig = new MetaClientConfig();
                        metaClientConfig.setServerUrl(serverUrl);
                        MetaMessageSessionFactory sessionFactory = new MetaMessageSessionFactory(metaClientConfig);
                        messageSessionFactoryCache.put(serverUrl, sessionFactory);
                        remotingClientCache.put(serverUrl, sessionFactory.getRemotingClient());

                    } catch (MetaClientException e) {
                        logger.error("GeckoHolder is error.", e);
                    }
                }
            }

        }

    }



    /*-------------------Helper-----------------------------------------------------------------*/

    public static StatsResult getStats(RemotingClientWrapper remotingClient, String serverUrl, String item, long timeout) {
        StatsResult statsResult = new StatsResult(serverUrl);
        try {
            BooleanCommand resp = (BooleanCommand) remotingClient.invokeToGroup(serverUrl,
                    new StatsCommand(OpaqueGenerator.getNextOpaque(), item),
                    timeout,
                    TimeUnit.MILLISECONDS);

            final String resultStr = resp.getErrorMsg();

            switch (resp.getCode()) {
                case HttpStatus.Success: {
                    statsResult.setSuccess(true);
                    statsResult.setStatsInfo(resultStr);
                    break;
                }
                default:
                    statsResult.setSuccess(false);
            }
        }
        catch (TimeoutException e) {
            statsResult.setException(new MetaOpeartionTimeoutException("Send message timeout in " + timeout + " mills"));
        }
        catch (InterruptedException e) {
            // ignore
            statsResult.setException(e);
        }
        catch (Exception e) {
            statsResult.setException(new MetaClientException("send stats failed", e));
        }
        return statsResult;
    }







}
