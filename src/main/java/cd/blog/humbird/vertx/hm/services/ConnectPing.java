package cd.blog.humbird.vertx.hm.services;

import cd.blog.humbird.vertx.hm.beans.Config;
import cd.blog.humbird.vertx.hm.commons.CmdId;
import cd.blog.humbird.vertx.hm.commons.Constant;
import cd.blog.humbird.vertx.hm.commons.EventAddress;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/17.
 */
public class ConnectPing {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConnectPing.class);

    private Vertx vertx;

    private Config config;

    private String channelId;

    private String connectId;

    private boolean init = false;

    private long periodicId = -1;
    private long timeOutId = -1;
    private MessageConsumer<Object> pingConsumer;
    private MessageConsumer<Object> pongConsumer;

    private Handler<Long> timeOutHandler = p -> {
        LOGGER.warn("not receive ping more than {} second, connectId: {}, try send close."
                , config.getPingTimeOut() / 1000, connectId);
        vertx.eventBus().send(EventAddress.close(this.connectId), "closed by ping");
        this.timeOutId = -1;
    };

    private Handler<Long> periodicHandler = p -> {
        if (timeOutId != -1) {
            LOGGER.warn("The ping periodic is reach, but PingTimeoutId!=-1, connectId: {}", this.connectId);
        } else {
            CmdId cid = CmdId.CONNECT_PING;
            Buffer buf = Buffer.buffer();
            buf.appendInt(cid.getId());
            buf.appendInt(Constant.commandHeadWithIdLength);
            buf.appendString(connectId);
            // TODO 为啥是 channelId, 因为 connect 的ping-pong 要走 channel开辟的隧道才可以
            vertx.eventBus().send(EventAddress.command(this.channelId), buf);
            LOGGER.debug("try send pong command for connectId: {}", connectId);
            newTimeOutId();
        }
    };


    public ConnectPing(Vertx vertx, Config config, String channelId, String connectId) {
        this.vertx = vertx;
        this.config = config;
        this.channelId = channelId;
        this.connectId = connectId;
    }

    public void newPeriodicId() {
        this.periodicId = this.vertx.setPeriodic(this.config.getConnectPingPeriodic(), periodicHandler);
    }

    public void newTimeOutId() {
        stopTimeOutId();
        this.timeOutId = this.vertx.setTimer(this.config.getPingTimeOut(), timeOutHandler);
    }

    public void resetPeriodicId() {
        stopPeriodicId();
        newPeriodicId();
    }

    public void stopPeriodicId() {
        if (this.periodicId != -1) {
            this.vertx.cancelTimer(this.periodicId);
            this.periodicId = -1;
        }
    }

    public void stopTimeOutId() {
        if (this.timeOutId != -1) {
            this.vertx.cancelTimer(this.timeOutId);
            this.timeOutId = -1;
        }
    }

    public void start() {
        if (init) {
            return;
        }
        LOGGER.info("start the pinger for connectId: {}", this.connectId);
        pingConsumer = this.vertx.eventBus().consumer(EventAddress.ping(this.connectId), p -> {
            LOGGER.debug("receive ping request from connectId: {}", this.connectId);
            stopTimeOutId();
        });
        pongConsumer = this.vertx.eventBus().consumer(EventAddress.pongAddr(connectId), p -> {
            CmdId cmId = CmdId.CONNECT_PONG;
            Buffer buf = Buffer.buffer();
            buf.appendInt(cmId.getId());
            buf.appendInt(Constant.commandHeadWithIdLength);
            buf.appendString(connectId);
            vertx.eventBus().send(EventAddress.command(channelId), buf);
        });
        newPeriodicId();
        this.init = true;
    }

    public void stop() {
        LOGGER.info("stop ping-pong request connectId :{}", this.connectId);
        stopPeriodicId();
        stopTimeOutId();
        if (pingConsumer != null) {
            pingConsumer.unregister();
            pingConsumer = null;
        }
        if (pongConsumer != null) {
            pongConsumer.unregister();
            pongConsumer = null;
        }
        init = true;
    }

}
