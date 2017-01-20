package cd.blog.humbird.vertx.hm;

import cd.blog.humbird.vertx.demo.util.Runner;
import cd.blog.humbird.vertx.hm.beans.ChannelState;
import cd.blog.humbird.vertx.hm.beans.Config;
import cd.blog.humbird.vertx.hm.commons.EventAddress;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by david on 17/1/16.
 */
public class Ping extends BaseVertx {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ping.class);

    private Map<String, ChannelState> cState = new HashMap();

    private Map<String, Integer> cCount = new HashMap<String, Integer>();

    private AtomicInteger numbers = new AtomicInteger(0);

    public static void main(String[] args) {
        Runner.runExample(Ping.class);
    }

    /**
     * 这里有很经典的 循环递增
     *
     * @param channelMap
     * @param domainName
     * @return
     */
    private String chooseChannelId(LocalMap<String, String> channelMap, String domainName) {
        String[] c = channelMap.keySet().toArray(new String[0]);
        if (c == null || c.length == 0) {
            return null;
        }
        int count = -1;
        String channelId = null;
        for (String id : c) {
            ChannelState state = cState.get(id);
            if (domainName != null && state != null && state.getClientInfo() != null
                    && !domainName.equals(state.getClientInfo().getDomainName())) {
                continue;
            }
            if (cCount.containsKey(id)) {
                if (count == -1 || cCount.get(id) < count) {
                    count = cCount.get(id);
                    channelId = id;
                }
            } else {
                cCount.put(id, 0);
                channelId = id;
                break;
            }
        }
        if (channelId != null) {
            cCount.put(channelId, cCount.get(channelId) + 1);
        } else {
            return null;
        }
        //返回channelId:shortId
        return channelId + ":" + channelMap.get(channelId);
    }

    private void removeChannelId(String channelId) {
        Integer count = cCount.get(channelId);
        if (count != null && count > 0) {
            cCount.put(channelId, count - 1);
        } else if (count != null && count == 0) {
            cCount.remove(channelId);
        }
    }

    private void installAddChooseChannelConsumer() {
        vertx.eventBus().consumer(EventAddress.chooseAddr(), msg -> {
            String domainName = (String) msg.body();
            LocalMap<String, String> channelMap = vertx.sharedData().getLocalMap("channelIds");
            String channelId = chooseChannelId(channelMap, domainName);
            msg.reply(channelId);
        });
    }

    private void installRemoveChooseChannelConsumer() {
        vertx.eventBus().consumer(EventAddress.removeAddr(), msg -> {
            String channelId = (String) msg.body();
            removeChannelId(channelId);
        });
    }

    @Override
    public void start(Config config) {

        installAddChooseChannelConsumer();
        installRemoveChooseChannelConsumer();

        NetServerOptions options = new NetServerOptions();
        options.setTcpKeepAlive(true);
        vertx.createNetServer(options)
                .connectHandler(p -> {
                    doHandler(p);
                })
                .listen(config.getPingPort(), config.getPingIp());
        vertx.deployVerticle(new ProxyClient());
    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("close ping");
    }

    private void doHandler(NetSocket netSock) {
        LocalMap<String, String> channelIds = vertx.sharedData().getLocalMap("channelIds");
        ChannelState channelState = new ChannelState(vertx, netSock, config);
        netSock.handler(buffer -> {
            if (!channelState.isInit()) {
                // 解析connectId
                // channelId:60bac594-2c2f-4af8-ad95-cee2a2d4825c
                if (buffer.length() < 46) {
                    netSock.close();
                    LOGGER.error("ping init command error, buffer length is less than 46");
                    return;
                }
                String channelId = buffer.getString(0, 46, "UTF-8");
                if (!channelId.startsWith("channelId:")) {
                    netSock.close();
                    LOGGER.error("ping init command error, not start with 'channelId:'");
                    return;
                }
                channelId = channelId.substring(10);
                cState.put(channelId, channelState);
                channelState.setChannelId(channelId);
                channelState.setNumber("" + numbers.getAndIncrement());
                channelIds.put(channelState.getChannelId(), channelState.getNumber());
                LOGGER.info("channelId create successfully, channelId: {}, number: {} ", channelState.getChannelId(), channelState.getNumber());
                channelState.setInit(true);
                // 由 触发 第一次 ping, 开始 ping-pong 事务
                channelState.installPing();
                channelState.installCommandParser();
                channelState.installClosePing();
                // 发送channel连接上来通知
                vertx.eventBus().publish(EventAddress.connectedAddr(channelId), channelId);
            } else {
                channelState.appendBuffer(buffer);
                channelState.reset();
            }
        });
        netSock.closeHandler(p -> {
            channelState.closePing();
        });
    }
}
