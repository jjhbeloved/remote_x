package cd.blog.humbird.vertx.vx.publish;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageProducer;

/**
 * Created by david on 17/1/5.
 */
public abstract class AbstractPublisher<T> {

    protected MessageProducer<T> publisher;

    public AbstractPublisher(Vertx vertx, String address) {
        this.publisher = vertx.eventBus().publisher(address);
    }

    public AbstractPublisher(Vertx vertx, String address, DeliveryOptions options) {
        this.publisher = vertx.eventBus().publisher(address, options);
    }

    public abstract MessageProducer<T> publish(T message);
}
