package cd.blog.humbird.vertx.proxy.beans;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by david on 17/1/3.
 */
public class JsonTest {
    Vertx vertx;
    private JsonObject json = null;

    @Before
    public void init() {
        vertx = Vertx.vertx();
    }

    @Test
    public void jsonTest() {
        vertx.fileSystem().readFile("/tmp/c.json", result -> {
            System.out.println("----");
            if (result.succeeded()) {
                Buffer buf = result.result();
                System.out.println("s->" + buf.toString());
                json = new JsonObject(buf.getString(0, buf.length(), "utf-8"));
                System.out.println("s->" + json.getString("name"));
                System.out.println("s->" + json.getInteger("age"));
            } else {
                json = new JsonObject();
                System.out.println("f->");
            }
            json.fieldNames().forEach(System.out::println);
        });
    }
}
