package cd.blog.humbird.vertx.proxy.beans;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.LocalMap;
import org.junit.Test;

/**
 * Created by david on 17/1/3.
 */
public class ProxyStateTest {

    @Test
    public void localShard() {
        Vertx vertx = Vertx.vertx();

        LocalMap<String, String> map1 = vertx.sharedData().getLocalMap("demo1");
        map1.put("ai", "ya");
        LocalMap<String, Buffer> map2 = vertx.sharedData().getLocalMap("demo2");
        map2.put("hi", Buffer.buffer().appendString("baby"));

        System.out.println(vertx.sharedData().getLocalMap("demo1").get("ai"));
        Buffer buffer = (Buffer) vertx.sharedData().getLocalMap("demo2").get("hi");
        System.out.println(buffer.toString());
    }

}