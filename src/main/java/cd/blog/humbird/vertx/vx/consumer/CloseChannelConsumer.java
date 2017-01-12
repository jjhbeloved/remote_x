package cd.blog.humbird.vertx.vx.consumer;

import cd.blog.humbird.vertx.vx.common.AddressTemplate;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

/**
 * Created by david on 17/1/5.
 */
public class CloseChannelConsumer<T> extends AbstractConsumer<T> {

    public CloseChannelConsumer(Vertx vertx, String channelId) {
        super(vertx, AddressTemplate.getChannelClosedAddr(channelId));
    }

    public CloseChannelConsumer(Vertx vertx, String channelId, Handler<Message<T>> handler) {
        super(vertx, AddressTemplate.getChannelClosedAddr(channelId), handler);
    }

    @Override
    public MessageConsumer<T> consume(Handler<Message<T>> handler) {
        return consumer.handler(handler);
    }

}
