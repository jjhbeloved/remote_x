package cd.blog.humbird.vertx.vx;

import cd.blog.humbird.vertx.demo.util.Runner;
import cd.blog.humbird.vertx.vx.beans.ChannelState;
import cd.blog.humbird.vertx.vx.utils.VxUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by david on 17/1/5.
 */
public class VxChannelClient extends VxAbstractVerticle {

    private static AtomicInteger channelShortId = new AtomicInteger(0);

    private static final Logger LOGGER = LoggerFactory.getLogger(VxChannelClient.class);

    private final String channelId = UUID.randomUUID().toString();

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        String strInstances = "5";
        if (args.length > 0) {
            strInstances = args[0];
        }
//        Runner.runExample(VxChannelClient.class);
        DeploymentOptions depOpt = new DeploymentOptions();
        depOpt.setInstances(Integer.parseInt(strInstances));
        Runner.runExample("./", VxChannelClient.class, null, depOpt);
    }

    @Override
    public void start(VxConfig config) {
        LOGGER.info("channelId: " + channelId);

        LocalMap<String, String> channelMap = VxUtils.getChannelIdsMap(vertx);
        String sChannelShortId = "" + (channelShortId.addAndGet(1));
        channelMap.put(channelId, sChannelShortId);
        connect();

        vertx.deployVerticle(new VxServer(channelId));

        if (config.isNeedHttpProxy() && "1".equals(sChannelShortId)) {
            vertx.deployVerticle(new HttpsVx());
        }
    }

    private void connect() {
        Handler<AsyncResult<NetSocket>> netHandler = netRes -> {
            handle(netRes);
        };
        Handler<WebSocket> webHandler = webSock -> {
            handle(webSock);
        };
        LOGGER.info("try connect to ProxyChannelServer, {}:{}, websock: {}",
                config.getChannelServerHost(),
                config.getChannelServerPort(),
                config.isWebSock() ? config.getChannelServerUri() : "false");
        if (config.isWebSock()) {

        } else {
            JsonObject json = new JsonObject();
            json.put("reconnectAttempts", 100000);
            json.put("reconnectInterval", 1000);
            NetClientOptions clientOpt = new NetClientOptions(json);
            clientOpt.setTcpKeepAlive(true);
            vertx.createNetClient(clientOpt)
                    .connect(config.getChannelServerPort(), config.getChannelServerHost(),
                            netHandler);
        }
    }

    private void handle(WebSocket sock) {
//        doHandler(new MySock(sock));
    }

    private void handle(AsyncResult<NetSocket> res) {
        if (res.succeeded()) {
            doHandler(new VxSock(res.result()));
        } else {
            LOGGER.error("Failed to connect channelServer {}:{} ,cause:{} "
                    , config.getChannelServerHost()
                    , config.getChannelServerPort()
                    , res.cause());
            vertx.setTimer(1000, timeId -> {
                connect();
            });
        }
    }

    private void doHandler(VxSock vxSock) {
        LOGGER.info("success connect to ProxyChannelServer,{}:{}",
                config.getChannelServerHost(),
                config.getChannelServerPort());
        ChannelState channelState = new ChannelState(vertx, config, vxSock, channelId);
        channelState.intallChannelDataConsumer();
        channelState.installPinger();

        // 这里 解析 server 端传回的命令
        vxSock.handler(buffer -> {
            channelState.appendBuffer(buffer);
            channelState.pausePinger();
        });
        // 写入通道ID信息
        vxSock.write("channelId:" + channelId);
        vxSock.closeHandler(v -> {
            LOGGER.info("sock closeHandler channelId {}", channelId);
//            channelState.doChannelClientClose(this);
        });
    }

}
