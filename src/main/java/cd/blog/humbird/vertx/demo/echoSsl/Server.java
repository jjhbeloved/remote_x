package cd.blog.humbird.vertx.demo.echoSsl;

import cd.blog.humbird.vertx.demo.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.NetServerOptions;
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
        System.out.println("Echo server is listened...");
        NetServerOptions options = new NetServerOptions()
                .setSsl(true).setKeyStoreOptions(new JksOptions().setPath("/install_apps/cetc_bak/remote_x/src/main/java/cd/blog/humbird/vertx/demo/echoSsl/server-keystore.jks").setPassword("wibble"));

        vertx.createNetServer(options).connectHandler(sock -> {

            // Create a pump
            Pump.pump(sock, sock).start();

        }).listen(33333);

        System.out.println("Echo server is now listening");
    }
}
