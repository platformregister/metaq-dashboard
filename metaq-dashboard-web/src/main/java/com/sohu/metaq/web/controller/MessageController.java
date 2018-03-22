package com.sohu.metaq.web.controller;

import com.sohu.metaq.model.StatsResult;
import com.sohu.metaq.service.ConstantsHolder;
import com.sohu.metaq.service.GeckoHolder;
import com.sohu.metaq.service.ResourcesHolder;
import com.taobao.gecko.service.exception.NotifyRemotingException;
import com.taobao.metamorphosis.client.RemotingClientWrapper;
import com.taobao.metamorphosis.cluster.Broker;
import com.taobao.metamorphosis.cluster.Cluster;
import com.taobao.metamorphosis.network.BooleanCommand;
import com.taobao.metamorphosis.network.GetCommand;
import com.taobao.metamorphosis.utils.MetaZookeeper;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by xiaofeiliu on 2015/3/6.
 */
@Controller
public class MessageController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(OffsetController.class);


    @RequestMapping(value = "getMessage.do", method = RequestMethod.POST)
    public void getMessage(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Map result = new HashMap();
        boolean success = false;
        String zkUrl = request.getParameter("zkUrl");
        String group = request.getParameter("group");
        String topic = request.getParameter("topic");
        if( zkUrl != null && !zkUrl.equals("-1")
                && group != null && !group.equals("-1")
                && topic != null && !topic.equals("-1")) {

            List<String> partitions = ResourcesHolder.getPartitions(zkUrl,group,topic);

            Cluster cluster = ResourcesHolder.getCluster(zkUrl);
/*            ZkClient zkClient = new ZkClient(zkUrl, 5000);
            MetaZookeeper metaZookeeper = new MetaZookeeper(zkClient,"/meta");
            Cluster cluster = metaZookeeper.getCluster();*/

            if(partitions != null && partitions.size() > 0) {
                success = true;
                for(String partition : partitions) {
                    String[] b_p = partition.split("-");

                    RemotingClientWrapper remotingClientWrapper = GeckoHolder.remotingClientCache.get(cluster.getBrokerRandom(Integer.parseInt(b_p[0])).getZKString());

                    GetCommand getCommand = new GetCommand(topic, group, Integer.parseInt(b_p[1]), ConstantsHolder.maxOffset, ConstantsHolder.messageMaxSize, ConstantsHolder.messageOpaque);

                    try {
                        BooleanCommand resp = (BooleanCommand) remotingClientWrapper
                                .invokeToGroup(cluster.getBrokerRandom(Integer.parseInt(b_p[0])).getZKString(),
                                        getCommand, ConstantsHolder.messageTimeout, TimeUnit.MILLISECONDS);
                        result.put(partition + "Max", Long.parseLong(resp.getErrorMsg()));

                    } catch (Exception e) {
                        logger.error("OffsetController's getMaxOffset is error.",e);
                    }

                }
            }
        }


        result.put("success",success);
        write(response,result);

    }








}
