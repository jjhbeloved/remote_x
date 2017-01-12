package cd.blog.humbird.vertx.vx.publish;

import cd.blog.humbird.vertx.vx.common.AddressTemplate;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageProducer;

/**
 * Created by david on 17/1/7.
 */
public class ChannelClosedAddrPublisher<T> extends AbstractPublisher<T> {

    public ChannelClosedAddrPublisher(Vertx vertx, String channelId) {
        super(vertx, AddressTemplate.getChannelClosedAddr(channelId));
    }

    @Override
    public MessageProducer<T> publish(T message) {
        return publisher.send(message);
    }
}
