package cd.blog.humbird.vertx.vx.consumer;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

/**
 * Created by david on 17/1/5.
 */
public abstract class AbstractConsumer<T> {

    protected MessageConsumer<T> consumer;

    public AbstractConsumer(Vertx vertx, String address) {
        this.consumer = vertx.eventBus().consumer(address);
    }

    public AbstractConsumer(Vertx vertx, String address, Handler<Message<T>> handler) {
        this.consumer = vertx.eventBus().consumer(address, handler);
    }

    public abstract MessageConsumer<T> consume(Handler<Message<T>> handler);

    public void unregister(){
        consumer.unregister();
    };

    public void resume(){
        consumer.resume();
    };

    public void pause(){
        consumer.pause();
    };

    public void remove() {
        if (consumer != null) {
            consumer.unregister();
            consumer = null;
        }
    }
}
