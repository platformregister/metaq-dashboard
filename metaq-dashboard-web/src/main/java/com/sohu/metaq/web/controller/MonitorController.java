package com.sohu.metaq.web.controller;

import com.sohu.metaq.service.ConstantsHolder;
import com.sohu.metaq.service.MonitorHolder;
import com.sohu.metaq.service.ResourcesHolder;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by xiaofeiliu on 2015/7/23.
 */
@Controller
public class MonitorController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(MonitorController.class);

    @RequestMapping(value = "getSupervisors.do", method = RequestMethod.POST)
    public void getSupervisors(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {

        Map result = new HashMap();
        boolean success = false;

        String zkUrl = request.getParameter("zkUrl");
        String group = request.getParameter("group");
        String topic = request.getParameter("topic");

        if( zkUrl != null && !zkUrl.equals("-1")
                && group != null && !group.equals("-1")
                && topic != null && !topic.equals("-1")) {

            ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
            String path = ConstantsHolder.monitorRootPath + "/" +  group + "/" + topic;
            boolean rst = zkClient.exists(path);
            if(rst == false) {
                zkClient.createPersistent(path, true);
            }

            String data = zkClient.readData(path);

            if(data != null && data.length() > 0) {
                String supervisors = MonitorHolder.getSupervisors(data);
                if(supervisors != null) {
                    success = true;
                    result.put("supervisors", supervisors);
                }
            }

        }

        result.put("success",success);
        write(response,result);
    }

    @RequestMapping(value = "addSupervisor.do", method = RequestMethod.POST)
    public void addSupervisor(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Map result = new HashMap();
        boolean success = false;

        String zkUrl = request.getParameter("zkUrl");
        String group = request.getParameter("group");
        String topic = request.getParameter("topic");

        String username = request.getParameter("userName");
        String password = request.getParameter("password");

        boolean isValidate = verify(username, password);
        //isValidate = true;
        if( isValidate==true
                && zkUrl != null && !zkUrl.equals("-1")
                && group != null && !group.equals("-1")
                && topic != null && !topic.equals("-1")) {

            ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
            String path = ConstantsHolder.monitorRootPath + "/" +  group + "/" + topic;
            boolean rst = zkClient.exists(path);
            if(rst == false) {
                zkClient.createPersistent(path, true);
            }

            String data = zkClient.readData(path);

            if(data != null && data.length() > 0) {
                data = MonitorHolder.modifyZkData(data,username,null,null,null,MonitorHolder.INCREASE);
            } else {
                data = MonitorHolder.initZkData(username,null,null,null);
            }

            zkClient.writeData(path,data);
            success = true;

        }

        result.put("success",success);
        write(response,result);
    }

    @RequestMapping(value = "deleteSupervisor.do", method = RequestMethod.POST)
    public void deleteSupervisor(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Map result = new HashMap();
        boolean success = false;

        String zkUrl = request.getParameter("zkUrl");
        String group = request.getParameter("group");
        String topic = request.getParameter("topic");

        String username = request.getParameter("userName");
        String password = request.getParameter("password");

        boolean isValidate = verify(username, password);
        //isValidate = true;
        if( isValidate==true
                && zkUrl != null && !zkUrl.equals("-1")
                && group != null && !group.equals("-1")
                && topic != null && !topic.equals("-1")) {

            ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
            String path = ConstantsHolder.monitorRootPath + "/" +  group + "/" + topic;
            boolean rst = zkClient.exists(path);
            if(rst) {
                String data = zkClient.readData(path, true);

                if (data != null && data.length() > 0) {
                    data = MonitorHolder.modifyZkData(data, username, null, null, null, MonitorHolder.DECREASE);
                    zkClient.writeData(path, data);
                    success = true;
                }
            }

        }

        result.put("success",success);
        write(response,result);
    }




}
