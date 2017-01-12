package cd.blog.humbird.vertx.vx.commands;

import cd.blog.humbird.vertx.vx.VxConfig;
import cd.blog.humbird.vertx.vx.sender.ConnectPingRespSender;
import cd.blog.humbird.vertx.vx.sender.ConnectPingSender;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelCommandConnectPing extends AbstractChannelCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelCommandConnectPing.class);

    public ChannelCommandConnectPing(int commandId, int commandLength) {
        super(commandId, commandLength);
    }

    public void process(Vertx vertx, VxConfig config) {
        String connectId = getKeyId();
        switch (commandId) {
            case CONNECT_PING:
                LOGGER.debug("Receive Connect ping for connectId:{}", connectId);
                new ConnectPingSender(vertx, connectId).send(Buffer.buffer(connectId));
                break;
            default:
                LOGGER.debug("Receive connect ping resp for connectId:{}", connectId);
                new ConnectPingRespSender(vertx, connectId).send(Buffer.buffer(connectId));
                break;
        }
    }
}
