package cd.blog.humbird.vertx.vx.consumer;

import cd.blog.humbird.vertx.vx.common.AddressTemplate;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

/**
 * Created by david on 17/1/8.
 */
public class ConnectPingRespConsumer<T> extends AbstractConsumer<T> {

    public ConnectPingRespConsumer(Vertx vertx, String connectId) {
        super(vertx, AddressTemplate.getConnectPingRespAddr(connectId));
    }

    @Override
    public MessageConsumer<T> consume(Handler<Message<T>> handler) {
        return consumer.handler(handler);
    }
}
