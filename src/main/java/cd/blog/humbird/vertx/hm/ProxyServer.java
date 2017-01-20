package cd.blog.humbird.vertx.hm;

import cd.blog.humbird.vertx.hm.beans.Config;
import cd.blog.humbird.vertx.hm.beans.State;
import cd.blog.humbird.vertx.hm.commons.Constant;
import cd.blog.humbird.vertx.hm.commons.EventAddress;
import cd.blog.humbird.vertx.hm.handler.CloseHandler;
import cd.blog.humbird.vertx.hm.utils.HmUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by david on 17/1/17.
 */
public class ProxyServer extends BaseVertx {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServer.class);

    private String channelId = null;

    private AtomicLong channelSendId = new AtomicLong(0);

    private Map<String, State> states = new HashMap();

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    private void handlerConnectMsg(String host, int port, Buffer leftBuf, State state) {
        String connectId = state.getConnectId();
        LOGGER.info("try to create new connectId: {}, channelId: {} using the proxy {}:{}"
                , connectId, channelId, host, port);
        state.installPing();
        // 连接到代理地址
        vertx.createNetClient().connect(port, host, handler -> {
            if (handler.failed()) {
                LOGGER.warn("unable to connect to the proxy server " + config.getProxyIp() + ":" + config.getProxyPort(), handler.cause());
                state.doClose();
            } else {
                NetSocket sock = handler.result();
                state.setSock(sock);
                // 这里相当于 回写, 接受 目标服务器响应, 回写给 通过隧道 连接过来的 客户端
                sock.handler(buffer -> {
                    state.setRecvCount(state.getRecvCount() + buffer.length());
                    state.setLastDataDate(new Date());
                    long curSendId = channelSendId.addAndGet(1);
                    state.sendProxyDataToChannel(buffer, curSendId);
                    state.reset();
                });
                // 写入剩下的数据到真实代理服务器, 这里只会第一次调用到
                if (leftBuf != null) {
                    state.setSendCount(state.getSendCount() + leftBuf.length());
                    sock.write(leftBuf);
                    if (!state.isTargetIpParsed()) {
                        HmUtil.parseTargetIpPort(leftBuf, state);
                    }
                }
                // 这里是后续 的连接 数据 都走这里了
                state.installDataConsumer(true);
                // 这里监听  channel 断开动作, 如果连 channel 都断开了, 所有面向 public 的 connect 都要断开
                MessageConsumer<Object> channelClosedConsumer = vertx.eventBus().consumer(EventAddress.closePublic(channelId));
                channelClosedConsumer.handler(new CloseHandler(connectId, channelId, sock, vertx, channelClosedConsumer, config));

                MessageConsumer<Object> connectCloseConsumer = vertx.eventBus().consumer(EventAddress.closeAddr(connectId));
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
            }
        });
    }

    @Override
    public void start(Config config) {
        vertx.eventBus().consumer(EventAddress.newAddr(), msg -> {
            Buffer dataBuf = (Buffer) msg.body();
            String connectId = dataBuf.getString(Constant.commandHeadLength, Constant.commandHeadWithIdLength);
            State state = new State(connectId, this.channelId, vertx, this.config);

            states.put(connectId, state);
            long recvId = dataBuf.getLong(Constant.commandHeadWithIdLength);
            LOGGER.debug("receive sendId: {}", recvId);
            // left Buf 是 传递的数据, 非协议头
            Buffer leftBuf = dataBuf.slice(Constant.dataCommandHeadLength, dataBuf.length());
            // LOGGER.debug("proxy server recv data: {}", leftBuf);
            handlerConnectMsg(config.getProxyIp(), config.getProxyPort(), leftBuf, state);
        });
    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("close proxy server");
    }
}
