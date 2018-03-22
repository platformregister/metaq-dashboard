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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaofeiliu on 2015/8/14.
 */
@Controller
public class InChargeController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(InChargeController.class);

    @RequestMapping(value = "getInCharge.do", method = RequestMethod.POST)
    public void getInCharge(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {

        Map result = new HashMap();
        boolean success = false;

        String zkUrl = request.getParameter("zkUrl");

        HashMap<String,String> inChargePersons = new HashMap<String, String>();

        if( zkUrl != null && !zkUrl.equals("-1")) {

            ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);

            String path1 = ConstantsHolder.inChargeRootPath;
            boolean rst1 = zkClient.exists(path1);
            if(rst1) {
                List<String> groups1 = zkClient.getChildren(path1);
                for(String group1 : groups1) {
                    List<String> topics1 = new ArrayList<String>();
                    try {
                        topics1 = zkClient.getChildren(path1 + "/" + group1);
                    } catch (Exception e) {
                        logger.error("InChargeController getTopic1 is error.",e);
                    }
                    for(String topic1 : topics1) {
                        String inChargePerson1 = zkClient.readData(path1 + "/" + group1 + "/" + topic1);
                        if(inChargePerson1 != null && inChargePerson1.length() > 0) {
                            StringBuilder sb = new StringBuilder(group1);
                            sb.append("#");
                            sb.append(topic1);
                            sb.append("#");
                            sb.append(inChargePerson1);
                            inChargePersons.put(sb.toString(), null);
                        }
                    }
                }
            }

            String path2 = ConstantsHolder.monitorRootPath;
            boolean rst2 = zkClient.exists(path2);
            if(rst2) {
                List<String> groups2 = zkClient.getChildren(path2);
                for(String group2 : groups2) {
                    List<String> topics2 = new ArrayList<String>();
                    try {
                        topics2 = zkClient.getChildren(path2 + "/" + group2);
                    } catch (Exception e) {
                        logger.error("InChargeController getTopic2 is error.",e);
                    }

                    for(String topic2 : topics2) {
                        String inChargePerson2 = zkClient.readData(path2 + "/" + group2 + "/" + topic2);
                        if(inChargePerson2 != null && inChargePerson2.length() > 0 && !inChargePerson2.equals("###")) {
                            StringBuilder sb = new StringBuilder(group2);
                            sb.append("#");
                            sb.append(topic2);
                            sb.append("#");
                            sb.append(inChargePerson2);
                            inChargePersons.put(sb.toString(), null);
                        }
                    }
                }

            }

        }

        if(inChargePersons.size() > 0) {
            success = true;
        }

        result.put("success",success);
        result.put("inChargePersons",inChargePersons.keySet());
        write(response,result);
    }

    @RequestMapping(value = "modifyInCharge.do", method = RequestMethod.POST)
    public void modifyInCharge(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
        Map result = new HashMap();
        boolean success = false;

        String zkUrl = request.getParameter("zkUrl");
        String group = request.getParameter("group");
        String topic = request.getParameter("topic");

        String username = request.getParameter("userName");
        String password = request.getParameter("password");

        String operation = request.getParameter("operation");

        boolean isValidate = verify(username, password);
        //boolean isValidate = true;
        if( isValidate==true
                && zkUrl != null && !zkUrl.equals("-1")
                && group != null && !group.equals("-1")
                && topic != null && !topic.equals("-1")) {

            ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
            StringBuilder sb = new StringBuilder(ConstantsHolder.inChargeRootPath);
            sb.append("/").append(group).append("/").append(topic);
            String path = sb.toString();
            String data = null;
            try {
                if(zkClient.exists(path)) {
                    data = zkClient.readData(path);
                } else {
                    zkClient.createPersistent(path, true);
                }
                success = true;
            } catch (Exception e) {
                logger.error("InChargeController addInCharge is error.", e);
            }

            if(data != null && data.length() > 0) {
                HashMap<String,String> inChargePersonMap = new HashMap<String, String>();
                String[] inChargePersonArray = data.split(",");
                StringBuilder temp = new StringBuilder();
                for(int i=0;i<inChargePersonArray.length;i++) {
                    inChargePersonMap.put(inChargePersonArray[i],null);
                }
                if(operation.equals("add")) {
                    inChargePersonMap.put(username, null);
                } else if(operation.equals("del")) {
                    inChargePersonMap.remove(username);
                }

                if(inChargePersonMap.size() > 0) {
                    for (String s : inChargePersonMap.keySet()) {
                        temp.append(s).append(",");
                    }
                    temp.deleteCharAt(temp.length() - 1);
                }
                data = temp.toString();
                zkClient.writeData(path,data);
            } else if(operation.equals("add")) {
                data = username;
                zkClient.writeData(path,data);
            }
        }
        result.put("isValidate",isValidate);
        result.put("success",success);
        write(response,result);

    }

/*    @RequestMapping(value = "deleteInCharge.do", method = RequestMethod.POST)
    public void deleteInCharge(HttpServletRequest request,HttpServletResponse response,ModelAndView model) {
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
            StringBuilder sb = new StringBuilder(ConstantsHolder.inChargeRootPath);
            sb.append("/").append(group).append("/").append("topic");
            String path = sb.toString();
            String data = null;
            try {
                if(zkClient.exists(path)) {
                    data = zkClient.readData(path);
                } else {
                    zkClient.createPersistent(path, true);
                }
                success = true;
            } catch (Exception e) {
                logger.error("InChargeController deleteInCharge is error.", e);
            }

            if(data != null && data.length() > 0) {
                HashMap<String,String> inChargePersonMap = new HashMap<String, String>();
                String[] inChargePersonArray = data.split(",");
                StringBuilder temp = new StringBuilder();
                for(int i=0;i<inChargePersonArray.length;i++) {
                    inChargePersonMap.put(inChargePersonArray[i],null);
                }
                inChargePersonMap.remove(username);

                if(inChargePersonMap.size() > 0) {
                    for (String s : inChargePersonMap.keySet()) {
                        temp.append(s).append(",");
                    }
                    temp.deleteCharAt(sb.length() - 1);
                }
                data = temp.toString();
                zkClient.writeData(path,data);
            }
        }

        result.put("success",success);
        write(response,result);

    }*/



}
