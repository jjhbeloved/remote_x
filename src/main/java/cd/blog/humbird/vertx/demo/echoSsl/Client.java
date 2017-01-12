package cd.blog.humbird.vertx.demo.echoSsl;

import cd.blog.humbird.vertx.demo.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;

/**
 * Created by david on 16/12/30.
 */
public class Client extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(Client.class);
    }

    @Override
    public void start() throws Exception {

        NetClientOptions options = new NetClientOptions().setSsl(true).setTrustAll(true);

        vertx.createNetClient(options).connect(33333, "localhost", res -> {
            if (res.succeeded()) {
                NetSocket sock = res.result();
                sock.handler(buff -> {
                    System.out.println("client receiving " + buff.toString("UTF-8"));
                });

                // Now send some data
                for (int i = 0; i < 10; i++) {
                    String str = "hello " + i + "\n";
                    System.out.println("Net client sending: " + str);
                    sock.write(str);
                }
            } else {
                System.out.println("Failed to connect " + res.cause());
            }
        });
    }
}
