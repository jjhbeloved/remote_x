package cd.blog.humbird.vertx.demo.http;

import cd.blog.humbird.vertx.demo.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

/**
 * Created by david on 17/1/10.
 */
public class ClientNet extends AbstractVerticle {

    public static void main(String[] args) {
        Runner.runExample(ClientNet.class);
    }


    @Override
    public void start() throws Exception {
        vertx.createNetClient().connect(13128, "127.0.0.1", sock -> {
            System.out.println(">>>>: " + sock.succeeded());
            if (sock.succeeded()) {
                NetSocket netSocket = sock.result();
                System.out.println(sock.result().remoteAddress());
//                netSocket.write("zxxx");
                netSocket.write(Buffer.buffer("hello"));
                netSocket.handler(buffer -> {
                    System.out.println("hi all...");
                    netSocket.write("go u");
                });
//                netSocket.write("byte ssss");
            } else {
                System.out.println("error: " + sock.cause().getMessage());
            }
//            sock.result().closeHandler(p -> {
//                System.out.println("over");
//            });
        });
    }
}
