package com.sohu.metaq.monitor.config.impl;

import com.sohu.metaq.monitor.config.LoadMonitorConfig;
import com.sohu.metaq.monitor.config.MonitorConfig;
import com.sohu.metaq.service.ConstantsHolder;
import com.sohu.metaq.service.MonitorHolder;
import com.sohu.metaq.service.ResourcesHolder;
import com.taobao.metamorphosis.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiaofeiliu on 2015/7/17.
 */
public class LoadMonitorConfigByZk implements LoadMonitorConfig {

    private final static Logger logger = LoggerFactory.getLogger(LoadMonitorConfigByZk.class);

    private String[] zkUrls;

    public LoadMonitorConfigByZk(String[] zkUrls) {
        this.zkUrls = zkUrls;
    }

    @Override
    public List<MonitorConfig> load(String resource){

        List<MonitorConfig> monitorConfigs = new ArrayList<MonitorConfig>();

        for(String zkUrl : zkUrls) {
            try {
                ZkClient zkClient = ResourcesHolder.zkClientCache.get(zkUrl);
                List<String> groups = zkClient.getChildren(ConstantsHolder.monitorRootPath);
                if(groups != null && groups.size() > 0) {
                    for (String group : groups) {
                        List<String> topics = zkClient.getChildren(ConstantsHolder.monitorRootPath + "/" + group);
                        if(topics != null && topics.size() > 0) {
                            for(String topic : topics) {
                                String zkData = zkClient.readData(ConstantsHolder.monitorRootPath + "/" + group + "/" + topic, true);
                                String[] array = MonitorHolder.zkDataToArray(zkData);

                                if(array == null) {
                                    continue;
                                }
                                MonitorConfig monitorConfig = new MonitorConfig();

                                if(array[0]!=null && array[0].length() > 0) {
                                    String[] emails = array[0].split(",");
                                    for(int i = 0; i < emails.length; i++) {
                                        emails[i] = emails[i] + "@sohu-inc.com";
                                    }
                                    monitorConfig.setMailList(emails);
                                } else {
                                    continue;
                                }

                                if(array[1]!=null && array[1].length() > 0) {
                                    String[] mobiles = array[1].split(",");
                                    monitorConfig.setMobileList(mobiles);
                                }

                                if(array[2]!=null && array[2].length() > 0) {
                                    monitorConfig.setMaxOverstockSize(Long.parseLong(array[2]));
                                }

                                if(array[3]!=null && array[3].length() > 0) {
                                    monitorConfig.setProbeInterval(Long.parseLong(array[3]));
                                }

                                monitorConfig.setServerUrl(zkUrl);
                                monitorConfig.setGroupName(group);
                                monitorConfig.setTopicName(topic);

                                monitorConfigs.add(monitorConfig);

                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("LoadMonitorConfigByZk load() is error.",e);
            }
        }

        return monitorConfigs;

    }


}
