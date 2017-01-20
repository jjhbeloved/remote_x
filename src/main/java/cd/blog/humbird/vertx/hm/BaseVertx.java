package cd.blog.humbird.vertx.hm;

import cd.blog.humbird.vertx.hm.beans.Config;
import io.vertx.core.AbstractVerticle;

/**
 * Created by david on 17/1/19.
 */
public abstract class BaseVertx extends AbstractVerticle {

    protected Config config;

    @Override
    public void start() throws Exception {
        config = new Config(vertx);
        config.readConfig(configRs -> {
            start(configRs);
        });
    }

    public abstract void start(Config config);

}
