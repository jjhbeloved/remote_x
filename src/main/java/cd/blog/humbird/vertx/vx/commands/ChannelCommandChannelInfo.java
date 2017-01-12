package cd.blog.humbird.vertx.vx.commands;

import cd.blog.humbird.vertx.vx.VxConfig;
import cd.blog.humbird.vertx.vx.beans.ChannelClientInfo;
import cd.blog.humbird.vertx.vx.common.AddressTemplate;
import cd.blog.humbird.vertx.vx.common.CommandId;
import cd.blog.humbird.vertx.vx.common.Constants;
import cd.blog.humbird.vertx.vx.sender.ChannelDataSender;
import cd.blog.humbird.vertx.vx.utils.ChannelClientInfoFactory;
import cd.blog.humbird.vertx.vx.utils.VxUtils;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChannelCommandChannelInfo extends AbstractChannelCommand {
    private static final Logger log = LoggerFactory.getLogger(ChannelCommandChannelInfo.class);

    public ChannelCommandChannelInfo(int commandId, int commandLength) {
        super(commandId, commandLength);
    }

    public void process(Vertx vertx, VxConfig config) {
        String channelId = super.getKeyId();

        switch (commandId) {
            case CHANNEL_INFO:
                log.debug("Receive CHANNEL_INFO  for connectId:{},", channelId);
                CommandId cid = CommandId.CHANNEL_INFO_RESP;
                Buffer buf = Buffer.buffer();
                buf.appendInt(cid.getId());
                buf.appendInt(0);
                buf.appendString(channelId);
                ChannelClientInfo info = ChannelClientInfoFactory.getChannelClientInfo();
                if (config.getChannelClientName() != null) {
                    info.setChannelClientName(config.getChannelClientName());
                } else {
                    info.setChannelClientName(info.getComputerName());
                }
                info.setDomainName(config.getDomainName());
                buf.appendBytes(VxUtils.o2b(info));
                buf.setInt(4, buf.length());
                new ChannelDataSender<>(vertx, channelId).send(buf);
                break;

            default:
                log.trace("Receive CHANNEL_INFO resp  for connectId:{},", channelId);
                vertx.eventBus().send(AddressTemplate.getChannelClientInfoAddr(channelId)
                        , dataBuf.slice(Constants.commandHeadWithIdLength, dataBuf.length()));
                break;
        }


    }
}
