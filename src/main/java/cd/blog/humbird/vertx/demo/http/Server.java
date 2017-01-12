package cd.blog.humbird.vertx.demo.http;

import cd.blog.humbird.vertx.demo.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/4.
 */
public class Server extends AbstractVerticle {

    private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(Server.class);
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        // channelId:60bac594-2c2f-4af8-ad95-cee2a2d4825c
        Handler<ServerWebSocket> wsHandler = sock -> {
            handle(sock);
        };

        vertx.createHttpServer(new HttpServerOptions().setTcpKeepAlive(true))
                .websocketHandler(wsHandler)
                .requestHandler(router::accept)
                .listen(33334, "127.0.0.1");
//        vertx.createHttpClient(new HttpClientOptions().setKeepAlive(true).setTcpKeepAlive(true).setConnectTimeout(30000))
//                .websocket(33334, sock -> {
//                    // Create a pump
//                    Pump.pump(sock, sock).start();
//                }).listen(1234);

    }

    public void handle(ServerWebSocket sock) {
        LOGGER.info("connected remote ip->{}, port->{}", sock.remoteAddress().host(), sock.remoteAddress().port());
        sock.handler(p->{
            LOGGER.info("rev count: {}, ", p.length());
            System.out.println("0: " + p.getInt(0));
            System.out.println("1: " + p.getInt(4));
            System.out.println("2: " + p.getString(4, p.length()));
        });
    }
}
