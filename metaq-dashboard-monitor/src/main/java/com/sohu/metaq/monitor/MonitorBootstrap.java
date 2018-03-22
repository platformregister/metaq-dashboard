package com.sohu.metaq.monitor;

import com.sohu.metaq.monitor.config.LoadMonitorConfig;
import com.sohu.metaq.monitor.config.MonitorConfig;
import com.sohu.metaq.monitor.config.impl.LoadMonitorConfigByZk;
import com.sohu.metaq.monitor.core.ProberManager;
import com.sohu.metaq.monitor.offsetprobe.OffsetProber;
import com.sohu.metaq.monitor.parser.YamlParser;
import com.sohu.metaq.service.ResourcesHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xiaofeiliu on 2015/7/17.
 */
public class MonitorBootstrap {

    public static void main(String[] args) throws Exception {

        HashMap<String,String> monitorConfigs = YamlParser.parse("etc/monitorConfig.yaml");
        //HashMap<String,String> monitorConfigs = YamlParser.parse("D:/code/metaq-dashboard/metaq-dashboard-monitor/etc/monitorConfig.yaml");

        String dmos = monitorConfigs.get("defaultMaxOverstockSize");
        if(dmos != null && dmos.length() > 0) {
            MonitorConfig.defaultMaxOverstockSize = Long.parseLong(dmos);
        }
        String dpi = monitorConfigs.get("defaultProbeInterval");
        if(dpi != null && dpi.length() > 0) {
            MonitorConfig.defaultProbeInterval = Long.parseLong(dpi);
        }

        String resource = monitorConfigs.get("zkUrls");

        if(resource == null || resource.length() <= 0) {
            throw new NullPointerException();
        }
        String[] zkUrls = resource.split(";");

        List<String> zookeeperUrls = new ArrayList<String>();
        for(String zkUrl : zkUrls) {
            zookeeperUrls.add(zkUrl);
        }
        ResourcesHolder resourcesHolder = new ResourcesHolder();
        resourcesHolder.setZkUrls(zookeeperUrls);
        resourcesHolder.setZkTimeout(60000);
        resourcesHolder.init();

        LoadMonitorConfig loadMonitorConfig = new LoadMonitorConfigByZk(zkUrls);

        OffsetProber offsetProber = new OffsetProber();
        offsetProber.setLoadMonitorConfig(loadMonitorConfig);

        ProberManager proberManager = new ProberManager();
        proberManager.registerProber(offsetProber);
        proberManager.initProber();
        proberManager.startProber();

        while (true) Thread.sleep(1000 * 60 * 60);

    }


}
