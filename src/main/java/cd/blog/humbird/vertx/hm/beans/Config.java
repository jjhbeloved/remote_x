package cd.blog.humbird.vertx.hm.beans;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/16.
 */
public class Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    private Vertx vertx;

    private JsonObject json = null;

    public Config() {
    }

    public Config(Vertx vertx) {
        this.vertx = vertx;
    }

    public String getPingIp() {
        return json.getString("pingHost", "0.0.0.0");
    }

    public int getPingPort() {
        return json.getInteger("pingPort", 13128);
    }

    public String getProxyIp() {
        return json.getString("proxyHost", "0.0.0.0");
    }

    public int getProxyPort() {
        return json.getInteger("proxyPort", 13127);
    }

    public String getClientIp() {
        return json.getString("clientHost", "0.0.0.0");
    }

    public int getClientPort() {
        return json.getInteger("clientPort", 13129);
    }

    public int getPingPeriodic() {
        return json.getInteger("pingPeriodic", 10000);
    }

    public int getPingTimeOut() {
        return json.getInteger("pingTimeout", 10000);
    }

    public int getConnectPingPeriodic() {
        return json.getInteger("connectPingPeriodic", 600000);
    }

    public int getCloseResumeTimeOut() {
        return json.getInteger("closeResumeTimeOut", 120000);
    }

    public boolean isNeedHttpProxy() {
        return json.getBoolean("needHttpProxy", true);
    }

    public void readConfig(Handler<Config> handler) {
        if (json != null) {
            handler.handle(this);
        }
        String configFile = "./hm.json";
        configFile = System.getProperty("hmcfg", configFile);
        LOGGER.info("using the configFile: {}", configFile);

        vertx.fileSystem().readFile(configFile, result -> {
            if (result.succeeded()) {
                Buffer buf = result.result();
                json = new JsonObject(buf.getString(0, buf.length(), "utf-8"));

            } else {
                LOGGER.warn("can't load the configure file: {}", result.cause().getMessage());
                json = new JsonObject();
            }
            handler.handle(Config.this);
        });
    }
}
