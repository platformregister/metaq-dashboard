package com.sohu.metaq.monitor.util;

import com.sohu.spaces.email.model.Mail;
import com.sohu.spaces.metaq.MetaQProducer;
import com.taobao.metamorphosis.exception.MetaClientException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class MetaqMailProducerUtil {    
	private static final Log log = LogFactory.getLog(MetaqMailProducerUtil.class);
    private static MetaqMailProducerUtil instance = null;
    private MetaQProducer metaQCount;
    
    public MetaqMailProducerUtil(){
        metaQCount = new MetaQProducer();
        metaQCount.setPath("zookeeper1.register.tv.sohu.com:2181,zookeeper2.register.tv.sohu.com:2181,zookeeper3.register.tv.sohu.com:2181,zookeeper4.register.tv.sohu.com:2181,zookeeper5.register.tv.sohu.com:2181");
        metaQCount.setTopic("mail");
        metaQCount.setDebug(false);
        try {
            metaQCount.afterPropertiesSet();
        } catch (Exception e) {
            log.error("##MetaqProducerUtil_metaQCount_build error:",e);
        }
    }

    public static MetaqMailProducerUtil getInstance(){
        if(instance == null){          
             synchronized (MetaqMailProducerUtil.class) {         
                if(instance == null){           
                    instance = new MetaqMailProducerUtil();
                 }          
             }       
          }      
      return instance;
    }

    public void sendMailMessage(final Mail bean){
        try{
            if(bean==null) return;
            metaQCount.produce(toByteArray(bean));
            System.out.println("####2");
        }catch(Exception e){
            log.error("##sendConnectMessage- error=",e);
        }
        return;
        }
    
    
    public byte[] toByteArray (Object obj) {      
        byte[] bytes = null;      
        ByteArrayOutputStream bos = new ByteArrayOutputStream();      
        try {        
            ObjectOutputStream oos = new ObjectOutputStream(bos);         
            oos.writeObject(obj);        
            oos.flush();         
            bytes = bos.toByteArray ();      
            oos.close();         
            bos.close();        
        } catch (IOException ex) {        
            ex.printStackTrace();   
        }      
        return bytes;    
    }

    public static void main(String[] args) throws MetaClientException, IOException, InterruptedException {

         Mail mail = new Mail();
         mail.setFrom("sohu-hdtv@sohu.com");
         mail.setEmailAddresses("xiaofeiliu@sohu-inc.com,feixu@sohu-inc.com".split(","));
         mail.setMailType((byte) 1);
         mail.setFromTitle("测试");
         mail.setTemplate("SohuPlayerPush");
         mail.setSubject("测试");
         mail.setContent("测试");

        try {
            MetaqMailProducerUtil.getInstance().sendMailMessage(mail);
            System.out.println("####2="+mail);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
