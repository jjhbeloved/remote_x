package cd.blog.humbird.vertx.demo.http;

import cd.blog.humbird.vertx.demo.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.shareddata.LocalMap;

import java.util.UUID;

/**
 * Created by david on 17/1/5.
 */
public class Client extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(Client.class);
    }


    /**
     * channelId(9):(1)uuid(36) = 46
     * 一个整形占4个byte, 所以在Buffer传输中, 每个整形的占位是0, 4, 8
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        String channelId = UUID.randomUUID().toString();    // 36
        LocalMap<String, String> channelMap = vertx.sharedData().getLocalMap("channelIds");
                vertx.createHttpClient(new HttpClientOptions().setKeepAlive(true).setTcpKeepAlive(true).setConnectTimeout(30000))
                .websocket(33334, "127.0.0.1", "/test", sock -> {
                    Buffer buffer = Buffer.buffer();
//                    buffer.appendInt(-2);
//                    buffer.appendInt(4);
//                    buffer.appendInt(-9);

//                    buffer.setInt(0, -2);
//                    buffer.setInt(4, 4);
//                    buffer.setInt(9, 8);

                    buffer.appendInt(-2);
                    buffer.appendString("hello world.");
                    buffer.setInt(4, 8);

//                    buffer.setInt(3, 34444);
//                    buffer.setString(3, "channelId:" + channelId);
//                    buffer.setInt(4, 4);
                    sock.write(buffer);
                });
    }
}
