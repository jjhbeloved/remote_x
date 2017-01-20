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
public class BaseCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCommand.class);

    protected CmdId cmdId;

    protected int commandLength;

    protected Buffer dataBuf;

    protected String channelId;

    public BaseCommand(CmdId cmdId, int commandLength) {
        this.cmdId = cmdId;
        this.commandLength = commandLength;
    }

    public void setDataBuf(Buffer dataBuf) {
        this.dataBuf = dataBuf;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public int getCommandLength() {
        return commandLength;
    }

    protected String getKeyId() {
        return dataBuf.getString(Constant.commandHeadLength, Constant.commandHeadWithIdLength);
    }

    public void process(Vertx vertx, Config config) {
        String connectId = getKeyId();
        switch (cmdId) {
            case CLOSE:
                vertx.eventBus().send(EventAddress.closeAddr(connectId), "close by channel command");
                break;
            case NEW_CONNECT:
                LOGGER.error("try to create new connectId:{}, this should proccessed by newconnectCommand", connectId);
                break;
            case DATA:
                if (vertx.sharedData().getLocalMap("activeConnectMap").get(connectId) != null) {
                    vertx.eventBus().send(EventAddress.recAddr(connectId), dataBuf.slice(Constant.commandHeadWithIdLength, dataBuf.length()));
                } else {
                    LOGGER.warn("The connectId {} not active, try send close command to channel {}", connectId);
                    CmdId cmdId = CmdId.CLOSE;
                    Buffer buf = Buffer.buffer();
                    buf.appendInt(cmdId.getId());
                    buf.appendInt(Constant.commandHeadWithIdLength);
                    buf.appendString(connectId);
                    vertx.eventBus().send(EventAddress.command(this.channelId), buf);
                }
                break;
            default:
                break;
        }
    }
}
