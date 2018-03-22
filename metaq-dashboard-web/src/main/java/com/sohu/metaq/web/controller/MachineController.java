package com.sohu.metaq.web.controller;

import com.sohu.metaq.service.ResourcesHolder;
import com.taobao.metamorphosis.utils.MetaZookeeper;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by liuxiaofei on 2017/7/31.
 */
@Controller
public class MachineController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(MachineController.class);


    @RequestMapping(value = "getMachinesByTopic.do", method = RequestMethod.POST)
    public void getMachinesByTopic(HttpServletRequest request, HttpServletResponse response, ModelAndView model) {

        Map result = new HashMap();
        boolean success = false;

        String targetTopic = request.getParameter("targetTopic");
        //String operator = request.getParameter("operator");
        if(StringUtils.isNotEmpty(targetTopic) /*&& StringUtils.isNotEmpty(operator)*/) {
            logger.error("searching " + targetTopic);

            String zkUrl = request.getParameter("zkUrl");
            if (zkUrl != null && zkUrl.length() > 0) {
                ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
                MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl);
                List<String> groups = zkClient.getChildren(metaZookeeper.consumersPath);
                Collections.sort(groups);


                List<String> machines = new ArrayList<String>();
                for (String group : groups) {
                    try {
                        List<String> topics = zkClient.getChildren(metaZookeeper.consumersPath + "/" + group + "/owners");
                        if (topics != null && topics.size() > 0) {
                            for(String topic : topics) {
                                if(targetTopic.equals(topic)) {
                                    List<String> consumerMachines = zkClient.getChildren(metaZookeeper.consumersPath + "/" + group + "/ids");
                                    if(consumerMachines != null && consumerMachines.size() > 0) {
                                        machines.addAll(consumerMachines);
                                    }
                                }

                            }
                        }
                    } catch (ZkNoNodeException e) {
                        logger.error("MachineController's getMachinesByTopic is error.", e);
                    }
                }

                success = true;
                result.put("machines", machines);
            }
        }
        result.put("success",success);
        write(response,result);

    }



}
