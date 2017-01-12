package cd.blog.humbird.vertx.vx.commands;

import cd.blog.humbird.vertx.vx.VxConfig;
import cd.blog.humbird.vertx.vx.common.CommandId;
import cd.blog.humbird.vertx.vx.common.Constants;
import cd.blog.humbird.vertx.vx.sender.ChannelDataSender;
import cd.blog.humbird.vertx.vx.sender.ChannelPingRespSender;
import cd.blog.humbird.vertx.vx.utils.VxUtils;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelCommandChannelPing extends AbstractChannelCommand {
    private static final Logger log = LoggerFactory.getLogger(ChannelCommandChannelPing.class);

    public ChannelCommandChannelPing(int commandId, int commandLength) {
        super(commandId, commandLength);
    }

    public void process(Vertx vertx, VxConfig config) {
        String channelId = getKeyId();
        switch (commandId) {
            case CHANNEL_PING:
                //String clientChannelId = ProxyUtils.getChannelId(vertx);
                String channelShortId = VxUtils.getChannelIdsMap(vertx).get(channelId);
                if (channelShortId == null) {
                    log.warn(" channel ping Not found channel in map"
                            + ", or not equals clientChannelId, channelId={}, not send the resp", channelId);
                    return;
                }
                log.debug("Receive channel ping for channelId: {}, and send resp", channelId);

                CommandId respCid = CommandId.CHANNEL_PING_RESP;
                Buffer buf = Buffer.buffer();
                buf.appendInt(respCid.getId());
                buf.appendInt(Constants.commandHeadWithIdLength);
                buf.appendString(channelId);
                new ChannelDataSender(vertx, channelId).send(buf);
                break;
            default:
                log.debug("Receive channel ping resp for channelId: {}", channelId);
                new ChannelPingRespSender(vertx, channelId).send(Buffer.buffer(channelId));
                break;
        }


    }
}
