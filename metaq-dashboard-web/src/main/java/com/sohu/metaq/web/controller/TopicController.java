package com.sohu.metaq.web.controller;

import com.sohu.metaq.service.ResourcesHolder;
import com.taobao.metamorphosis.utils.MetaZookeeper;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiaofeiliu on 2015/3/9.
 */
@Controller
public class TopicController extends BaseController  {
    private final static Logger logger = LoggerFactory.getLogger(TopicController.class);

    private final static ConcurrentHashMap<String, List<String>> topicsCache = new ConcurrentHashMap<String, List<String>>();

    @RequestMapping(value = "getTopics.do", method = RequestMethod.POST)
    public void getTopics(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Map result = new HashMap();
        boolean success = false;
        String zkUrl = request.getParameter("zkUrl");
        if( zkUrl != null && zkUrl.length() > 0) {
            ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
            MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl);
            List<String> topics = zkClient.getChildren(metaZookeeper.brokerTopicsPath);
            success = true;
            result.put("topics",topics);
        }
        result.put("success",success);
        write(response,result);
    }


    @RequestMapping(value = "getTopicsByGroup.do", method = RequestMethod.POST)
    public void getTopicsByGroup(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Map result = new HashMap();
        boolean success = false;
        String zkUrl = request.getParameter("zkUrl");
        String group = request.getParameter("group");
        if( zkUrl != null && zkUrl.length() > 0) {
            ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
            MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl);
            try {
                List<String> topics = zkClient.getChildren(metaZookeeper.consumersPath + "/" + group + "/owners");
                success = true;
                result.put("topics", topics);
            } catch (ZkNoNodeException e) {
                logger.error("TopicController's getTopicsByGroup is error.",e);
            }
        }
        result.put("success",success);
        write(response,result);
    }

    @RequestMapping(value = "deleteTopic.do", method = RequestMethod.POST)
    public void deleteTopic(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Map result = new HashMap();
        boolean success = false;
        String zkUrl = request.getParameter("zkUrl");
        String group = request.getParameter("group");
        String topic = request.getParameter("topic");
        if( zkUrl != null && !zkUrl.equals("-1")
                && group != null && !group.equals("-1")) {
            ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
            MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl);
            result.put("groupDelete",false);
            if(topic != null && !topic.equals("-1")) {
                try {
                    zkClient.deleteRecursive(metaZookeeper.consumersPath + "/" + group + "/owners/" + topic);
                    zkClient.deleteRecursive(metaZookeeper.consumersPath + "/" + group + "/offsets/" + topic);
                    success = true;
                    List<String> topics = zkClient.getChildren(metaZookeeper.consumersPath + "/" + group + "/owners");
                    if(topics == null || topics.size() == 0) {
                        zkClient.deleteRecursive(metaZookeeper.consumersPath + "/" + group);
                        result.put("groupDelete",true);
                    }
                } catch (Exception e) {
                    logger.error("TopicController's deleteTopic is error1.",e);
                }

            } else {
                try {
                    zkClient.deleteRecursive(metaZookeeper.consumersPath + "/" + group);
                    success = true;
                    result.put("groupDelete",true);
                } catch (Exception e) {
                    logger.error("TopicController's deleteTopic is error2.",e);
                }
            }

        }
        result.put("success",success);
        write(response,result);
    }





}
