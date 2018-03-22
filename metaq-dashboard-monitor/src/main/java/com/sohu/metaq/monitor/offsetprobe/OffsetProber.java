package com.sohu.metaq.monitor.offsetprobe;

import com.sohu.metaq.monitor.config.LoadMonitorConfig;
import com.sohu.metaq.monitor.config.MonitorConfig;
import com.sohu.metaq.monitor.core.AbstractProber;
import com.sohu.metaq.monitor.core.Prober;
import com.sohu.metaq.monitor.core.ProberManager;
import com.sohu.metaq.monitor.util.MetaqMailProducerUtil;
import com.sohu.metaq.service.FunctionHolder;
import com.sohu.spaces.email.model.Mail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by xiaofeiliu on 2015/6/12.
 */
public class OffsetProber extends AbstractProber {

    private final static Logger logger = LoggerFactory.getLogger(OffsetProber.class);

    private LoadMonitorConfig loadMonitorConfig;

    public void setLoadMonitorConfig(LoadMonitorConfig loadMonitorConfig) {
        this.loadMonitorConfig = loadMonitorConfig;
    }

    private List<MonitorConfig> monitorConfigs;

    @Override
    public void init() {

    }

    @Override
    protected void doStopProb() {

    }

    @Override
    protected void doStartProb() throws InterruptedException {

/*        //判断哪些监控是进行过参数个性化设置的
        List<MonitorConfig> monitorConfigCustomList = new ArrayList<MonitorConfig>();
        List<MonitorConfig> monitorConfigCommonList = new ArrayList<MonitorConfig>();
        for(MonitorConfig monitorConfig : monitorConfigs) {
            if(monitorConfig.getMaxOverstockSize() != 0l || monitorConfig.getProbeInterval() != 0l) {
                monitorConfigCustomList.add(monitorConfig);
            } else {
                monitorConfigCommonList.add(monitorConfig);
            }
        }

        //定制任务
        for(MonitorConfig monitorConfig : monitorConfigCustomList) {




        }*/

        //通用任务
        CommonProbeTask probeTask = new CommonProbeTask(null);
        //probeTask.run(); //for test
        Timer timer = new Timer();
        timer.schedule(probeTask, 0l ,MonitorConfig.defaultProbeInterval);
    }

    private  void sendMail(String[] sendManArray,String title, String content) {
        try {
            Mail mail = new Mail();
            mail.setFrom("sohu-hdtv@sohu.com");
            mail.setEmailAddresses(sendManArray);
            mail.setMailType((byte) 1);
            mail.setFromTitle(title);
            mail.setTemplate("SohuPlayerPush");
            mail.setSubject(title);
            mail.setContent(content);
            MetaqMailProducerUtil.getInstance().sendMailMessage(mail);
        } catch (Exception e) {
            logger.error("MetaqMailProducerUtil's sendMail is error." + sendManArray,e);
        }
    }

    class CommonProbeTask extends TimerTask {

        private List<MonitorConfig> monitorConfigs;

        public CommonProbeTask(List<MonitorConfig> monitorConfigs) {
            this.monitorConfigs = monitorConfigs;
        }

        @Override
        public void run() {

            monitorConfigs = loadMonitorConfig.load(null);
            for(MonitorConfig monitorConfig : monitorConfigs) {
                final HashMap<String, String> max_min_offset = new HashMap<String, String>();
                final HashMap<String, String> avg_size = new HashMap<String, String>();
                boolean isOverstocked = false;
                long overstockNum = 0l;
                try {
                    FunctionHolder.getOffsetInfo(monitorConfig.getServerUrl(), monitorConfig.getGroupName(), monitorConfig.getTopicName(), max_min_offset, avg_size, false);

                    //计算消息的平均大小
                    long avgSize = 1l;
                    for (String key : avg_size.keySet()) {
                        String[] values = avg_size.get(key).split("#");
                        if (values.length == 2 && Integer.parseInt(values[0]) != 0) {
                            avgSize = Long.parseLong(values[1]) / Long.parseLong(values[0]);
                        }
                    }

                    for (String key : max_min_offset.keySet()) {
                        String[] values = max_min_offset.get(key).split("#");
                        long overstockSize = Long.parseLong(values[1]) - Long.parseLong(values[2]);
                        if (overstockSize > 0 && overstockSize/avgSize > MonitorConfig.defaultMaxOverstockSize) {
                            isOverstocked = true;
                            overstockNum = overstockSize/avgSize;
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error("CommonProbeTask is error.",e);
                }

                if(isOverstocked) {
                    StringBuilder sb = new StringBuilder("您所在group名称为：");
                    sb.append(monitorConfig.getGroupName())
                            .append("，订阅topic名称为：")
                            .append(monitorConfig.getTopicName())
                            .append("的消息积压了")
                            .append(overstockNum)
                            .append("条，请及时查看.\n")
                            .append("http://10.10.77.26:8880/clusterU.do?zkUrlPre=")
                            .append(monitorConfig.getServerUrl())
                            .append("&groupPre=")
                            .append(monitorConfig.getGroupName())
                            .append("&topicPre=")
                            .append(monitorConfig.getTopicName());

                    sendMail(monitorConfig.getMailList(),"消息积压报警",sb.toString());
                    logger.error("Some monitor's state is ill." + monitorConfig.getGroupName());
                } else {
                    logger.error("This monitor's state is health.");
                }

            }


        }
    }

}
