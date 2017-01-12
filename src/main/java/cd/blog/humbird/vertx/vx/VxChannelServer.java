package cd.blog.humbird.vertx.vx;

import cd.blog.humbird.vertx.demo.util.Runner;
import cd.blog.humbird.vertx.vx.beans.ChannelState;
import cd.blog.humbird.vertx.vx.beans.ProxyQueryParams;
import cd.blog.humbird.vertx.vx.common.CommandId;
import cd.blog.humbird.vertx.vx.common.Constants;
import cd.blog.humbird.vertx.vx.consumer.*;
import cd.blog.humbird.vertx.vx.sender.AbstractSender;
import cd.blog.humbird.vertx.vx.sender.ChannelDataSender;
import cd.blog.humbird.vertx.vx.utils.DateUtil;
import cd.blog.humbird.vertx.vx.utils.SerializableMessageCode;
import cd.blog.humbird.vertx.vx.utils.VxUtils;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by david on 17/1/5.
 */
public class VxChannelServer extends VxAbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxChannelServer.class);

    private Integer channelShortId = 0;

    public static AtomicLong channelSendId = new AtomicLong(0);
    public static AtomicInteger connectCount = new AtomicInteger(0);


    private Map<String, ChannelState> vState = new HashMap<String, ChannelState>();
    private Map<String, Integer> hChannelCount = new HashMap<String, Integer>();

    private AbstractConsumer<Object> monitorRemoteConnectConsumer;
    private AbstractConsumer<Object> channelChooseAddrConsumer;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        Runner.runExample(VxChannelServer.class);
    }

    @Override
    public void start(VxConfig config) {
        monitorRemoteConnectConsumer = new MonitorRemoteConnectConsumer(vertx);
        channelChooseAddrConsumer = new ChannelChooseAddrConsumer<>(vertx);
//        consumerQueryList();
        installChooseChannelConsumer();
        // 此步骤用来 全局解析 Object, 对应解析为 CODE_NAME 里面的别名
        // 第一个参数是 对应的解析, 第二个是具体的解析方法
        vertx.eventBus().registerDefaultCodec(Serializable.class, new SerializableMessageCode());
        if (config.isWebSock()) {

        } else {
            Handler<NetSocket> handler = p -> {
                handle(new VxSock(p));
            };
            NetServerOptions options = new NetServerOptions();
            options.setTcpKeepAlive(true);
            vertx.createNetServer(options)
                    .connectHandler(handler)
                    .listen(config.getChannelServerPort(), config.getChannelServerHost());
            //使用netsocket的方式才发布ProxyMonitor
//            vertx.deployVerticle(new ProxyMonitor());
        }
        vertx.deployVerticle(new VxClient());
        LOGGER.info("VxChannelserver is now listening on {}, websock:{}"
                , config.getChannelServerPort()
                , config.isWebSock() ? config.getChannelServerUri() : "false");
    }

    private ChannelState[] filterStates(ChannelState[] states, String search) {
        if (search == null || search.trim().length() == 0) {
            return states;
        }
        List<ChannelState> rsList = new ArrayList<ChannelState>();
        for (int i = 0; i < states.length; i++) {
            String str = states[i].toString().toLowerCase();
            if (str.contains(search.toLowerCase())) {
                rsList.add(states[i]);
            }
        }
        return rsList.toArray(new ChannelState[0]);
    }

    private JsonArray sortStates(JsonArray rows, ProxyQueryParams params) {
        List<JsonObject> rsList = new ArrayList<JsonObject>();
        for (int i = 0, len = rows.size(); i < len; i++) {
            JsonObject row = rows.getJsonObject(i);
            String channelId = row.getString("channelId");
            ChannelState state = vState.get(channelId);
            if (state != null) {
                row.put("channelShortId", state.getChannelShortId());
            }
            rsList.add(rows.getJsonObject(i));
        }


        Comparator<JsonObject> c = (o1, o2) -> {
            int rs = 0;
            try {
                String sort = params.getSort();
                String order = params.getOrder();
                if ("connectId".equals(sort) || "channelId".equals(sort)
                        || "targetIpPort".equals(sort) || "startDate".equals(sort)
                        || "lastDataDate".equals(sort)) {
                    rs = o1.getString(sort).compareTo(o2.getString(sort));
                } else if ("sendCount".equals(sort)) {
                    rs = o1.getLong("sendCount").compareTo(o2.getLong("sendCount"));
                } else if ("recvCount".equals(sort)) {
                    rs = o1.getLong("recvCount").compareTo(o2.getLong("recvCount"));
                } else if ("closed".equals(sort)) {
                    rs = o1.getString("closed").compareTo(o2.getString("closed"));
                } else {
                    rs = o1.getString("startDate").compareTo(o2.getString("startDate"));
                }
                if ("desc".equals(order)) {
                    rs = 0 - rs;
                }
            } catch (Exception ex) {
                rs = 0;
            }
            return rs;
        };
        Collections.sort(rsList, c);
        JsonArray rsArr = new JsonArray();
        rsList.forEach(rsArr::add);
        return rsArr;
    }

    private ChannelState[] sortStates(ChannelState[] states, ProxyQueryParams params) {
        List<ChannelState> rsList = new ArrayList<ChannelState>();
        for (ChannelState e : states) {
            rsList.add(e);
        }
        Comparator<ChannelState> c = (o1, o2) -> {
            int rs = 0;
            try {
                String sort = params.getSort();
                String order = params.getOrder();
                //region sort if
                if ("channelId".equals(sort)) {
                    rs = o1.getChannelId().compareTo(o2.getChannelId());
                } else if ("channelShortId".equals(sort)) {
                    rs = new Integer(o1.getChannelShortId())
                            .compareTo(new Integer(o2.getChannelShortId()));
                } else if ("remoteIp".equals(sort)) {
                    rs = o1.getRemoteIp().compareTo(o2.getRemoteIp());
                } else if ("remotePort".equals(sort)) {
                    rs = new Integer(o1.getRemotePort()).compareTo(o2.getRemotePort());
                } else if ("startDate".equals(sort)) {
                    rs = o1.getInitDate().compareTo(o2.getInitDate());
                } else if ("sendCount".equals(sort)) {
                    rs = new Long(o1.getSendCount()).compareTo(o2.getSendCount());
                } else if ("recvCount".equals(sort)) {
                    rs = new Long(o1.getRecvCount()).compareTo(o2.getRecvCount());
                } else if ("closed".equals(sort)) {
                    rs = Boolean.valueOf(o1.isClosed()).compareTo(o2.isClosed());
                } else {
                    rs = o1.getInitDate().compareTo(o2.getInitDate());
                }
                //endregion
                if ("desc".equals(order)) {
                    rs = 0 - rs;
                }
            } catch (Exception ex) {
                rs = 0;
            }
            return rs;
        };
        Collections.sort(rsList, c);
        return rsList.toArray(new ChannelState[0]);
    }

    /**
     * 获取域名相等的 id
     *
     * @param channelMap
     * @return channelId:
     */
    private String chooseChannelId(LocalMap<String, String> channelMap, String domainName) {
        String[] c = channelMap.keySet().toArray(new String[0]);
        LocalMap<String, Boolean> channelPausedMap = VxUtils.getChannelPauseIdsMap(vertx);
        if (c == null || c.length == 0) {
            return null;
        }
        int count = -1;
        String channelId = null;
        for (String id : c) {
            ChannelState state = vState.get(id);
            // 如果域名==null, 或者 client域名存在 并且必须保持和 server端相当
            // 同时 必须有 客户端连上来
            if (domainName != null && state != null && state.getChannelClientInfo() != null
                    && !domainName.equals(state.getChannelClientInfo().getDomainName())) {
                continue;
            }
            if (channelPausedMap != null
                    && channelPausedMap.get(id) != null
                    && channelPausedMap.get(id)) {
                LOGGER.info("ChannelId {} paused, skip!", id);
                continue;
            }
            if (hChannelCount.containsKey(id)) {
                if (count == -1 || hChannelCount.get(id) < count) {
                    count = hChannelCount.get(id);
                    channelId = id;
                }
            } else {
                hChannelCount.put(id, 0);
                channelId = id;
                break;
            }
        }
        if (channelId != null) {
            hChannelCount.put(channelId, hChannelCount.get(channelId) + 1);
        } else {
            return null;
        }
        //返回channelId:shortId
        return channelId + ":" + channelMap.get(channelId);
    }

    private void returnChannelId(String channelId) {
        Integer count = hChannelCount.get(channelId);
        if (count != null && count > 0) {
            hChannelCount.put(channelId, count - 1);
        } else if (count != null && count == 0) {
            hChannelCount.remove(channelId);
        }
    }

    private void installReturnChannelConsumer() {
        new ChannelReturnAddrConsumer<>(vertx).consume(msg -> {
            String channelId = (String) msg.body();
            returnChannelId(channelId);
        });
    }

    /**
     * 选择一个再用的信道
     */
    private void installChooseChannelConsumer() {
        channelChooseAddrConsumer.consume(msg -> {
            String domainName = (String) msg.body();
            LocalMap<String, String> channelMap = VxUtils.getChannelIdsMap(vertx);
            String channelId = chooseChannelId(channelMap, domainName);
            msg.reply(channelId);
        });
        installReturnChannelConsumer();
    }

    private void consumerQueryList() {
        remoteQueryList();
        new MonitorChannelAddrConsumer<>(vertx).consume(msg -> {
            ProxyQueryParams params = (ProxyQueryParams) msg.body();
            LOGGER.info("queryParams:{}", params);
            ChannelState[] states = vState.values().toArray(new ChannelState[0]);
            JsonObject json = new JsonObject();
            json.put("total", states.length);
            ChannelState[] filterRs = filterStates(states, params.getSearch());
            filterRs = sortStates(filterRs, params);
            JsonArray arr = new JsonArray();

            int start = params.getOffset() == -1 ? 0 : params.getOffset();
            int end = params.getLimit() == -1 ? filterRs.length : Math.min(filterRs.length, start + params.getLimit());

            for (int i = start; i < end; i++) {
                ChannelState state = filterRs[i];
                JsonObject row = new JsonObject();
                row.put("channelId", state.getChannelId());
                row.put("channelShortId", state.getChannelShortId());
                row.put("remoteIp", state.getRemoteIp());
                row.put("remotePort", state.getRemotePort());
                row.put("startDate", DateUtil.toStr(state.getInitDate()));
                row.put("sendCount", state.getSendCount());
                row.put("recvCount", state.getRecvCount());
                row.put("paused", state.isPaused());
                row.put("closed", state.isClosed());
                row.put("closeDate", DateUtil.toStr(state.getCloseDate()));
                row.put("clientInfo", VxUtils.toJsonObj(state.getChannelClientInfo()));
                arr.add(row);
            }
            json.put("rows", arr);
            msg.reply(json);
        });
    }

    /**
     * 获取远程监控列表 连接信息
     * 将获取的每一个 channel 远程信息以 REMOTE_CONNECT_REQ 方式发送出去
     */
    public void remoteQueryList() {
        monitorRemoteConnectConsumer.consume(buffer -> {
            ProxyQueryParams params = (ProxyQueryParams) buffer.body();
            byte[] bytes = VxUtils.o2b(params);
            AbstractConsumer monitorRemoteConnectRespConsumer = new MonitorRemoteConnectRespConsumer(vertx);

            CommandId cid = CommandId.REMOTE_CONNECT_REQ;
            Buffer buf = Buffer.buffer();
            buf.appendInt(cid.getId());
            buf.appendInt(Constants.commandHeadLength + bytes.length);
            buf.appendBytes(bytes);
            LOGGER.info("queryParams:{}", params);

            String[] keys = VxUtils.getChannelIdsMap(vertx).keySet().toArray(new String[0]);
            CountDownLatch countDown = new CountDownLatch(keys.length);
            List<Long> timeoutIds = new ArrayList();
            for (String channelId : keys) {
                LOGGER.debug("try send query remote connect req to channelId:{}", channelId);
                AbstractSender sender = new ChannelDataSender(vertx, channelId);
                sender.send(buf);
                timeoutIds.add(vertx.setTimer(config.getRemoteQueryTimeout(), p -> {
                    LOGGER.warn("A remote connect query timeout");
                    countDown.countDown();
                    if (countDown.getCount() == 0) {
                        monitorRemoteConnectRespConsumer.unregister();
                    }
                }));
            }
//            handlerRemoteQueryRespMsg(buffer, params, monitorRemoteConnectRespConsumer, countDown, timeoutIds);
        });
    }

    /**
     * 服务端收到数据请求, 则进行处理
     *
     * @param sock
     */
    private void handle(VxSock sock) {
        ChannelState channelState = new ChannelState(vertx, config, sock);
        channelState.setRemoteIp(sock.remoteAddress().host());
        channelState.setRemotePort(sock.remoteAddress().port());
        LOGGER.info("connected remote ip->{}, port->{}", channelState.getRemoteIp(), channelState.getRemotePort());
        sock.handler(buffer -> {
            channelState.setRecvCount(channelState.getRecvCount() + buffer.length());
            LOGGER.debug("rev count: {}", buffer.length());
            // 初始化信道, 第一次连接要 校验是否符合规格, 再校验命令, 第二次连接则直接校验 命令
            if (!channelState.isInited()) {
                if (buffer.length() < 46) {
                    sock.close();
                    LOGGER.error("intial command error, buf length is less than 46");
                    return;
                }
                // 解析connectId
                // channelId:60bac594-2c2f-4af8-ad95-cee2a2d4825c
                String channelId = buffer.getString(0, 46, "UTF-8");
                if (!channelId.startsWith("channelId:")) {
                    sock.close();
                    LOGGER.error("intial command error, not start with channelId:");
                    return;
                }
                channelId = channelId.substring(10);
                LocalMap<String, String> channelMap = VxUtils.getChannelIdsMap(vertx);
                String sChannelShortId = "" + (++channelShortId);
                channelMap.put(channelId, sChannelShortId);
                channelState.setChannelShortId(sChannelShortId);
                channelState.setChannelId(channelId);
                channelState.init();
                LOGGER.info("the channelId create successfully, channelId: {}, shortId: {} ", channelId, sChannelShortId);
                if (buffer.length() > 46) {
                    LOGGER.warn("commands: {}", buffer.slice(46, buffer.length()).toString());
                    channelState.appendBuffer(buffer.slice(46, buffer.length()));
                }
                channelState.intallChannelDataConsumer();
                channelState.installPinger();
                channelState.loadClientInfo();
                // 发送channel连接上来通知, 这里似乎没啥用
//                new ChannelClosedAddrPublisher<>(vertx, channelId).publish(channelId);
                return;
            }
            channelState.appendBuffer(buffer);
            channelState.pausePinger();
        });

        sock.closeHandler(p -> {
            LOGGER.warn("the channel sock closed: {}, channelShortId: {}", channelState.getChannelId(), channelState.getChannelShortId());
            channelState.doChannelServerClose();
        });
    }
}
