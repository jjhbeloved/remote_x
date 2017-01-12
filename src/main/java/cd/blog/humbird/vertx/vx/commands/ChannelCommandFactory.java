package cd.blog.humbird.vertx.vx.commands;

import cd.blog.humbird.vertx.vx.common.CommandId;

/**
 * Created by david on 17/1/7.
 */
public class ChannelCommandFactory {

    public static AbstractChannelCommand newCommand(int commandId, int commandLength) {
        CommandId cid = CommandId.valueOf(commandId);
        if (cid == null) {
            return null;
        }
        switch (cid) {
            case NEW_CONNECT:
                return new ChannelCommandNewConnect(commandId, commandLength);
//            case PING:
//                return new ChannelCommandPing(commandId, commandLength);
//            case SEND_FEEDBACK:
//                return new ChannelCommandFeedback(commandId, commandLength);
            case CONNECT_PING:
            case CONNECT_PING_RESP:
                return new ChannelCommandConnectPing(commandId, commandLength);
//            case REMOTE_CONNECT_REQ:
//                return new ChannelCommandRemoteConnectReq(commandId, commandLength);
//            case REMOTE_CONNECT_RESP:
//                return new ChannelCommandRemoteConnectResp(commandId, commandLength);
//            case REMOTE_CONNECT_CLOSE:
//                return new ChannelCommandRemoteConnectClose(commandId, commandLength);
            case CHANNEL_PING:
            case CHANNEL_PING_RESP:
                return new ChannelCommandChannelPing(commandId, commandLength);
            case CHANNEL_INFO:
            case CHANNEL_INFO_RESP:
                return new ChannelCommandChannelInfo(commandId, commandLength);
//            case NEW_DIRECT_CONNECT:
            case PROXY_CONNECTED:
                return new ChannelCommandDirectConnect(commandId, commandLength);
            default:
                return new AbstractChannelCommand(commandId, commandLength);
        }
    }
}
