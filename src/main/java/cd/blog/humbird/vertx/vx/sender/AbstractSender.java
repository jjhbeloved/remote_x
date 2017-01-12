package cd.blog.humbird.vertx.vx.sender;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageProducer;

/**
 * Created by david on 17/1/6.
 */
public abstract class AbstractSender<T> {

    protected MessageProducer<T> sender;

    public AbstractSender(Vertx vertx, String address) {
        this.sender = vertx.eventBus().sender(address);
    }

    public abstract MessageProducer<T> send(T message);

    public abstract <R> MessageProducer<T> send(T message, Handler<AsyncResult<Message<R>>> replyHandler);

    public void remove() {
        if (sender != null) {
            close();
            sender = null;
        }
    }

    public void close() {
        sender.close();
    }

    public MessageProducer<T> drainHandler(Handler<Void> handler) {
        return sender.drainHandler(handler);
    }

    public boolean writeQueueFull() {
        return sender.writeQueueFull();
    }

}
