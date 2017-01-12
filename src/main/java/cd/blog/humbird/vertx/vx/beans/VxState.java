package cd.blog.humbird.vertx.vx.beans;

import cd.blog.humbird.vertx.vx.VxConfig;
import cd.blog.humbird.vertx.vx.VxConnectPing;
import cd.blog.humbird.vertx.vx.common.CommandId;
import cd.blog.humbird.vertx.vx.common.Constants;
import cd.blog.humbird.vertx.vx.consumer.AbstractConsumer;
import cd.blog.humbird.vertx.vx.consumer.ConnectReceiveAddrConsumer;
import cd.blog.humbird.vertx.vx.sender.ChannelDataSender;
import cd.blog.humbird.vertx.vx.utils.VxUtils;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 17/1/6.
 */
public class VxState implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxState.class);
    /**
     * .
     */
    private static final long serialVersionUID = 1L;
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
    private String proxyType;
    private boolean targetIpParsed = false;
    private String targetIpPort;
    private boolean closed = false;
    private transient Map<Long, Long> sendFeedbackTimeIds = new HashMap<Long, Long>();
    private transient Map<Long, MessageConsumer<Object>> sendFeedbackConsumers = new HashMap<Long, MessageConsumer<Object>>();

    private transient NetSocket sock;
    private transient VxConnectPing pinger;
    private transient ChannelDataSender sender;
    private transient Vertx vertx;
    private transient AbstractConsumer<Buffer> msgConsumer;
    private transient MessageConsumer<Object> connectedConsumer;
    private String targetIp;
    private int targetPort;
    private boolean pass = false;

    public VxState(String connectId, String channelId, Vertx vertx) {
        connected = false;
        initDate = new Date();
        this.connectId = connectId;
        this.channelId = channelId;
        sender = new ChannelDataSender(vertx, channelId);
        this.vertx = vertx;
        msgConsumer = new ConnectReceiveAddrConsumer(vertx, this.connectId);
        VxUtils.getActiveConnectMap(vertx).put(connectId, channelId);
    }

    public void setSock(NetSocket sock) {
        this.sock = sock;
    }

    public NetSocket getSock() {
        return sock;
    }

    /**
     * @return connected属性
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @param connected 设置connected属性
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * @return initDate属性
     */
    public Date getInitDate() {
        return initDate;
    }

    /**
     * @param initDate 设置initDate属性
     */
    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    /**
     * @return connectId属性
     */
    public String getConnectId() {
        return connectId;
    }

    /**
     * @param connectId 设置connectId属性
     */
    public void setConnectId(String connectId) {
        this.connectId = connectId;
    }

    /**
     * @return channelId属性
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * @param channelId 设置channelId属性
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * @return sendCount属性
     */
    public long getSendCount() {
        return sendCount;
    }

    /**
     * @param sendCount 设置sendCount属性
     */
    public void setSendCount(long sendCount) {
        this.sendCount = sendCount;
    }

    /**
     * @return recvCount属性
     */
    public long getRecvCount() {
        return recvCount;
    }

    /**
     * @param recvCount 设置recvCount属性
     */
    public void setRecvCount(long recvCount) {
        this.recvCount = recvCount;
    }

    /**
     * @return clientIp属性
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * @param clientIp 设置clientIp属性
     */
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    /**
     * @return clientPort属性
     */
    public int getClientPort() {
        return clientPort;
    }

    /**
     * @param clientPort 设置clientPort属性
     */
    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }


    /**
     * @return proxyType属性
     */
    public String getProxyType() {
        return proxyType;
    }


    /**
     * @param proxyType 设置proxyType属性
     */
    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }


    /**
     * @return targetIpParsed属性
     */
    public boolean isTargetIpParsed() {
        return targetIpParsed;
    }


    /**
     * @param targetIpParsed 设置targetIpParsed属性
     */
    public void setTargetIpParsed(boolean targetIpParsed) {
        this.targetIpParsed = targetIpParsed;
    }


    /**
     * @return targetIpPort属性
     */
    public String getTargetIpPort() {
        return targetIpPort;
    }


    /**
     * @param targetIpPort 设置targetIpPort属性
     */
    public void setTargetIpPort(String targetIpPort) {
        this.targetIpPort = targetIpPort;
    }

    /**
     * @return closed属性
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @param closed 设置closed属性
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
        VxUtils.getActiveConnectMap(vertx).remove(connectId);
    }

    /**
     * @return connectShortId属性
     */
    public String getConnectShortId() {
        return connectShortId;
    }

    /**
     * @param connectShortId 设置connectShortId属性
     */
    public void setConnectShortId(String connectShortId) {
        this.connectShortId = connectShortId;
    }

    /**
     * @return channelShortId属性
     */
    public String getChannelShortId() {
        return channelShortId;
    }

    /**
     * @param channelShortId 设置channelShortId属性
     */
    public void setChannelShortId(String channelShortId) {
        this.channelShortId = channelShortId;
    }

    /**
     * @return sendFeedbackTimeIds属性
     */
    public Map<Long, Long> getSendFeedbackTimeIds() {
        return sendFeedbackTimeIds;
    }

    /**
     * @param sendFeedbackTimeIds 设置sendFeedbackTimeIds属性
     */
    public void setSendFeedbackTimeIds(Map<Long, Long> sendFeedbackTimeIds) {
        this.sendFeedbackTimeIds = sendFeedbackTimeIds;
    }

    /**
     * @return sendFeedbackConsumers属性
     */
    public Map<Long, MessageConsumer<Object>> getSendFeedbackConsumers() {
        return sendFeedbackConsumers;
    }

    /**
     * @param sendFeedbackConsumers 设置sendFeedbackConsumers属性
     */
    public void setSendFeedbackConsumers(Map<Long, MessageConsumer<Object>> sendFeedbackConsumers) {
        this.sendFeedbackConsumers = sendFeedbackConsumers;
    }

    /**
     * @return lastDataDate属性
     */
    public Date getLastDataDate() {
        return lastDataDate;
    }

    /**
     * @param lastDataDate 设置lastDataDate属性
     */
    public void setLastDataDate(Date lastDataDate) {
        this.lastDataDate = lastDataDate;
    }

    /**
     * @return closeDate属性
     */
    public Date getCloseDate() {
        return closeDate;
    }

    /**
     * @param closeDate 设置closeDate属性
     */
    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }


    public void setConnectedConsumer(MessageConsumer connectedConsumer) {
        this.connectedConsumer = connectedConsumer;
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

    public boolean isPass() {
        return pass;
    }

    public void intallChannelDataConsumer(boolean isServer) {
        msgConsumer.consume(msg -> {
            Buffer buf = msg.body();
            long dataRecvId = buf.getLong(0);
            LOGGER.debug("receive sendId: {}", dataRecvId);
            Buffer leftBuf = buf.slice(Constants.sendIdLength, buf.length());
            if (isServer) {
                if (!this.isTargetIpParsed()) {
                    VxUtils.parseTargetIpPort(leftBuf, this);
                }
                this.setSendCount(this.getSendCount() + leftBuf.length());
            } else {
                this.setRecvCount(this.getRecvCount() + buf.length() - Constants.sendIdLength);
            }
            this.setLastDataDate(new Date());
            // 回写
            LOGGER.debug("receive data: {}, to {}:{}", leftBuf, sock.remoteAddress().host(), sock.remoteAddress().port());
            sock.write(leftBuf);
            if (sock.writeQueueFull()) {
                LOGGER.warn("Proxy sock writeQueue full, try to pause the channel data consumer, connectId: {}", connectId);
                msgConsumer.pause();
                sock.drainHandler(v -> {
                    LOGGER.info("Proxy sock drainHandler, try to resume the channel data consumer, connectId: {}", connectId);
                    msgConsumer.resume();
                    sock.drainHandler(null);
                });
            }
            pausePinger();
        });
    }

    public void installPinger(Vertx vertx, VxConfig config) {
        if (pinger != null) {
            pinger = new VxConnectPing(vertx, config);
            pinger.setChannelId(channelId);
            pinger.setConnectId(connectId);
            pinger.start();
        }
    }

    public void sendProxyConnectedToChannel() {
        Buffer buf = Buffer.buffer();
        buf.appendInt(CommandId.PROXY_CONNECTED.getId());
        buf.appendInt(Constants.commandHeadWithIdLength);
        buf.appendString(connectId);
        sender.send(buf);
    }

    public void sendProxyCloseToChannel() {
        Buffer buf = Buffer.buffer();
        buf.appendInt(CommandId.CLOSE.getId());
        buf.appendInt(Constants.commandHeadWithIdLength);
        buf.appendString(connectId);
        sender.send(buf);

        removeConnectedConsumer();
    }

    public void sendBufferDataToChannel(Buffer buf) {
        sender.send(buf);
    }

    public void sendProxyDataToChannel(Buffer buf, long curSendId) {
        CommandId commandId = CommandId.DATA;
        Buffer channelData = Buffer.buffer();
        channelData.appendInt(commandId.getId());
        channelData.appendInt(Constants.dataCommandHeadLength + buf.length());
        channelData.appendString(connectId);
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

    public void pausePinger() {
        if (pinger != null) {
            pinger.pause();
        }
    }

    public void stopPinger() {
        if (pinger != null) {
            pinger.stop();
            pinger = null;
        }
    }

    public void removeChannelDataConsumer() {
        if (msgConsumer != null) {
            msgConsumer.unregister();
            msgConsumer = null;
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
            this.removeChannelDataConsumer();

            this.stopPinger();
            this.setCloseDate(new Date());
            this.sendProxyCloseToChannel();
            this.setClosed(true);
            this.pass = false;
//            this.removeAllFeedback(vertx);

            LocalMap<String, String> connectMap = VxUtils.getChannelIdsMap(vertx);
            connectMap.remove(this.connectId);
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
