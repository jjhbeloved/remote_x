package cd.blog.humbird.vertx.demo.echo;

import cd.blog.humbird.vertx.demo.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.streams.Pump;

/**
 * Created by david on 16/12/30.
 */
public class Server extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(Server.class);
    }

    @Override
    public void start() throws Exception {
        vertx.createNetServer().connectHandler(sock -> {
            // Create a pump
            Pump.pump(sock, sock).start();
        }).listen(1234);
        System.out.println("Echo server is now listening");

    }

}
