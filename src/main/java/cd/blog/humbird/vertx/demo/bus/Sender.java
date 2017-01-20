package cd.blog.humbird.vertx.demo.bus;

import cd.blog.humbird.vertx.demo.util.Runner;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

/**
 * Created by david on 17/1/3.
 */
public class Sender extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
//        Config config = new Config();
//        HazelcastClusterManager mgr = new HazelcastClusterManager();
//        mgr.setConfig(config);
//        Runner.runClusteredExample(Sender.class, mgr);
        Runner.runExample(Sender.class);
    }

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        System.out.println("----");
//        VxQueryParams vxQueryParams = new VxQueryParams();
//        vxQueryParams.setLimit(1000);
//        eventBus.send("xxmmm.19001", vxQueryParams, handler -> {
//            if (handler.succeeded()) {
//                System.out.println("reply: " + handler.result().body());
//            } else {
//
//            }
//        });
    }
}
