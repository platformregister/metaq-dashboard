package com.sohu.metaq.service;

import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by xiaofeiliu on 2015/7/23.
 */
public class MonitorHolder {

    private final static Logger logger = LoggerFactory.getLogger(MonitorHolder.class);

    //在监控路径上的数据格式:emails#mobiles#maxOverstockSize#probeInterval
    public static String[] zkDataToArray(String zkData) {
        if(zkData == null || zkData == "") {
            return null;
        }

        String[] rst = StringUtils.splitPreserveAllTokens(zkData, "#");
        return rst;
    }

    public static String arrayToZkData(String[] array) {
        if(array == null || array.length <= 0) {
            return null;
        }

/*        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < array.length; i++) {
            sb.append(array[i] != null ? array[i] : "").append("#");
        }*/
        StringBuilder sb = new StringBuilder(array[0]);
        for(int i=1;i<array.length;i++) {
            sb.append("#").append(array[i] != null ? array[i] : "");
        }
        return  sb.toString();
    }

    public static String initZkData(String userName,String mobile,Long maxOverstockSize,Long probeInterval) {
        StringBuilder sb = new StringBuilder();
        sb.append(userName != null ? userName : "").append("#")
                .append(mobile != null ? mobile : "").append("#")
                .append(maxOverstockSize != null ? maxOverstockSize : "").append("#")
                .append(probeInterval != null ? probeInterval : "");
        return  sb.toString();
    }

    public static final int INCREASE = 1;
    public static final int DECREASE = -1;

    public static String modifyZkData(String zkData,String userName,String mobile,Long maxOverstockSize,Long probeInterval,int operation) {
        String[] array = zkDataToArray(zkData);

        //处理userName字段
        if(array[0] != null && array[0].length() > 0) {
            HashMap<String,String> userNameMap = new HashMap<String, String>();
            String[] userNameArray = array[0].split(",");
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<userNameArray.length;i++) {
                userNameMap.put(userNameArray[i],null);
            }

            if(operation == MonitorHolder.INCREASE && userName != null) {
                userNameMap.put(userName,null);
            } else {
                userNameMap.remove(userName);
            }

            if(userNameMap.size() > 0) {
                for (String s : userNameMap.keySet()) {
                    sb.append(s).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
            }
            array[0] = sb.toString();
        } else {
            if(operation == MonitorHolder.INCREASE && userName != null) {
                array[0] = userName;
            }
        }


        //处理mobile字段
        if(array[1] != null && array[1].length() > 0) {
            HashMap<String,String> mobileMap = new HashMap<String, String>();
            String[] mobileArray = array[1].split(",");
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<mobileArray.length;i++) {
                mobileMap.put(mobileArray[i],null);
            }

            if(operation == MonitorHolder.INCREASE && mobile != null) {
                mobileMap.put(mobile,null);
            } else {
                mobileMap.remove(mobile);
            }

            if(mobileMap.size() > 0) {
                for (String s : mobileMap.keySet()) {
                    sb.append(s).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
            }
            array[1] = sb.toString();
        } else {
            if(operation == MonitorHolder.INCREASE && mobile != null) {
                array[1] = mobile;
            }
        }

        if(maxOverstockSize != null) {
            array[2] = maxOverstockSize.toString();
        }

        if(probeInterval != null) {
            array[3] = probeInterval.toString();
        }

        return  arrayToZkData(array);
    }

    public static String getSupervisors(String data) {
        String[] array = zkDataToArray(data);
        if(array[0] != null && array[0].length() > 0) {
            return array[0];
        }
        return null;
    }

    public static void main(String[] args) {

        String test = "aaa,bbb,ccc#11##";
        System.out.println(modifyZkData(test, null, null, null, null, INCREASE));
        System.out.println(modifyZkData(test, "bbb", null, 10l, 9l, DECREASE));

    }

}
