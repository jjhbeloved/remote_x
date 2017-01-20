package cd.blog.humbird.vertx.hm.cmd;

import cd.blog.humbird.vertx.hm.beans.Config;
import cd.blog.humbird.vertx.hm.commons.CmdId;
import cd.blog.humbird.vertx.hm.commons.EventAddress;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/17.
 */
public class CommandConnectPingPong extends BaseCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandConnectPingPong.class);


    public CommandConnectPingPong(CmdId cid, int cmdLength) {
        super(cid, cmdLength);
    }

    @Override
    public void process(Vertx vertx, Config config) {
        String connectId = getKeyId();
        // PING PONG
        switch (cmdId) {
            case CONNECT_PING:
                LOGGER.debug("receive connectPing for connectId: {}, and send connectPong", connectId);
                vertx.eventBus().send(EventAddress.pongAddr(connectId), connectId);
                break;
            default:
                LOGGER.debug("receive pong for channelId: {}", connectId);
                vertx.eventBus().send(EventAddress.pingAddr(connectId), connectId);
                break;
        }
    }
}
