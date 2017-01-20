package cd.blog.humbird.vertx.hm.beans;

import cd.blog.humbird.vertx.hm.Pong;
import cd.blog.humbird.vertx.hm.cmd.BaseCommand;
import cd.blog.humbird.vertx.hm.cmd.CommandFactory;
import cd.blog.humbird.vertx.hm.commons.EventAddress;
import cd.blog.humbird.vertx.hm.services.ChannelPing;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/16.
 */
public class ChannelState {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelState.class);

    private Vertx vertx;

    private NetSocket sock;

    private Config config;

    private String channelId;

    private String number;

    private ChannelClientInfo clientInfo;

    private SocketAddress remoteAddress;

    private SocketAddress localAddress;

    private boolean init = false;

    private BaseCommand lastCommand;

    private Buffer dataBuf = Buffer.buffer();

    private ChannelPing ping;

    private MessageConsumer closeConsumer;

    private MessageConsumer<Buffer> commandParserConsumer;

    public ChannelState(Vertx vertx, NetSocket sock, Config config) {
        this.vertx = vertx;
        this.sock = sock;
        this.config = config;
        this.remoteAddress = sock.remoteAddress();
        this.localAddress = sock.localAddress();
        LOGGER.debug("ping receive request from {}:{}", remoteAddress.host(), remoteAddress.port());
    }

    public Vertx getVertx() {
        return vertx;
    }

    public NetSocket getSock() {
        return sock;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public ChannelClientInfo getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(ChannelClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public void installPing() {
        if (this.ping == null) {
            this.ping = new ChannelPing(this.vertx, this.config, this.channelId, this.number);
            this.ping.start();
        }
    }

    /**
     * 解析事务命令
     * 由事务触发
     */
    public void installCommandParser() {
        // 处理发送到channel队列中的消息，写入到sock中
        this.commandParserConsumer = vertx.eventBus().consumer(EventAddress.command(this.channelId));
        this.commandParserConsumer.handler(msg -> {
            Buffer msgBuf = msg.body();
//            LOGGER.debug("command parser receive data -> {}", msgBuf);
            this.sock.write(msgBuf);
            if (this.sock.writeQueueFull()) {
                LOGGER.warn("channelId {} sockWriteQueue is full,try to pause the sendDataConsumer", this.channelId);
                commandParserConsumer.pause();
                this.sock.drainHandler(v -> {
                    LOGGER.warn("channelId {} sock drainHandler try to resume the sendDataConsumer", this.channelId);
                    commandParserConsumer.resume();
                    this.sock.drainHandler(null);
                });
            }
            this.reset();
        });
    }

    public void installClosePing() {
        if (this.closeConsumer == null) {
            this.closeConsumer = this.vertx.eventBus().consumer(EventAddress.close(this.channelId), p -> {
                LOGGER.info("receive close channel msg, try to close it: {}, number: {},msg: {}"
                        , this.channelId
                        , this.number
                        , p.body());
                this.closePing();
                this.sock.close();
            });
        }
    }

    public void installClosePong(Pong pong) {
        if (this.closeConsumer == null) {
            this.closeConsumer = this.vertx.eventBus().consumer(EventAddress.close(this.channelId), p -> {
                LOGGER.info("receive close channel msg, try to close it: {}, number: {},msg: {}"
                        , this.channelId
                        , this.number
                        , p.body());
                this.closePong(pong);
                this.sock.close();
            });
        }
    }

    public void reset() {
        if (ping != null) {
            ping.resetPeriodicId();
        }
    }

    /**
     * TODO
     */
    private void resolveCommand() {
        int commandLength = 8;
        if (lastCommand == null && dataBuf.length() > 8) {
            int commandId = dataBuf.getInt(0);
            commandLength = dataBuf.getInt(4);
            lastCommand = CommandFactory.newCommand(commandId, commandLength);
            lastCommand.setChannelId(channelId);
        } else if (lastCommand != null) {
            commandLength = lastCommand.getCommandLength();
        }
        if (dataBuf.length() >= commandLength) {
            Buffer commandDataBuf = dataBuf.slice(0, commandLength);
            // 如果带有命令外的 数据, 会重复执行
            if (dataBuf.length() > commandLength) {
                Buffer leftBuf = dataBuf.slice(commandLength, dataBuf.length());
                dataBuf = Buffer.buffer();
                dataBuf.appendBuffer(leftBuf);
            } else {
                dataBuf = Buffer.buffer();
            }

            // 每次执行完命令, 置空上一次命令
            if (lastCommand != null) {
                lastCommand.setDataBuf(commandDataBuf);
                lastCommand.process(vertx, config);
                lastCommand = null;
            }
            resolveCommand();
        }
    }

    public void appendBuffer(Buffer buffer) {
        dataBuf.appendBuffer(buffer);
        resolveCommand();
    }

    public void closeChannelPing() {
        if (ping != null) {
            ping.stop();
            ping = null;
        }
    }

    public void closeChannelPong() {
        if (ping != null) {
            ping.stop();
            ping = null;
        }
    }

    public void closeCommandParser() {
        if(commandParserConsumer!=null){
            commandParserConsumer.unregister();
            commandParserConsumer = null;
        }
    }

    public void closeClose() {
        if (this.closeConsumer != null) {
            this.closeConsumer.unregister();
            this.closeConsumer = null;
        }
    }


    /**
     * 关闭 ping server
     * <p>
     * 发送 关闭公网连接
     */
    public void closePing() {
        if (!this.init) {
            return;
        }
        if (this.channelId != null) {
            LOGGER.warn("the channel sock closed channelId: {}, number: {}", this.channelId, this.number);
            this.vertx.sharedData().getLocalMap("channelIds").remove(this.channelId);
            this.vertx.eventBus().publish(EventAddress.closePublic(this.channelId), this.channelId);
            closeChannelPing();
            closeClose();
            closeCommandParser();
            this.init = false;
            this.channelId = null;
        }
    }

    public void closePong(Pong pong) {
        if (!this.init) {
            return;
        }
        LOGGER.warn("the channel sock closed channelId: {}, number: {}", channelId, number);
        this.vertx.eventBus().publish(EventAddress.closePublic(this.channelId), this.channelId);
        closeChannelPong();
        closeClose();
        closeCommandParser();
        this.init = false;
        //发送channel连接关闭通知
        vertx.eventBus().publish(EventAddress.closeAddr(channelId), channelId);
        vertx.setTimer(1000, timeId -> {
            pong.connect();
        });
    }
}
