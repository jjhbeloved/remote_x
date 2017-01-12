package cd.blog.humbird.vertx.vx.sender;

import cd.blog.humbird.vertx.vx.common.AddressTemplate;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageProducer;

/**
 * Created by david on 17/1/9.
 */
public class DirectConnectedAddrSender<T> extends AbstractSender<T> {

    public DirectConnectedAddrSender(Vertx vertx, String connectId) {
        super(vertx, AddressTemplate.getDirectConnectedAddr(connectId));
    }

    @Override
    public MessageProducer<T> send(T message) {
        return sender.send(message);
    }

    @Override
    public <R> MessageProducer<T> send(T message, Handler<AsyncResult<Message<R>>> replyHandler) {
        return sender.send(message, replyHandler);
    }
}


