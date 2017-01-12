package cd.blog.humbird.vertx.proxy.beans;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;

/**
 * Created by david on 17/1/9.
 */
public class HttpTest {

    static Vertx vertx = Vertx.vertx();

    public static void serverTest() {

        vertx.createNetServer().connectHandler(sock -> {
            sock.pause();
            sock.write("go U...");
            sock.handler(buffer -> {
                System.out.println(buffer);
            });
            System.out.println("hello pause.");
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            sock.resume();
        }).listen(7777);
    }

    public static void clientTest() {
        vertx.createNetClient().connect(7777, "localhost", sock -> {
            System.out.println("----");
            if (sock.succeeded()) {
                sock.result().handler(buffer -> {
                    System.out.println(buffer);

                });
                sock.result().write("hi man");
            } else {

            }
        });
    }

    public static void main(String[] args) {
//        serverTest();
//        clientTest();
        httpTest();
    }

    public static void httpTest() {
        JsonObject json = new JsonObject();
        json.put("reconnectAttempts", 100000);
        json.put("reconnectInterval", 1000);
        NetClientOptions clientOpt = new NetClientOptions(json);
        clientOpt.setTcpKeepAlive(true);
        vertx.createNetClient(clientOpt).connect(13128, "127.0.0.1", sock-> {
            if (sock.succeeded()) {
                System.out.println(">>>>");
                sock.result().handler(buffer -> {
                    sock.result().write("go u");
                    System.out.println(buffer);
//                    sock.result().close();
                });
                sock.result().closeHandler(p -> {
                    System.out.println("over");
                });
                sock.result().write("CONNECT www.baidu.com:80");
            } else {
                System.out.println("error: " + sock.cause().getMessage());
            }
        });
    }

}
