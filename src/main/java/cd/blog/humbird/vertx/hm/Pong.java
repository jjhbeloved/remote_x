package cd.blog.humbird.vertx.hm;

import cd.blog.humbird.vertx.demo.util.Runner;
import cd.blog.humbird.vertx.hm.beans.ChannelState;
import cd.blog.humbird.vertx.hm.beans.Config;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by david on 17/1/16.
 */
public class Pong extends BaseVertx {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pong.class);

    private String channelId = null;

    private AtomicInteger numbers = new AtomicInteger(0);

    public static void main(String[] args) {
        String strInstances = "5";
        if (args.length > 0) {
            strInstances = args[0];
        }
        DeploymentOptions depOpt = new DeploymentOptions();
        depOpt.setInstances(Integer.parseInt(strInstances));
        Runner.runExample("./", Pong.class, null, depOpt);
//        Runner.runExample(Pong.class);

    }

    @Override
    public void start(Config config) {
        channelId = UUID.randomUUID().toString();
        LOGGER.info("channelId is: " + channelId);

        LocalMap<String, String> channelMap = vertx.sharedData().getLocalMap("channelIds");
        String number = "" + numbers.getAndIncrement();
        channelMap.put(channelId, number);
        connect();
        ProxyServer proxyServer = new ProxyServer();
        proxyServer.setChannelId(channelId);
        vertx.deployVerticle(proxyServer);
        if (config.isNeedHttpProxy() && "0".equals(number)) {
            vertx.deployVerticle(new Proxy());
        }
    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("close pong");
    }

    public void connect() {
        JsonObject json = new JsonObject();
        json.put("reconnectAttempts", 100000);
        json.put("reconnectInterval", 1000);
        NetClientOptions clientOpt = new NetClientOptions(json);
        clientOpt.setTcpKeepAlive(true);
        vertx.createNetClient().connect(config.getPingPort(), config.getPingIp(), p -> {
            doHandler(p);
        });
    }

    private void doHandler(AsyncResult<NetSocket> result) {
        if (result.succeeded()) {
            LOGGER.info("success connect to proxy server {}:{}",
                    config.getPingIp(),
                    config.getPingPort());
            NetSocket socket = result.result();
            ChannelState channelState = new ChannelState(vertx, socket, config);
            channelState.setChannelId(channelId);
            channelState.setNumber("" + (numbers.get() - 1));
            channelState.setInit(true);
            // 由 触发 第一次 ping, 开始 ping-pong 事务
            channelState.installPing();
            channelState.installCommandParser();
            channelState.installClosePong(this);
            socket.handler(buffer -> {
                channelState.appendBuffer(buffer);
                channelState.reset();
            });
            // 写入通道ID信息
            socket.write("channelId:" + channelId);
            socket.closeHandler(p -> {
                channelState.closePong(this);
            });
        } else {
            LOGGER.error("failed to connect proxy server {}:{} ,cause: {} "
                    , config.getPingIp()
                    , config.getPingPort()
                    , result.cause());
            vertx.setTimer(1000, timeId -> {
                connect();
            });
        }
    }
}
