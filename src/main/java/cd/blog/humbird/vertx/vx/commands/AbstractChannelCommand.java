package cd.blog.humbird.vertx.vx.commands;

import cd.blog.humbird.vertx.vx.VxConfig;
import cd.blog.humbird.vertx.vx.common.CommandId;
import cd.blog.humbird.vertx.vx.common.Constants;
import cd.blog.humbird.vertx.vx.sender.AbstractSender;
import cd.blog.humbird.vertx.vx.sender.ChannelDataSender;
import cd.blog.humbird.vertx.vx.sender.CloseConnectSender;
import cd.blog.humbird.vertx.vx.sender.ConnectReceiveAddrSender;
import cd.blog.humbird.vertx.vx.utils.VxUtils;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/6.
 */
public class AbstractChannelCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractChannelCommand.class);

    protected CommandId commandId;
    protected int commandLength;
    protected Buffer dataBuf;
    protected String channelId;

    public AbstractChannelCommand(int commandId, int commandLength) {
        this.commandId = CommandId.valueOf(commandId);
        this.commandLength = commandLength;
    }

    /**
     * 获取 channelId
     *
     * @return
     */
    protected String getKeyId() {
        return dataBuf.getString(Constants.commandHeadLength, Constants.commandHeadWithIdLength);
    }

    public void process(Vertx vertx, VxConfig config) {
        String connectId = getKeyId();
        AbstractSender sender;
        switch (commandId) {
            case CLOSE:
                sender = new CloseConnectSender(vertx, connectId);
                sender.send(Buffer.buffer("close by channel command"));
                break;
            case NEW_CONNECT:
                LOGGER.error("try to create new connectId:{}, this should proccessed by newconnectCommand", connectId);
                break;
            case DATA:
                // 如果连接没有建立, 则不发送信息
                // 这里是转发代理数据
                if (VxUtils.getActiveConnectMap(vertx).get(connectId) != null) {
                    sender = new ConnectReceiveAddrSender(vertx, connectId);
                    sender.send(dataBuf.slice(Constants.commandHeadWithIdLength, dataBuf.length()));
                } else {
                    LOGGER.warn("The connectId {} not active, try send close command to channel {}", connectId);
                    Buffer buf = Buffer.buffer();
                    buf.appendInt(CommandId.CLOSE.getId());
                    buf.appendInt(Constants.commandHeadWithIdLength);
                    buf.appendString(connectId);
                    sender = new ChannelDataSender(vertx, connectId);
                    sender.send(buf);
                }
                break;
            default:
                break;
        }
    }

    public CommandId getCommandId() {
        return commandId;
    }

    public void setCommandId(CommandId commandId) {
        this.commandId = commandId;
    }

    public int getCommandLength() {
        return commandLength;
    }

    public void setCommandLength(int commandLength) {
        this.commandLength = commandLength;
    }

    public Buffer getDataBuf() {
        return dataBuf;
    }

    public void setDataBuf(Buffer dataBuf) {
        this.dataBuf = dataBuf;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
