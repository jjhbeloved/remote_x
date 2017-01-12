package cd.blog.humbird.vertx.vx.commands;

import cd.blog.humbird.vertx.vx.VxConfig;
import cd.blog.humbird.vertx.vx.sender.ConnectNewAddrSender;
import io.vertx.core.Vertx;

public class ChannelCommandNewConnect extends AbstractChannelCommand {

    public ChannelCommandNewConnect(int commandId, int commandLength) {
        super(commandId, commandLength);
    }

    @Override
    public void process(Vertx vertx, VxConfig config) {
        //发送请求给ProxyServer即可
        new ConnectNewAddrSender(vertx).send(dataBuf);
    }
}
