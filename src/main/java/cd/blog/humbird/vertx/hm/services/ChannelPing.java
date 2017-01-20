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
 * Created by david on 17/1/16.
 */
public class ChannelPing {

    private final static Logger LOGGER = LoggerFactory.getLogger(ChannelPing.class);

    private Vertx vertx;

    private Config config;

    private String channelId;

    private String number;

    private boolean init = false;

    private long periodicId = -1;

    private long timeOutId = -1;

    private MessageConsumer pingConsumer = null;

    private Handler<Long> timeOutHandler = p -> {
        LOGGER.warn("not receive ping more than {} second, channelId: {}, try send close."
                , config.getPingTimeOut() / 1000, channelId);
        vertx.eventBus().send(EventAddress.close(this.channelId), "closed by ping");
        this.timeOutId = -1;
    };

    private Handler<Long> periodicHandler = p -> {
        if (timeOutId != -1) {
            LOGGER.warn("The ping periodic is running, but ping timeoutId != -1, waiting time out or new ping-pong. from {}:{}", this.channelId, this.number);
        } else {
            CmdId cid = CmdId.PING;
            Buffer buf = Buffer.buffer();
            buf.appendInt(cid.getId());
            buf.appendInt(Constant.commandHeadWithIdLength);
            buf.appendString(channelId);
            vertx.eventBus().send(EventAddress.command(this.channelId), buf);
            LOGGER.debug("try send pong command for channelId: {}", channelId);
            newTimeOutId();
        }
    };

    public ChannelPing(Vertx vertx, Config config, String channelId, String number) {
        this.vertx = vertx;
        this.config = config;
        this.channelId = channelId;
        this.number = number;
    }

    public Vertx getVertx() {
        return vertx;
    }

    public Config getConfig() {
        return config;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getNumber() {
        return number;
    }

    public boolean isInit() {
        return init;
    }

    public void newPeriodicId() {
        this.periodicId = this.vertx.setPeriodic(this.config.getPingPeriodic(), periodicHandler);
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
        LOGGER.info("start the pinger for channelId: {}", this.channelId);
        pingConsumer = this.vertx.eventBus().consumer(EventAddress.ping(this.channelId), p -> {
            LOGGER.debug("receive ping request from channelId {}:{}", this.channelId, this.number);
            stopTimeOutId();
        });
        newPeriodicId();
        this.init = true;
    }

    public void stop() {
        LOGGER.info("stop ping-pong request from {}:{}", this.channelId, this.number);
        stopPeriodicId();
        stopTimeOutId();
        if (pingConsumer != null) {
            pingConsumer.unregister();
            pingConsumer = null;
        }
        init = true;
    }


}
