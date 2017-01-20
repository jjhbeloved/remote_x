package cd.blog.humbird.vertx.hm.cmd;


import cd.blog.humbird.vertx.hm.commons.CmdId;

/**
 * Created by david on 17/1/17.
 */
public class CommandFactory {

    public static BaseCommand newCommand(int cmdId, int cmdLength) {
        CmdId cid = CmdId.valueOf(cmdId);
        if (cid == null) {
            return null;
        }
        switch (cid) {
            case NEW_CONNECT:
                return new CommandNewConnect(cid, cmdLength);
            case PING:
            case PONG:
                return new CommandPingPong(cid, cmdLength);
            default:
                return new BaseCommand(cid, cmdLength);
        }
    }

}
