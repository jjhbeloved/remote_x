package cd.blog.humbird.vertx.vx.consumer;

import cd.blog.humbird.vertx.vx.common.AddressTemplate;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

/**
 * Created by david on 17/1/9.
 */
public class MonitorRemoteConnectCloseAddrConsumer<T> extends AbstractConsumer<T> {

    public MonitorRemoteConnectCloseAddrConsumer(Vertx vertx) {
        super(vertx, AddressTemplate.getMonitorRemoteConnectCloseAddr());
    }

    @Override
    public MessageConsumer<T> consume(Handler<Message<T>> handler) {
        return consumer.handler(handler);
    }
}
