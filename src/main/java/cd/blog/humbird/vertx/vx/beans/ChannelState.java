package cd.blog.humbird.vertx.vx.beans;

import cd.blog.humbird.vertx.vx.VxChannelPing;
import cd.blog.humbird.vertx.vx.VxConfig;
import cd.blog.humbird.vertx.vx.VxSock;
import cd.blog.humbird.vertx.vx.commands.AbstractChannelCommand;
import cd.blog.humbird.vertx.vx.commands.ChannelCommandFactory;
import cd.blog.humbird.vertx.vx.common.CommandId;
import cd.blog.humbird.vertx.vx.common.Constants;
import cd.blog.humbird.vertx.vx.consumer.AbstractConsumer;
import cd.blog.humbird.vertx.vx.consumer.ChannelClientInfoConsumer;
import cd.blog.humbird.vertx.vx.consumer.ChannelDataConsumer;
import cd.blog.humbird.vertx.vx.consumer.CloseChannelConsumer;
import cd.blog.humbird.vertx.vx.publish.ChannelClosedAddrPublisher;
import cd.blog.humbird.vertx.vx.sender.AbstractSender;
import cd.blog.humbird.vertx.vx.sender.ChannelDataSender;
import cd.blog.humbird.vertx.vx.utils.VxUtils;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by david on 17/1/5.
 */
