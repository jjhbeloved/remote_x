package cd.blog.humbird.vertx.vx;

import cd.blog.humbird.vertx.vx.common.CommandId;
import cd.blog.humbird.vertx.vx.common.Constants;
import cd.blog.humbird.vertx.vx.consumer.AbstractConsumer;
import cd.blog.humbird.vertx.vx.consumer.ChannelPingRespConsumer;
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
public class VxChannelPing {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxChannelPing.class);
    private Vertx vertx;
    private VxConfig config;
    private String channelId;
    private long pingPriodicId = -1;
    private long pingTimeoutId = -1;
    private AbstractConsumer<Object> pingConsumer;
    private boolean started = false;

    private Handler<Long> timeoutHandler;
    private Handler<Long> pridicHandler;

    public VxChannelPing(Vertx vertx, VxConfig config) {
        this.vertx = vertx;
        this.config = config;
        timeoutHandler = p -> {
            LOGGER.debug("Not receive channel ping Resp more than {} second,channelId: {}, try send Close msg",
                    config.getPingTimeout() / 1000,
                    channelId);
            new CloseConnectSender(vertx, channelId).send(Buffer.buffer("closed by ping"));
            pingTimeoutId = -1;
        };
        pridicHandler = p -> {
            if (pingTimeoutId != -1) {
                LOGGER.warn("The pingPriodic is reach, but PingTimeoutId != -1, channelId: {}", channelId);
            } else {
                CommandId cid = CommandId.CHANNEL_PING;
                Buffer buf = Buffer.buffer();
                buf.appendInt(cid.getId());
                buf.appendInt(Constants.commandHeadWithIdLength);
                buf.appendString(channelId);
                new ChannelDataSender(vertx, channelId).send(buf);
                LOGGER.debug("try send channel ping command for channelId: {}", channelId);
                newPingTimeoutId();
            }
        };
    }

    private void newPriodicTimeid() {
        pingPriodicId = vertx.setPeriodic(config.getPingDelay(), pridicHandler);
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
        LOGGER.info("start the channel pinger for channelId: {}", channelId);
        started = true;
        pingConsumer = new ChannelPingRespConsumer(vertx, channelId);
        pingConsumer.consume(buffer -> {
            // 这里 响应了 ping 取消了上一次的一次性超时监听
            // 由于 存在 定时ping 模块, 会马上重新发送一个 ping, 并且充值 ping 模块
            LOGGER.debug("receive channel ping resp for channelId/buffer: {}", buffer.body());
            canelPingTimeoutId();
        });
        newPriodicTimeid();
    }

    public void stop() {
        LOGGER.info("stop the channel pinger for channelId: {}", channelId);
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
        started = false;
    }

    /**
     * @return started属性
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * @param channelId 设置 channelId 属性
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
