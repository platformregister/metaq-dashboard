package com.sohu.metaq.web.controller;

import com.sohu.metaq.model.StatsResult;
import com.sohu.metaq.model.ZkStatResult;
import com.sohu.metaq.service.ConstantsHolder;
import com.sohu.metaq.service.FunctionHolder;
import com.sohu.metaq.service.GeckoHolder;
import com.sohu.metaq.service.ResourcesHolder;
import com.sohu.metaq.web.utils.Http3xUtil;
import com.sohu.metaq.web.utils.JsonUtils;
import com.taobao.metamorphosis.client.RemotingClientWrapper;
import com.taobao.metamorphosis.cluster.Broker;
import com.taobao.metamorphosis.cluster.Cluster;
import com.taobao.metamorphosis.exception.MetaClientException;
import com.taobao.metamorphosis.exception.MetaOpeartionTimeoutException;
import com.taobao.metamorphosis.network.BooleanCommand;
import com.taobao.metamorphosis.network.GetCommand;
import com.taobao.metamorphosis.network.HttpStatus;
import com.taobao.metamorphosis.network.StatsCommand;
import com.taobao.metamorphosis.utils.MetaZookeeper;
import com.taobao.metamorphosis.utils.Utils;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by xiaofeiliu on 2015/3/6.
 */
@Controller
public class OffsetController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(OffsetController.class);



    @RequestMapping(value = "getOffset.do", method = RequestMethod.POST)
    public void getOffset(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Map result = new HashMap();
        boolean success = false;
        final String zkUrl = request.getParameter("zkUrl");
        final String group = request.getParameter("group");
        final String topic = request.getParameter("topic");
        if( zkUrl != null && !zkUrl.equals("-1")
                && group != null && !group.equals("-1")
                && topic != null && !topic.equals("-1")) {

            final HashMap<String, String> max_min_offset = new HashMap<String, String>();
            final HashMap<String, String> avg_size = new HashMap<String, String>();
            final HashMap<String, Long> current_offset = new HashMap<String, Long>();

            boolean isOutOfDate = FunctionHolder.getOffsetInfo(zkUrl,group,topic,max_min_offset,avg_size,true);

            if(max_min_offset.size() > 0 && avg_size.size() > 0) {
                success = true;
                result.put("mmOffset", JsonUtils.map2Json(max_min_offset));
                result.put("avg", avg_size);
                result.put("isOutOfDate", isOutOfDate);
            }

        }

        result.put("success",success);
        write(response,result);

    }


    @RequestMapping(value = "skip.do", method = RequestMethod.POST)
    public void skip(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        String result = "网络异常";
        String zkUrl = request.getParameter("zkUrl");
        String serverUrl = request.getParameter("serverUrl");
        final String group = request.getParameter("group");
        final String topic = request.getParameter("topic");
        final String partition = request.getParameter("partition");

        final StringBuilder skipUrl = new StringBuilder();
        skipUrl.append(serverUrl.replaceFirst("meta","http").substring(0, serverUrl.lastIndexOf(":") + 1));

        //获取单机dashboard端口
        RemotingClientWrapper remotingClientWrapper = GeckoHolder.remotingClientCache.get(serverUrl);
        try {
            StatsResult statsResult = GeckoHolder.getStats(remotingClientWrapper, serverUrl, ConstantsHolder.statsConfig, ConstantsHolder.messageTimeout);
            if(statsResult.isSuccess()) {
                try {
                    Utils.processEachLine(statsResult.getStatsInfo(), new Utils.Action() {
                        @Override
                        public void process(String line) {
                            if (line.startsWith("dashboardHttpPort")) {
                                skipUrl.append(line.substring(line.indexOf("=") + 1));
                            }
                        }
                    });

                    skipUrl.append("/topics/").append(topic).append("/groups/").append(group).append("/partitions/").append(partition).append("/skip");
                    result = Http3xUtil.postMethod(skipUrl.toString());
                }
                catch (IOException e) {
                    logger.error("ClusterController's getCluster is error1.",e);
                }
            }
        } catch (Exception e) {
            logger.error("ClusterController's getCluster is error2.",e);
        }

        write(response,result);
    }


}
