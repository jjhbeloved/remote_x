package cd.blog.humbird.vertx.demo.file;

import cd.blog.humbird.vertx.demo.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * Created by david on 17/1/3.
 */
public class FileSystem extends AbstractVerticle {

    JsonObject json = null;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(FileSystem.class);
    }

    @Override
    public void start() throws Exception {
        vertx.fileSystem().readFile("/tmp/c.json", result -> {
            System.out.println("----");
            if (result.succeeded()) {
                Buffer buf = result.result();
                json = new JsonObject(buf.getString(0, buf.length(), "utf-8"));
                System.out.println("s->" + json.getString("name"));
                System.out.println("s->" + json.getInteger("age"));
            } else {
                json = new JsonObject();
                System.out.println("f->");
            }
        });
    }

}
