package com.sohu.metaq.web.controller;

import com.sohu.metaq.model.StatsResult;
import com.sohu.metaq.service.ConstantsHolder;
import com.sohu.metaq.service.GeckoHolder;
import com.sohu.metaq.service.ResourcesHolder;
import com.sohu.metaq.web.utils.Http3xUtil;
import com.sohu.metaq.web.utils.JsonUtils;
import com.taobao.metamorphosis.client.RemotingClientWrapper;
import com.taobao.metamorphosis.cluster.Broker;
import com.taobao.metamorphosis.cluster.Cluster;
import com.taobao.metamorphosis.utils.Utils;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiaofeiliu on 2015/3/18.
 */
@Controller
public class ClusterController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(ClusterController.class);

    @RequestMapping(value = "getCluster.do", method = RequestMethod.POST)
    public void getCluster(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Map result = new HashMap();
        boolean success = false;
        final String zkUrl = request.getParameter("zkUrl");

        HashMap<String, String> ids = new HashMap<String, String>();
        if( zkUrl != null && !zkUrl.equals("-1")) {
            Cluster cluster = ResourcesHolder.getCluster(zkUrl);
            ConcurrentHashMap<Integer, Set<Broker>> brokers =  cluster.getBrokers();
            for(Integer id : brokers.keySet()) {
                Set<Broker> master_slave = brokers.get(id);
                final StringBuilder sb  = new StringBuilder();
                int i = 0;
                for(Broker broker : master_slave) {
                    if(i == 1) {
                        sb.append("#");
                    }
                    sb.append(broker.getZKString()).append("(").append(broker.isSlave()?"Slave":"Master").append(")");
                    i++;

                    //获取单机dashboard端口
                    RemotingClientWrapper remotingClientWrapper = GeckoHolder.remotingClientCache.get(broker.getZKString());
                    try {
                        StatsResult statsResult = GeckoHolder.getStats(remotingClientWrapper, broker.getZKString(), ConstantsHolder.statsConfig, ConstantsHolder.messageTimeout);
                        if(statsResult.isSuccess()) {
                            try {
                                Utils.processEachLine(statsResult.getStatsInfo(), new Utils.Action() {
                                    @Override
                                    public void process(String line) {
                                        if (line.startsWith("dashboardHttpPort")) {
                                            sb.append("@").append(line.substring(line.indexOf("=") + 1));
                                        }
                                    }
                                });
                            }
                            catch (IOException e) {
                                logger.error("ClusterController's getCluster is error1.",e);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("ClusterController's getCluster is error2.",e);
                    }
                }
                ids.put(id.toString(),sb.toString());
            }
            success = true;
        }

        result.put("ids", JsonUtils.map2Json(ids));
        result.put("success",success);
        write(response,result);
    }

    @RequestMapping(value = "reloadConfig.do", method = RequestMethod.POST)
    public void reloadConfig(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        String reloadUrl = request.getParameter("reloadUrl");
        String result = Http3xUtil.postMethod(reloadUrl);
        write(response, result);
    }

    @RequestMapping(value = "reloadConfigAll.do", method = RequestMethod.POST)
    public void reloadConfigAll(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        final String zkUrl = request.getParameter("zkUrl");
        final StringBuilder sb = new StringBuilder();
        if( zkUrl != null && !zkUrl.equals("-1")) {
            Cluster cluster = ResourcesHolder.getCluster(zkUrl);
            ConcurrentHashMap<Integer, Set<Broker>> brokers = cluster.getBrokers();
            for (Integer id : brokers.keySet()) {
                Set<Broker> master_slave = brokers.get(id);
                for (Broker broker : master_slave) {
                    final StringBuilder reloadUrl = new StringBuilder("http://");
                    reloadUrl.append(broker.getHost());
                    sb.append(broker.getHost()).append("操作返回:");
                    //获取单机dashboard端口
                    RemotingClientWrapper remotingClientWrapper = GeckoHolder.remotingClientCache.get(broker.getZKString());
                    try {
                        StatsResult statsResult = GeckoHolder.getStats(remotingClientWrapper, broker.getZKString(), ConstantsHolder.statsConfig, ConstantsHolder.messageTimeout);
                        if(statsResult.isSuccess()) {
                            try {
                                Utils.processEachLine(statsResult.getStatsInfo(), new Utils.Action() {
                                    @Override
                                    public void process(String line) {
                                        if (line.startsWith("dashboardHttpPort")) {
                                            reloadUrl.append(":").append(line.substring(line.indexOf("=") + 1));
                                        }
                                    }
                                });
                            }
                            catch (IOException e) {
                                logger.error("ClusterController's getCluster is error1.",e);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("ClusterController's getCluster is error2.",e);
                    }

                    sb.append(Http3xUtil.postMethod(reloadUrl.append("/reload-config").toString())).append("\n");

                }
            }

        }
        write(response, sb.toString());

    }

    @RequestMapping(value = "getConfig.do", method = RequestMethod.POST)
    public void getConfig(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Map result = new HashMap();
        boolean success = false;
        String serverUrl = request.getParameter("serverUrl");
        RemotingClientWrapper remotingClientWrapper = GeckoHolder.remotingClientCache.get(serverUrl);
        try {
            StatsResult statsResult = GeckoHolder.getStats(remotingClientWrapper, serverUrl, ConstantsHolder.statsConfig, ConstantsHolder.messageTimeout);
            if(statsResult.isSuccess()) {
                success = true;
                result.put("config", statsResult.getStatsInfo());
            }
        } catch (Exception e) {
            logger.error("ClusterController's getCluster is error2.",e);
        }
        result.put("success",success);
        write(response,result);
    }

    @RequestMapping(value = "getConfigByDashboardPort.do", method = RequestMethod.POST)
    public void getConfigByDashboardPort(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {

        String serverUrl = request.getParameter("serverUrl");
        String result = Http3xUtil.getMethod(serverUrl);
        result = result.substring(result.indexOf("[system]"),result.lastIndexOf("</textarea>"));
        write(response, result);

    }

    @RequestMapping(value = "getStat.do", method = RequestMethod.POST)
    public void getStat(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Map result = new HashMap();
        boolean success = false;
        String serverUrl = request.getParameter("serverUrl");
        RemotingClientWrapper remotingClientWrapper = GeckoHolder.remotingClientCache.get(serverUrl);
        try {
            StatsResult statsResult = GeckoHolder.getStats(remotingClientWrapper, serverUrl, "", ConstantsHolder.messageTimeout);
            if(statsResult.isSuccess()) {
                success = true;
                try {
                    Utils.processEachLine(statsResult.getStatsInfo(), new Utils.Action() {
                        @Override
                        public void process(String line) {
                            String[] tmp = StringUtils.splitByWholeSeparator(line, " ");
                            if (tmp != null && tmp.length == 2) {
                                if ("".equals(tmp[0])) {
                                    //do something
                                }
                            }
                        }
                    });
                }
                catch (IOException e) {
                    logger.error("ClusterController's getCluster is error1.",e);
                }
            }
        } catch (Exception e) {
            logger.error("ClusterController's getCluster is error2.",e);
        }
        result.put("success",success);
        write(response,result);
    }

    @RequestMapping(value = "getSystem.do", method = RequestMethod.POST)
    public void getSystem(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {

        String systemUrl = request.getParameter("systemUrl");
        String result = Http3xUtil.getMethod(systemUrl);
        result = result.substring(0,result.lastIndexOf("<div class=\"row\">"));
        write(response, result);

    }

    @RequestMapping(value = "getJvm.do", method = RequestMethod.POST)
    public void getJvm(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {

        String jvmUrl = request.getParameter("jvmUrl");
        String result = Http3xUtil.getMethod(jvmUrl);
        write(response, result);

    }

    @RequestMapping(value = "getThread.do", method = RequestMethod.POST)
    public void getThread(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {

        String threadUrl = request.getParameter("threadUrl");
        String result = Http3xUtil.getMethod(threadUrl);
        write(response, result);


    }

}
