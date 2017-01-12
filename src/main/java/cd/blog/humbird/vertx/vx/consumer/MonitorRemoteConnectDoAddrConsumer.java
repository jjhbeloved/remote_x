package cd.blog.humbird.vertx.vx.consumer;

import cd.blog.humbird.vertx.vx.common.AddressTemplate;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

/**
 * Created by david on 17/1/9.
 */
public class MonitorRemoteConnectDoAddrConsumer<T> extends AbstractConsumer<T> {

    public MonitorRemoteConnectDoAddrConsumer(Vertx vertx) {
        super(vertx, AddressTemplate.getMonitorRemoteConnectDoAddr());
    }

    @Override
    public MessageConsumer<T> consume(Handler<Message<T>> handler) {
        return consumer.handler(handler);
    }
}
