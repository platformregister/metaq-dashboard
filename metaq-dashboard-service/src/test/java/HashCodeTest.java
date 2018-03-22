/**
 * Created by xiaofeiliu on 2015/3/6.
 */
public class HashCodeTest {

    public static void main(String[] args) {

        String s = "10.10.78.95:2181,10.10.78.61:2181,10.10.77.208:2181,10.10.52.114:2181,10.10.52.115:2181";
        System.out.println(s.hashCode());

        String t = "10.10.53.93:2181,10.10.53.20:2181,10.10.53.21:2181";
        System.out.println(t.hashCode());

    }


}
