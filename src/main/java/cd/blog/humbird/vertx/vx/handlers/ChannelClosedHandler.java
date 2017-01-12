package cd.blog.humbird.vertx.vx.handlers;

import cd.blog.humbird.vertx.vx.VxConfig;
import cd.blog.humbird.vertx.vx.consumer.AbstractConsumer;
import cd.blog.humbird.vertx.vx.consumer.ChannelCloseAddrConsumer;
import cd.blog.humbird.vertx.vx.consumer.ChannelConnectedAddrConsumer;
import cd.blog.humbird.vertx.vx.sender.ConnectCloseAddrSender;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelClosedHandler implements Handler<Message<Object>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelClosedHandler.class);

    private String connectId;
    private String channelId;
    private NetSocket sock;
    private Vertx vertx;
    private ChannelCloseAddrConsumer channelCloseAddrConsumer;
    private VxConfig config;

    public ChannelClosedHandler(String connectId, String channelId
            , NetSocket sock
            , Vertx vertx
            , ChannelCloseAddrConsumer channelCloseAddrConsumer
            , VxConfig config) {
        this.connectId = connectId;
        this.channelId = channelId;
        this.sock = sock;
        this.vertx = vertx;
        this.channelCloseAddrConsumer = channelCloseAddrConsumer;
        this.config = config;
    }

    @Override
    public void handle(Message<Object> msg) {

        LOGGER.warn("The channel {} was closed, try to pause the connect {}", channelId, connectId);
        //channel关闭则暂停接收代理连接数据
        sock.pause();

        AbstractConsumer channelReconnectedAddrConsumer = new ChannelConnectedAddrConsumer<>(vertx, channelId);

        //超过10秒channel连接还没有恢复则关闭代理连接
        long closeTimeId = vertx.setTimer(config.getChannelCloseResumeTimeout(), id -> {
            LOGGER.error("channel {} closed exceed more than {} second" + ",close the connect {}",
                    channelId,
                    config.getChannelCloseResumeTimeout() / 1000,
                    connectId);
            new ConnectCloseAddrSender<>(vertx, connectId).send("channel closed exceed more than " + config.getChannelCloseResumeTimeout() / 1000 + " second");
            channelCloseAddrConsumer.unregister();
            channelReconnectedAddrConsumer.unregister();
        });

        //channel连接恢复则取消关闭连接定时器
        channelReconnectedAddrConsumer.consume(reconnectedMsg -> {
            sock.resume();
            vertx.cancelTimer(closeTimeId);
            channelReconnectedAddrConsumer.unregister();
            LOGGER.warn("The channel {} reconnected, resume the connect {}"
                    , channelId
                    , connectId);
        });
    }

}
