package cd.blog.humbird.vertx.vx;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/5.
 */
public class VxConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxConfig.class);
    private Vertx vertx;
    private JsonObject json = null;

    public static final String VXCFG = "vxcfg";

    public VxConfig(Vertx vertx) {
        this.vertx = vertx;
    }

    public void readConfig(Handler<VxConfig> handler) {
        if (json != null) {
            handler.handle(this);
        }
        String configFile = "./vx.json";
        configFile = System.getProperty(VXCFG, configFile);
        LOGGER.info("using the configFile :{}", configFile);

        vertx.fileSystem().readFile(configFile, result -> {
            if (result.succeeded()) {
                Buffer buf = result.result();
                json = new JsonObject(buf.getString(0, buf.length(), "utf-8"));
            } else {
                LOGGER.warn("can't load the configure file :{}", result.cause().getMessage());
                json = new JsonObject();
            }
            handler.handle(this);
        });
    }

    public String getChannelServerUri() {
        return json.getString("channelServerUri", "/remote_proxy/websock");
    }

    public String getChannelServerHost() {
        return json.getString("channelServerHost", "fzodc.dlna.me");
    }

    public int getChannelServerPort() {
        return json.getInteger("channelServerPort", 9190);
    }

    public String getProxyHost() {
        return json.getString("proxyHost", "localhost");
    }

    public int getProxyPort() {
        return json.getInteger("proxyPort", 1090);
    }

    public int getProxyClientPort() {
        return json.getInteger("proxyClientPort", 9191);
    }

    public int getMonitorPort() {
        return json.getInteger("monitorPort", 9192);
    }

    public int getPingDelay() {
        return json.getInteger("pingDelay", 10000);
    }

    public int getConnectPingDelay() {
        return json.getInteger("connectPingDelay", 600000);
    }

    public int getPingTimeout() {
        return json.getInteger("pingTimeout", 10000);
    }

    public int getChannelCloseResumeTimeout() {
        return json.getInteger("channelCloseResumeTimeout", 60000);
    }

    public int getChannelSendFeedbackTimeout() {
        return json.getInteger("channelSendFeedbackTimeout", 10000);
    }

    public int getRemoteQueryTimeout() {
        return json.getInteger("remoteQueryTimeout", 60000);
    }

    public boolean isWebSock() {
        return json.getBoolean("websock", false);
    }

    public String getChannelClientName() {
        return json.getString("channelClientName");
    }

    public String getDirectConfig() {
        return json.getString("directConfig", "direct_connect.json");
    }

    public long getCheckFileTime() {
        return json.getInteger("checkFileTime", 10000);
    }

    public String getDomainName() {
        return json.getString("domainName", "humbird.blog.cd");
    }

    public boolean isNeedHttpProxy() {
        return json.getBoolean("needHttpProxy", false);
    }

}
