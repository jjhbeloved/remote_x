package cd.blog.humbird.vertx.hm.cmd;

import cd.blog.humbird.vertx.hm.beans.Config;
import cd.blog.humbird.vertx.hm.commons.CmdId;
import cd.blog.humbird.vertx.hm.commons.Constant;
import cd.blog.humbird.vertx.hm.commons.EventAddress;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/17.
 */
public class CommandPingPong extends BaseCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandPingPong.class);


    public CommandPingPong(CmdId cid, int cmdLength) {
        super(cid, cmdLength);
    }

    @Override
    public void process(Vertx vertx, Config config) {
        String channelId = getKeyId();
        // PING PONG
        switch (cmdId) {
            case PING:
                String number = (String) vertx.sharedData().getLocalMap("channelIds").get(channelId);
                if (number == null) {
                    LOGGER.warn(" ping not found channel in map, or not equals clientChannelId, channelId={}, not send the resp", channelId);
                    return;
                }
                LOGGER.debug("receive ping for channelId: {}, and send pong", channelId);

                CmdId cmId = CmdId.PONG;
                Buffer buf = Buffer.buffer();
                buf.appendInt(cmId.getId());
                buf.appendInt(Constant.commandHeadWithIdLength);
                buf.appendString(channelId);
                vertx.eventBus().send(EventAddress.command(channelId), buf);
                break;
            default:
                LOGGER.debug("receive pong for channelId: {}", channelId);
                vertx.eventBus().send(EventAddress.ping(channelId), channelId);
                break;
        }
    }
}
