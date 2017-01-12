package cd.blog.humbird.vertx.vx.consumer;

import cd.blog.humbird.vertx.vx.common.AddressTemplate;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

/**
 * Created by david on 17/1/8.
 */
public class ChannelCloseAddrConsumer<T> extends AbstractConsumer<T> {

    public ChannelCloseAddrConsumer(Vertx vertx, String channelId) {
        super(vertx, AddressTemplate.getChannelClosedAddr(channelId));
    }

    @Override
    public MessageConsumer<T> consume(Handler<Message<T>> handler) {
        return consumer.handler(handler);
    }
}
