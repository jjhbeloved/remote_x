package cd.blog.humbird.vertx.vx;

import cd.blog.humbird.vertx.vx.common.CommandId;
import cd.blog.humbird.vertx.vx.common.Constants;
import cd.blog.humbird.vertx.vx.consumer.AbstractConsumer;
import cd.blog.humbird.vertx.vx.consumer.ConnectPingConsumer;
import cd.blog.humbird.vertx.vx.consumer.ConnectPingRespConsumer;
import cd.blog.humbird.vertx.vx.sender.ChannelDataSender;
import cd.blog.humbird.vertx.vx.sender.CloseConnectSender;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/8.
 */
public class VxConnectPing {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxConnectPing.class);
    private Vertx vertx;
    private VxConfig config;
    private String channelId;
    private String connectId;
    private long pingPriodicId = -1;
    private long pingTimeoutId = -1;    // ping 超时判断变量
    private AbstractConsumer<Object> pingConsumer;
    private AbstractConsumer<Object> connectPingRespConsumer;
    private boolean started = false;
    private Handler<Long> timeoutHandler;
    private Handler<Long> pridicHandler;

    public VxConnectPing(Vertx vertx, VxConfig config) {
        this.vertx = vertx;
        this.config = config;
        timeoutHandler = p -> {
            LOGGER.debug("Not receive connect ping Resp more than {} second,connectId: {}, try send Close msg",
                    config.getPingTimeout() / 1000,
                    connectId);
            new CloseConnectSender(vertx, connectId).send(Buffer.buffer("closed by ping"));

        };
        pridicHandler = p -> {
            if (pingTimeoutId != -1) {
                LOGGER.warn("The pingPriodic is reach, but PingTimeoutId != -1, connectId: {}", connectId);
            } else {
                CommandId cid = CommandId.CONNECT_PING;
                Buffer buf = Buffer.buffer();
                buf.appendInt(cid.getId());
                buf.appendInt(Constants.commandHeadWithIdLength);
                buf.appendString(connectId);
                new ChannelDataSender(vertx, channelId).send(buf);
                LOGGER.debug("try send connect ping command for connectId: {}", connectId);
                newPingTimeoutId();
            }
        };
    }

    private void newPriodicTimeid() {
        pingPriodicId = vertx.setPeriodic(config.getConnectPingDelay(), pridicHandler);
    }

    /**
     * 设置 ping 超时处理器
     */
    private void newPingTimeoutId() {
        // 如果 第二次调用, 说明 ping成功, 则取消前一次的 ping 超时定时处理器, 重置 ping
        if (pingTimeoutId != -1) {
            vertx.cancelTimer(pingTimeoutId);
            pingTimeoutId = -1;
        }
        pingTimeoutId = vertx.setTimer(config.getPingTimeout(), timeoutHandler);
    }

    private void canelPingTimeoutId() {
        if (pingTimeoutId != -1) {
            vertx.cancelTimer(pingTimeoutId);
            pingTimeoutId = -1;
        }
    }

    // 这个好像 没什么意义
    public void pause() {
        if (pingPriodicId != -1) {
            vertx.cancelTimer(pingPriodicId);
            pingPriodicId = -1;
        }
        newPriodicTimeid();
    }

    public void start() {
        if (started) {
            return;
        }
        LOGGER.info("start the connect pinger for connectId: {}", connectId);
        started = true;
        connectPingRespConsumer = new ConnectPingRespConsumer(vertx, connectId);
        connectPingRespConsumer.consume(buffer -> {
            // 这里 响应了 ping 取消了上一次的一次性超时监听
            // 由于 存在 定时ping 模块, 会马上重新发送一个 ping, 并且充值 ping 模块
            LOGGER.debug("receive connect ping resp for connectId: {}, from buffer {}", connectId, buffer.body());
            canelPingTimeoutId();
        });

        // 收到 ping 请求, 马上响应 ping resp 来证明连接存在
        pingConsumer = new ConnectPingConsumer(vertx, connectId);
        pingConsumer.consume(buffer -> {
            LOGGER.debug("receive connect ping for connectId: {}, from buffer {}", connectId, buffer.body());
            CommandId respCid = CommandId.CONNECT_PING_RESP;
            Buffer buf = Buffer.buffer();
            buf.appendInt(respCid.getId());
            buf.appendInt(Constants.commandHeadWithIdLength);
            buf.appendString(connectId);
            new ChannelDataSender(vertx, channelId).send(buf);
        });
        newPriodicTimeid();
    }

    public void stop() {
        LOGGER.info("stop the pinger for connectId: {},channelId: {}", connectId, channelId);
        if (pingPriodicId != -1) {
            vertx.cancelTimer(pingPriodicId);
            pingPriodicId = -1;
        }
        if (pingTimeoutId != -1) {
            vertx.cancelTimer(pingTimeoutId);
            pingTimeoutId = -1;
        }
        if (pingConsumer != null) {
            pingConsumer.unregister();
            pingConsumer = null;
        }
        if (connectPingRespConsumer != null) {
            connectPingRespConsumer.unregister();
            connectPingRespConsumer = null;
        }
        started = false;
    }

    /**
     * @return started属性
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * @param channelId 设置channelId属性
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * @param connectId 设置connectId属性
     */
    public void setConnectId(String connectId) {
        this.connectId = connectId;
    }
}
