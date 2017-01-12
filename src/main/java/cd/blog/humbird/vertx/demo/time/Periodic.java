package cd.blog.humbird.vertx.demo.time;

import cd.blog.humbird.vertx.demo.util.Runner;
import io.vertx.core.AbstractVerticle;

/**
 * Created by david on 17/1/4.
 */
public class Periodic extends AbstractVerticle {

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(Periodic.class);
    }

    @Override
    public void start() throws Exception {
        final int[] i = {0};
        vertx.setPeriodic(1000, handler -> {
            System.out.println(i[0]++);
        });
        System.out.println("First this is printed");
    }
}
