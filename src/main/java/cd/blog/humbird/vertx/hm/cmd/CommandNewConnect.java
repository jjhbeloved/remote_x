package cd.blog.humbird.vertx.hm.cmd;

import cd.blog.humbird.vertx.hm.beans.Config;
import cd.blog.humbird.vertx.hm.commons.CmdId;
import cd.blog.humbird.vertx.hm.commons.EventAddress;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/19.
 */
public class CommandNewConnect extends BaseCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandPingPong.class);


    public CommandNewConnect(CmdId cid, int cmdLength) {
        super(cid, cmdLength);
    }

    @Override
    public void process(Vertx vertx, Config config) {
        //发送请求给 proxy server即可
        vertx.eventBus().send(EventAddress.newAddr(), dataBuf);
    }
}
