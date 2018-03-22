package com.sohu.metaq.web.controller;

import com.sohu.metaq.service.ResourcesHolder;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiaofeiliu on 2015/3/10.
 */
@Controller
public class GroupController extends BaseController   {

    private final static Logger logger = LoggerFactory.getLogger(GroupController.class);

    @RequestMapping(value = "getGroups.do", method = RequestMethod.POST)
    public void getGroups(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {

        Map result = new HashMap();
        boolean success = false;
        String zkUrl = request.getParameter("zkUrl");
        if( zkUrl != null && zkUrl.length() > 0) {
            ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
            MetaZookeeper metaZookeeper = ResourcesHolder.metaZookeeperCache.get(zkUrl);
            List<String> groups = zkClient.getChildren(metaZookeeper.consumersPath);

            Collections.sort(groups);

            success = true;
            result.put("groups",groups);
        }
        result.put("success",success);
        write(response,result);

    }



}
