package cd.blog.humbird.vertx.vx;

import cd.blog.humbird.vertx.vx.beans.VxState;
import cd.blog.humbird.vertx.vx.common.CommandId;
import cd.blog.humbird.vertx.vx.common.Constants;
import cd.blog.humbird.vertx.vx.consumer.ChannelCloseAddrConsumer;
import cd.blog.humbird.vertx.vx.consumer.ConnectCloseAddrConsumer;
import cd.blog.humbird.vertx.vx.handlers.ChannelClosedHandler;
import cd.blog.humbird.vertx.vx.sender.ChannelChooseAddrSender;
import cd.blog.humbird.vertx.vx.sender.ChannelReturnAddrSender;
import cd.blog.humbird.vertx.vx.utils.VxUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

/**
 * Created by david on 17/1/9.
 */
public class VxClient extends VxAbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxClient.class);

    private NetServer listenServer = null;

    protected int getListenPort() {
        return config.getProxyClientPort();
    }

    // server 端 不需要 domainName
    protected String getDomainName() {
        return config.getDomainName();
    }
//    protected String getDomainName() {
//        return null;
//    }

    /**
     * @param config
     * @param sock
     * @param channelId
     * @param channelShortId
     * @return 返回是否有对sok调用了pause操作
     */
    protected boolean handelNewConnect(VxConfig config, NetSocket sock, String channelId, String channelShortId) {
        LocalMap<String, String> connectMap = VxUtils.getChannelIdsMap(vertx);
        String connectId = UUID.randomUUID().toString();
        String connectShortId = "" + (VxChannelServer.connectCount.addAndGet(1));
        connectMap.put(connectId, channelId);

        ConnectionBase conBase = (ConnectionBase) sock;
        VxState vxState = new VxState(connectId, channelId, vertx);
        vxState.setSock(sock);
        vxState.setConnectShortId(connectShortId);
        vxState.setChannelShortId(channelShortId);
        vxState.setClientPort(conBase.remoteAddress().port());
        vxState.setClientIp(conBase.remoteAddress().host());

        ChannelCloseAddrConsumer channelCloseAddrConsumer = new ChannelCloseAddrConsumer<>(vertx, channelId);
        channelCloseAddrConsumer.consume(new ChannelClosedHandler(connectId, channelId, sock, vertx, channelCloseAddrConsumer, config));

        LOGGER.info("new connectId: {}, choose the channel: {},clientIp: {}", connectId, channelId, vxState.getClientIp());

        //从sock读取数据后发送个数据到channel
        sock.handler(buf -> {
            vxState.setLastDataDate(new Date());
            System.out.println(buf);
            long curSendId = VxChannelServer.channelSendId.addAndGet(1L);
            if (!vxState.isTargetIpParsed()) {
                VxUtils.parseTargetIpPort(buf, vxState);
            }
            vxState.setSendCount(vxState.getSendCount() + buf.length());
            if (!vxState.isConnected()) {
                if (!vxState.isPass()) {
                    return;
                }
                LOGGER.debug("-->>>>");
                sendNewConnectCommand(config, vxState, buf, curSendId);
            } else {
                if (!vxState.isPass()) {
                    return;
                }
                LOGGER.debug(">>>>-->>>>");
                vxState.sendProxyDataToChannel(buf, curSendId);
            }
            vxState.pausePinger();
        });

        vxState.intallChannelDataConsumer(false);

        ConnectCloseAddrConsumer<Object> connectCloseAddrConsumer = new ConnectCloseAddrConsumer(vertx, connectId);
        connectCloseAddrConsumer.consume(msg -> {
            LOGGER.info("receive close command:" + connectId + ", close by " + msg.body());
            connectCloseAddrConsumer.unregister();
            channelCloseAddrConsumer.unregister();
            vxState.doClose();
            sock.close();
            LOGGER.info("success close socke:" + connectId);
        });


        //关闭连接发送到channel
        sock.closeHandler(v -> {
            LOGGER.info("send close command:" + connectId);
            connectCloseAddrConsumer.unregister();
            channelCloseAddrConsumer.unregister();
            vxState.doClose();
            new ChannelReturnAddrSender<>(vertx).send(vxState.getChannelId());
        });

        return sendInitProxyStateCommand(vxState, sock);
    }

    /**
     * @param vxState
     * @param sock
     * @return 是否对sock做了pause操作
     */
    protected boolean sendInitProxyStateCommand(VxState vxState, NetSocket sock) {
        // 代理不需要发送，直连才需要发送
        return false;
    }

    protected void sendNewConnectCommand(VxConfig config, VxState vxState, Buffer buf, long curSendId) {
        vxState.setConnected(true);
        //发送连接命令
        CommandId newCommandId = CommandId.NEW_CONNECT;
        Buffer newChannelData = Buffer.buffer();
        newChannelData.appendInt(newCommandId.getId());
        newChannelData.appendInt(Constants.dataCommandHeadLength + buf.length());
        newChannelData.appendString(vxState.getConnectId());
        newChannelData.appendLong(curSendId);
        newChannelData.appendBuffer(buf);
        System.out.println("sendNewConnectCommand: " + buf);
        vxState.sendBufferDataToChannel(newChannelData);
        vxState.installPinger(vertx, config);
    }

    @Override
    public void start(VxConfig config) {
        listenServer = vertx.createNetServer().connectHandler(sock -> {
            // 在初始化的时候, 停止接收信息
            sock.pause();
            //发送选择通道命令
            new ChannelChooseAddrSender<>(vertx).send(getDomainName(), res -> {
                if (res.succeeded()) {
                    String channelInfo = (String) res.result().body();
                    if (channelInfo == null) {
                        LOGGER.error("no avariable channel");
                        sock.close();
                        return;
                    }
                    String[] aTmp = channelInfo.split(":");
                    String channelId = aTmp[0];
                    String channelShortId = aTmp[1];

                    if (!handelNewConnect(config, sock, channelId, channelShortId)) {
                        sock.resume();
                    }
                } else {
                    LOGGER.error("choose channel fail");
                    sock.close();
                    return;
                }
            });
        }).listen(getListenPort());
        LOGGER.info("VxClient is now listening on {}", getListenPort());
    }

    @Override
    public void stop() throws Exception {
        if (listenServer != null) {
            listenServer.close();
            listenServer = null;
        }
        super.stop();
    }
}