public class ChannelState {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelState.class);

    private Buffer dataBuf = Buffer.buffer();

    private Vertx vertx;
    private VxConfig vxConfig;

    private AbstractConsumer<Object> channelDataConsumer;
    private AbstractConsumer<Object> closeChannelConsumer;
    private AbstractSender channelDataSender;
    private VxChannelPing pinger;

    // 每个信道的 id
    private String channelId;
    // 每个信道的 子id
    private String channelShortId;

    private String remoteIp;
    private int remotePort;
    // 统计传输的字长
    private long sendCount;
    // 统计接受的字长
    private long recvCount;
    private long pingTimeId = -1;


    private boolean inited = false;
    private boolean paused = false;
    private boolean closed = false;

    private Date initDate = new Date();
    private Date closeDate;

    private VxSock vxSock;
    private ChannelClientInfo channelClientInfo;
    private AbstractChannelCommand lastCommand;

    public ChannelState(Vertx vertx, VxConfig vxConfig, VxSock vxSock) {
        this.vertx = vertx;
        this.vxSock = vxSock;
        this.vxConfig = vxConfig;
    }

    public ChannelState(Vertx vertx, VxConfig vxConfig, VxSock vxSock, String channelId) {
        this.vertx = vertx;
        this.vxSock = vxSock;
        this.vxConfig = vxConfig;
        this.channelId = channelId;
        init();
    }

    public boolean init() {
        if (!inited) {
            this.channelDataConsumer = new ChannelDataConsumer(this.vertx, channelId);
            // 接受关闭命令
            this.closeChannelConsumer = new CloseChannelConsumer(vertx, channelId, new Handler<Message>() {
                @Override
                public void handle(Message event) {
                    LOGGER.info("receive close channel msg, try to close it: {}, shortId:{}, msg:{}"
                            , channelId
                            , channelShortId
                            , event.body());
                    doChannelServerClose();
                    vxSock.close();
                }
            });
            this.channelDataSender = new ChannelDataSender(this.vertx, channelId);
            inited = true;
        }
        return inited;
    }

    public static void main(String[] args) {
        Buffer buffer = Buffer.buffer();
        buffer.appendBuffer(Buffer.buffer("123"));
        LOGGER.info(buffer.toString());
    }

    /**
     * 删除模块
     */
    public void removeChannelDataConsumer() {
        channelDataConsumer.remove();
    }

    public void removeCloseMsgConsumer() {
        closeChannelConsumer.remove();
    }

    public void removePinger() {
        if (pinger != null) {
            pinger.stop();
            pinger = null;
        }
    }

    public void pausePinger() {
        if (pinger != null) {
            pinger.pause();
        }
    }

    /**
     * 关闭服务端 sock 信道
     */
    public void doChannelServerClose() {
        if (closed) {
            return;
        }
        this.setClosed(true);
        // 移除connectId
        if (channelId != null) {
            LocalMap<String, String> channelMap = VxUtils.getChannelIdsMap(vertx);
            channelMap.remove(channelId);
            LocalMap<String, Boolean> channelPausedMap = VxUtils.getChannelPauseIdsMap(vertx);
            channelPausedMap.remove(channelId);

            // 发送channel连接关闭通知
            new ChannelClosedAddrPublisher(vertx, channelId).publish(Buffer.buffer(channelId));
            this.removePinger();
            this.removeChannelDataConsumer();
            this.removeCloseMsgConsumer();

            channelDataConsumer = null;
            closeChannelConsumer = null;
            dataBuf = null;
            vertx = null;
            vxSock = null;
            vxConfig = null;
//            pinger = null;
            lastCommand = null;
        }
    }

    /**
     * 这里处理具体命令数据
     *
     * 将命令内容 回写给调用者
     */
    public void intallChannelDataConsumer() {
        channelDataConsumer.consume(buffer -> {
            Buffer msgBuf = (Buffer) (buffer.body());
            this.setSendCount(this.getSendCount() + msgBuf.length());
//            LOGGER.info(msgBuf.getInt(0) + ":" + msgBuf.getInt(4) + ":" + msgBuf.getString(8, msgBuf.length()));
            vxSock.write(msgBuf);
            // 此步骤解决 读取速度 快过写入速度 造成Full问题
            if (vxSock.writeQueueFull()) {
                LOGGER.warn("channelId {} sockWriteQueue is full,try to pause the sendDataConsumer", channelId);
                channelDataConsumer.pause();
                vxSock.drainHandler(v -> {
                    LOGGER.warn("channelId {} sock drainHandler try to resume the sendDataConsumer", channelId);
                    channelDataConsumer.resume();
                    vxSock.drainHandler(null);
                });
            }
        });
    }

    public void installPinger() {
        pinger = new VxChannelPing(vertx, vxConfig);
        pinger.setChannelId(channelId);
        pinger.start();
    }

    /**
     * 向 channelId 传输 读取客户端信息 命令
     */
    private void sendLoadClientInfoCommand() {
        CommandId cid = CommandId.CHANNEL_INFO;
        Buffer buf = Buffer.buffer();
        buf.appendInt(cid.getId());
        buf.appendInt(Constants.commandHeadWithIdLength);
        buf.appendString(channelId);
        channelDataSender.send(buf);
    }

    /**
     * 加载 客户端信息
     */
    public void loadClientInfo() {
        AbstractConsumer<Buffer> channelClientInfoConsumer = new ChannelClientInfoConsumer(this.vertx, channelId);
        long timeoutId = vertx.setTimer(vxConfig.getRemoteQueryTimeout(), tid -> {
            LOGGER.warn("loadChannel client info exceed more than {}, try to reload,channelId:{}", vxConfig.getRemoteQueryTimeout(), channelId);
            loadClientInfo();
            channelClientInfoConsumer.unregister();
        });
        channelClientInfoConsumer.consume(msg -> {
            vertx.cancelTimer(timeoutId);
            Buffer msgBuf = msg.body();
            ChannelClientInfo info = VxUtils.b2o(msgBuf.getBytes());
            channelClientInfo = info;
            LOGGER.info("success load channelClientInfo: {}", Json.encode(info));
            channelClientInfoConsumer.unregister();
        });

        // 传输 客户端信息
        sendLoadClientInfoCommand();
        LOGGER.info("send loadChannelInfo success,channelId: {}", channelId);
    }

    /**
     * 解析传递需要执行的命令
     * <p>
     * 命令构成为 4个byte的整形命令编码, 4个byte的整形命令长度, 具体命令
     */
    private void resolveCommand() {
        int commandLength = 8;
        if (lastCommand == null && dataBuf.length() > 8) {
            int commandId = dataBuf.getInt(0);
            commandLength = dataBuf.getInt(4);
            // 这里是获取真实的命令 数据解析器
            lastCommand = ChannelCommandFactory.newCommand(commandId, commandLength);
            lastCommand.setChannelId(channelId);
        } else if (lastCommand != null) {
            commandLength = lastCommand.getCommandLength();
        }

        if (dataBuf.length() >= commandLength) {
            Buffer commandDataBuf = dataBuf.slice(0, commandLength);
            if (dataBuf.length() > commandLength) {
                Buffer leftBuf = dataBuf.slice(commandLength, dataBuf.length());
                dataBuf = Buffer.buffer();
                dataBuf.appendBuffer(leftBuf);
            } else {
                dataBuf = Buffer.buffer();
            }
            // 触发远程 执行命令
            if (lastCommand != null) {
                lastCommand.setDataBuf(commandDataBuf);
                lastCommand.process(vertx, vxConfig);
                lastCommand = null;
            }
            // 有可能一次性 触发多条串行命令
            resolveCommand();
        }
    }

    public void appendBuffer(Buffer buf) {
        dataBuf.appendBuffer(buf);
        resolveCommand();
    }

    public VxConfig getVxConfig() {
        return vxConfig;
    }

    public void setVxConfig(VxConfig vxConfig) {
        this.vxConfig = vxConfig;
    }

    public AbstractConsumer getChannelDataConsumer() {
        return channelDataConsumer;
    }


    public AbstractConsumer getCloseChannelConsumer() {
        return closeChannelConsumer;
    }

    public AbstractSender getChannelDataSender() {
        return channelDataSender;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelShortId() {
        return channelShortId;
    }

    public void setChannelShortId(String channelShortId) {
        this.channelShortId = channelShortId;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
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

    public long getPingTimeId() {
        return pingTimeId;
    }

    public void setPingTimeId(long pingTimeId) {
        this.pingTimeId = pingTimeId;
    }

    public boolean isInited() {
        return inited;
    }

    public void setInited(boolean inited) {
        this.inited = inited;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
        this.closeDate = new Date();
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public void setChannelClientInfo(ChannelClientInfo channelClientInfo) {
        this.channelClientInfo = channelClientInfo;
    }

    public ChannelClientInfo getChannelClientInfo() {
        return channelClientInfo;
    }
}
