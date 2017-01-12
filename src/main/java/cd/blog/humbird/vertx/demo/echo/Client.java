package cd.blog.humbird.vertx.demo.echo;

import cd.blog.humbird.vertx.demo.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.NetSocket;

/**
 * Created by david on 16/12/30.
 */
public class Client extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(Client.class);
    }


    @Override
    public void start() throws Exception {
        vertx.createNetClient().connect(1234, "localhost", res -> {
            if (res.succeeded()) {
                NetSocket socket = res.result();
                socket.handler(buffer -> {
                    System.out.println("Net client receiving: " + buffer.toString("UTF-8"));
                });
                System.out.println("--------");
                // Now send some data
                for (int i = 0; i < 10; i++) {
                    String str = "hello " + i + "\n";
                    System.out.println("Net client sending: " + str);
                    socket.write(str);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Failed to connect " + res.cause());
            }
        });
    }
}
