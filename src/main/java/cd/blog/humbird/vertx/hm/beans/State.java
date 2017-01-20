package cd.blog.humbird.vertx.hm.beans;

import cd.blog.humbird.vertx.hm.commons.CmdId;
import cd.blog.humbird.vertx.hm.commons.Constant;
import cd.blog.humbird.vertx.hm.commons.EventAddress;
import cd.blog.humbird.vertx.hm.services.ConnectPing;
import cd.blog.humbird.vertx.hm.utils.HmUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 17/1/17.
 * <p>
 * 此对象 用于 connect
 */
public class State implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(State.class);

    /**
     * 表示是否发送了connect命令
     */
    private boolean connected = false;

    private Date initDate;

    private Date lastDataDate = new Date();

    private Date closeDate;

    private String connectId;

    private String channelId;

    private String connectShortId;

    private String channelShortId;

    private long sendCount = 0L;

    private long recvCount = 0L;

    private String clientIp;

    private int clientPort;

    private Config config;

    private String proxyType;

    private boolean targetIpParsed = false;

    private String targetRemote;

    private String targetIp;

    private int targetPort;

    private boolean closed = false;
    private transient Map<Long, Long> sendFeedbackTimeIds = new HashMap<Long, Long>();
    private transient Map<Long, MessageConsumer<Object>> sendFeedbackConsumers = new HashMap<Long, MessageConsumer<Object>>();

    private transient NetSocket sock;

    private transient ConnectPing ping = null;

    private transient MessageProducer<Object> sender;

    private transient Vertx vertx;

    private transient MessageConsumer<Buffer> dataConsumer;

    private transient MessageConsumer<Object> connectedConsumer;

    private boolean pass = false;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Date getLastDataDate() {
        return lastDataDate;
    }

    public void setLastDataDate(Date lastDataDate) {
        this.lastDataDate = lastDataDate;
    }

    public String getConnectShortId() {
        return connectShortId;
    }

    public void setConnectShortId(String connectShortId) {
        this.connectShortId = connectShortId;
    }

    public String getChannelShortId() {
        return channelShortId;
    }

    public void setChannelShortId(String channelShortId) {
        this.channelShortId = channelShortId;
    }

    public long getSendCount() {
        return sendCount;
    }

    public void setSendCount(long sendCount) {
        this.sendCount = sendCount;
    }

    public long getRecvCount() {
        return recvCount;
    }

    public void setRecvCount(long recvCount) {
        this.recvCount = recvCount;
    }

    public String getConnectId() {
        return connectId;
    }

    public String getChannelId() {
        return channelId;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public boolean isTargetIpParsed() {
        return targetIpParsed;
    }

    public void setTargetIpParsed(boolean targetIpParsed) {
        this.targetIpParsed = targetIpParsed;
    }

    public String getTargetRemote() {
        return targetRemote;
    }

    public void setTargetRemote(String targetRemote) {
        this.targetRemote = targetRemote;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public NetSocket getSock() {
        return sock;
    }

    public void setSock(NetSocket sock) {
        this.sock = sock;
    }

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public State(String connectId, String channelId, Vertx vertx, Config config) {
        initDate = new Date();
        this.connectId = connectId;
        this.channelId = channelId;
        this.sender = vertx.eventBus().sender(EventAddress.command(this.channelId));
        this.vertx = vertx;
        this.config = config;
        this.vertx.sharedData().getLocalMap("activeConnectMap").put(connectId, channelId);
    }

    /**
     * 回写响应
     * ProxyClient 回写给客户端
     * ProxyServer 回写给目标服务器
     *
     * @param isServer proxy Client/Server
     */
    public void installDataConsumer(boolean isServer) {
        dataConsumer = this.vertx.eventBus().consumer(EventAddress.recAddr(connectId));
        dataConsumer.handler(msg -> {
            Buffer buf = msg.body();
            long dataRecvId = buf.getLong(0);
//            LOGGER.debug("receive sendId: {}", dataRecvId);
            Buffer leftBuf = buf.slice(Constant.sendIdLength, buf.length());
            if (isServer) {
                if (!this.targetIpParsed) {
                    HmUtil.parseTargetIpPort(leftBuf, this);
                }
                this.sendCount = this.sendCount + leftBuf.length();
            } else {
                this.recvCount = this.recvCount + buf.length() - Constant.sendIdLength;
            }
            this.lastDataDate = new Date();

            sock.write(leftBuf);
            if (sock.writeQueueFull()) {
                LOGGER.warn("proxy sock writeQueue full, try to pause the channel data consumer, connectId: {}", connectId);
                dataConsumer.pause();
                sock.drainHandler(v -> {
                    LOGGER.info("proxy sock drainHandler, try to resume the channel data consumer, connectId: {}", connectId);
                    dataConsumer.resume();
                    sock.drainHandler(null);
                });
            }
            reset();
        });
    }

    public void installPing() {
        // 这里 的确不会生成 connect 的 ping
        if (ping == null) {
            ping = new ConnectPing(this.vertx, this.config, this.channelId, this.connectId);
            ping.start();
        }
    }

    public void sendDataToChannel(Buffer buf, long curSendId) {
        connected = true;
        //发送连接命令
        CmdId newCommandId = CmdId.NEW_CONNECT;
        Buffer newChannelData = Buffer.buffer();
        newChannelData.appendInt(newCommandId.getId());
        newChannelData.appendInt(Constant.dataCommandHeadLength + buf.length());
        newChannelData.appendString(this.connectId);
        newChannelData.appendLong(curSendId);
        newChannelData.appendBuffer(buf);
        installPing();
        sender.send(newChannelData);
    }

    public void sendProxyDataToChannel(Buffer buf, long curSendId) {
        CmdId cmdId = CmdId.DATA;
        Buffer channelData = Buffer.buffer();
        channelData.appendInt(cmdId.getId());
        channelData.appendInt(Constant.dataCommandHeadLength + buf.length());
        channelData.appendString(this.connectId);
        channelData.appendLong(curSendId);
        channelData.appendBuffer(buf);
        sender.send(channelData);
        if (sender.writeQueueFull()) {
            sock.pause();
            sender.drainHandler(v -> {
                sock.resume();
                sender.drainHandler(null);
            });
        }
    }

    public void reset() {
        if (ping != null) {
            ping.resetPeriodicId();
        }
    }

    /**
     * 关闭远程连接
     */
    public void closeConnect() {
        CmdId cmdId = CmdId.CLOSE;
        Buffer buf = Buffer.buffer();
        buf.appendInt(cmdId.getId());
        buf.appendInt(Constant.commandHeadWithIdLength);
        buf.appendString(this.connectId);
        sender.send(buf);
        removeConnectedConsumer();
    }

    public void closelPing() {
        if (ping != null) {
            ping.stop();
            ping = null;
        }
    }

    public void removeDataConsumer() {
        if (this.dataConsumer != null) {
            dataConsumer.unregister();
            dataConsumer = null;
        }
    }

    public void removeConnectedConsumer() {
        if (connectedConsumer != null) {
            connectedConsumer.unregister();
            connectedConsumer = null;
        }
    }

    public void doClose() {
        if (!closed) {
            this.removeDataConsumer();
            this.closelPing();
            this.closeDate = new Date();
            this.closeConnect();
            this.closed = true;
//            this.removeAllFeedback(vertx);
            vertx.sharedData().getLocalMap("connectIds").remove(this.connectId);
            vertx.sharedData().getLocalMap("activeConnectMap").remove(this.connectId);
            this.sender.close();
            this.sender = null;
            sock = null;
        }
    }

    public boolean validate(String sec) {
        if ("YWRtaW46SlcjRmghazltaGdOc3JQbA==".equals(sec)) {
            pass = true;
        }
        return pass;
    }
}
