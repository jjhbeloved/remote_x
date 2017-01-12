package cd.blog.humbird.vertx.vx;

import io.vertx.core.AbstractVerticle;

/**
 * Created by david on 17/1/5.
 */
public abstract class VxAbstractVerticle extends AbstractVerticle {

    protected VxConfig config;

    @Override
    public void start() throws Exception {
        config = new VxConfig(vertx);
        config.readConfig(handle -> {
            start(handle);
        });
    }

    public abstract void start(VxConfig config);
}
