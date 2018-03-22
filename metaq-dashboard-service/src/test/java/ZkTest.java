import com.taobao.metamorphosis.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;

/**
 * Created by xiaofeiliu on 2015/3/12.
 */
public class ZkTest {

    public static void main(String[] args) {

        String zkUrl = "10.10.78.95:2181,10.10.78.61:2181,10.10.77.208:2181,10.10.52.114:2181,10.10.52.115:2181";
        ZkClient zkClient = new ZkClient(zkUrl, 5000, 5000, new ZkUtils.StringSerializer());
        String url = zkClient.readData("/meta/brokers/ids/1",false);
        System.out.println(url);

    }

}
