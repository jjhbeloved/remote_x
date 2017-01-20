package cd.blog.humbird.vertx.hm;

import cd.blog.humbird.vertx.hm.beans.Config;
import cd.blog.humbird.vertx.hm.beans.State;
import cd.blog.humbird.vertx.hm.commons.EventAddress;
import cd.blog.humbird.vertx.hm.handler.CloseHandler;
import cd.blog.humbird.vertx.hm.utils.HmUtil;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by david on 17/1/18.
 * <p>
 * ProxyClient close的原因有
 * 1. channel 隧道断开
 * 2. 远程 ProxyServer 断开
 * 3. 客户端 连接断开
 */
public class ProxyClient extends BaseVertx {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyClient.class);

    private AtomicLong channelSendId = new AtomicLong(0);

    private AtomicInteger connCount = new AtomicInteger(0);

    private NetServer server = null;

    protected String getDomainName() {
        return null;
    }

    private boolean handelNewConnect(Config config, NetSocket sock, String channelId, String number) {
        LocalMap<String, String> connectMap = vertx.sharedData().getLocalMap("connectIds");

        String connectId = UUID.randomUUID().toString();
        String connectShortId = "" + (connCount.addAndGet(1));

        connectMap.put(connectId, channelId);
        State state = new State(connectId, channelId, vertx, config);
        state.setSock(sock);
        state.setChannelShortId(number);
        state.setConnectShortId(connectShortId);
        state.setClientIp(sock.remoteAddress().host());
        state.setClientPort(sock.remoteAddress().port());

        LOGGER.info("new connectId:{}, choose the channel: {}, clientIp: {}", connectId, channelId, state.getClientIp());

        // 这里监听  channel 断开动作, 如果连 channel 都断开了, 所有面向 public 的 connect 都要断开
        MessageConsumer<Object> channelClosedConsumer = vertx.eventBus().consumer(EventAddress.closePublic(channelId));
        channelClosedConsumer.handler(new CloseHandler(connectId, channelId, sock, vertx, channelClosedConsumer, config));

        MessageConsumer<Object> connectCloseConsumer = vertx.eventBus().consumer(EventAddress.closeAddr(connectId));

        state.installDataConsumer(false);

        // 这里相当于 接受客户端请求, 然后发送给 隧道
        sock.handler(buffer -> {
            state.setLastDataDate(new Date());
            long curSendId = channelSendId.addAndGet(1L);
            if (!state.isTargetIpParsed()) {
                HmUtil.parseTargetIpPort(buffer, state);
            }
            state.setSendCount(state.getSendCount() + buffer.length());
            if (!state.isConnected()) {
                if (!state.isPass()) {
                    return;
                }
                LOGGER.debug(">>>>");
                state.sendDataToChannel(buffer, curSendId);
            } else {
                if (!state.isPass()) {
                    return;
                }
                LOGGER.debug(">>>>-->>>>");
                state.sendProxyDataToChannel(buffer, curSendId);
            }
            state.reset();
        });

        // 关闭 客户端 连接信息
        // 这里应该是 通过监控模块 monitor 来关闭
        connectCloseConsumer.handler(msg -> {
            LOGGER.info("receive close command: " + connectId + ", close by " + msg.body());
            connectCloseConsumer.unregister();
            channelClosedConsumer.unregister();
            state.doClose();
            sock.close();
            LOGGER.info("success close socke: " + connectId);
        });
        //从 channelId 里面删除对应的 连接数
        // 这里是 通过 sock 连接来关闭
        sock.closeHandler(v -> {
            LOGGER.info("send close command: " + connectId);
            connectCloseConsumer.unregister();
            channelClosedConsumer.unregister();
            state.doClose();
            vertx.eventBus().send(EventAddress.removeAddr(), state.getChannelId());
        });
        return false;
    }

    @Override
    public void start(Config config) {
        server = vertx.createNetServer().connectHandler(sock -> {
            sock.pause();
            vertx.eventBus().send(EventAddress.chooseAddr(), getDomainName(), result -> {
                if (result.succeeded()) {
                    String tmp = (String) result.result().body();
                    if (tmp == null) {
                        LOGGER.error("no avariable channel");
                        sock.close();
                        return;
                    }
                    String[] aTmp = tmp.split(":");
                    String channelId = aTmp[0];
                    String number = aTmp[1];
                    if (!handelNewConnect(config, sock, channelId, number)) {
                        sock.resume();
                    }
                } else {
                    LOGGER.error("choose channel fail");
                    sock.close();
                    return;
                }
            });
        }).listen(config.getClientPort(), config.getClientIp());
        LOGGER.info("client is now listening on {}", config.getClientPort());
    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("close proxy client");
    }
}
