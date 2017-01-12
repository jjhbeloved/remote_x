package cd.blog.humbird.vertx.vx.commands;

import cd.blog.humbird.vertx.vx.VxConfig;
import cd.blog.humbird.vertx.vx.sender.DirectConnectNewAddrSender;
import cd.blog.humbird.vertx.vx.sender.DirectConnectedAddrSender;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelCommandDirectConnect extends AbstractChannelCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelCommandDirectConnect.class);

    public ChannelCommandDirectConnect(int commandId, int commandLength) {
        super(commandId, commandLength);
    }

    public void process(Vertx vertx, VxConfig config) {
        String connectId = getKeyId();
        switch (commandId) {
            case NEW_DIRECT_CONNECT:
                new DirectConnectNewAddrSender<>(vertx).send(dataBuf);
                break;
            default:
                new DirectConnectedAddrSender<String>(vertx, connectId).send(connectId);
                break;
        }


    }
}
