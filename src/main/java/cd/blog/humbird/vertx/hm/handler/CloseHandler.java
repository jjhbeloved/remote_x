package cd.blog.humbird.vertx.hm.handler;

import cd.blog.humbird.vertx.hm.beans.Config;
import cd.blog.humbird.vertx.hm.commons.EventAddress;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/19.
 */
public class CloseHandler implements Handler<Message<Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseHandler.class);

    private String connectId;

    private String channelId;

    private NetSocket sock;

    private Vertx vertx;

    private MessageConsumer<Object> channelClosedConsumer;

    private Config config;

    public CloseHandler(String connectId, String channelId
            , NetSocket sock
            , Vertx vertx
            , MessageConsumer<Object> channelClosedConsumer
            , Config config) {
        this.connectId = connectId;
        this.channelId = channelId;
        this.sock = sock;
        this.vertx = vertx;
        this.channelClosedConsumer = channelClosedConsumer;
        this.config = config;
    }

    @Override
    public void handle(Message<Object> event) {
        LOGGER.warn("The channel {} was closed, try to pause the connect {}", this.channelId, this.connectId);
        //channel关闭则暂停接收代理连接数据
        this.sock.pause();

        MessageConsumer<Object> reconnectedConsumer = vertx.eventBus().consumer(EventAddress.connectedAddr(this.channelId));

        //超过10秒channel连接还没有恢复则关闭 connect 代理连接
        long closeTimeId = vertx.setTimer(config.getCloseResumeTimeOut(), id -> {
            LOGGER.error("channel {} closed exceed more than {} second, close the connect {}",
                    channelId,
                    config.getCloseResumeTimeOut() / 1000,
                    connectId);
            vertx.eventBus().send(EventAddress.closeAddr(connectId),
                    "channel closed exceed more than " + config.getCloseResumeTimeOut() / 1000 + " second");
            reconnectedConsumer.unregister();
            channelClosedConsumer.unregister();
        });

        //channel 连接恢复则取消关闭连接定时器(尝试重连成功后, 继续接受信息, 但是由于在 单节点模式下, 不可能收到第二次 请求)
        reconnectedConsumer.handler(reconnectedMsg -> {
            sock.resume();
            vertx.cancelTimer(closeTimeId);
            reconnectedConsumer.unregister();
            LOGGER.warn("The channel {} reconnected, resume the connect {}", this.channelId, this.connectId);
        });
    }
}
