package cd.blog.humbird.vertx.demo.bus;

import cd.blog.humbird.vertx.demo.util.Runner;
import com.hazelcast.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

/**
 * Created by david on 17/1/3.
 */
public class Receiver extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Config config = new Config();
//        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("127.0.0.1").setEnabled(true);
//        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
//        HazelcastClusterManager mgr = new HazelcastClusterManager();
//        mgr.setConfig(config);
//        Runner.runClusteredExample(Receiver.class, mgr);
        Runner.runExample(Receiver.class);
    }

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("xxmmm.19001", message -> {
            System.out.println("from: " + message.address() + ", message: " + message.body().toString());
            message.reply("Go U.");
        }).completionHandler(handler -> {
            if (handler.succeeded()) {
                System.out.println("succeeded");
            } else {
                System.out.println("failed");
            }
        });
    }
}
