package cd.blog.humbird.vertx.proxy.util;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

/**
 * Created by david on 17/1/4.
 */
public class ProxyUtilsTest {
    @Test
    public void o2b() throws Exception {
//        ChannelClientInfo info = new ChannelClientInfo();
//        info.setComputerName("Hi, man");
//        byte[] bytes = VxUtils.o2b(info);
//        ChannelClientInfo info1 = VxUtils.b2o(bytes);
//        System.out.println(info1.getComputerName());
    }

    @Test
    public void b2o() throws Exception {

    }

    @Test
    public void base64Test() throws EncoderException {
        String t = "admin:admin";
        Base64 base64 = new Base64();
        byte[] bytes = base64.decode("YWRtaW46YWRtaW4=");

        String name = "admin";
        String passwd = "JW#Fh!k9mhgNsrPl";
        System.out.println(new String(bytes));
        System.out.println(new String(base64.encode((name + ":" + passwd).getBytes())));
    }

}