package cd.blog.humbird.vertx.vx;

import cd.blog.humbird.vertx.vx.beans.ProxyQueryParams;
import cd.blog.humbird.vertx.vx.beans.VxState;
import cd.blog.humbird.vertx.vx.common.CommandId;
import cd.blog.humbird.vertx.vx.common.Constants;
import cd.blog.humbird.vertx.vx.consumer.*;
import cd.blog.humbird.vertx.vx.sender.ChannelDataSender;
import cd.blog.humbird.vertx.vx.sender.ConnectCloseAddrSender;
import cd.blog.humbird.vertx.vx.utils.DateUtil;
import cd.blog.humbird.vertx.vx.utils.VxUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by david on 17/1/8.
 */
public class VxServer extends VxBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxServer.class);

    private long channelSendId = 0;

    private String channelId;

    private AbstractConsumer<Buffer> monitorRemoteConnectDoAddrConsumer;
    private AbstractConsumer<Buffer> monitorRemoteConnectCloseAddrConsumer;

    public VxServer(String channelId) {
        this.channelId = channelId;
    }

    /**
     * 符合条件的查询过滤
     *
     * @param states
     * @param search
     * @return
     */
    private VxState[] filterStates(VxState[] states, String search) {
        if (search == null || search.trim().length() == 0) {
            return states;
        }
        List<VxState> rsList = new ArrayList();
        for (int i = 0; i < states.length; i++) {
            String str = states[i].toString().toLowerCase();
            if (str.indexOf(search.toLowerCase()) != -1) {
                rsList.add(states[i]);
            }
        }
        return rsList.toArray(new VxState[0]);
    }

    protected void handlerConnectMsg(String host, int port, Buffer leftBuf, VxState vxState) {
        String connectId = vxState.getConnectId();
        LOGGER.info("try to create new connectId: {}, channelId: {} using the proxy {}:{}", connectId, channelId, host, port);
        // 这里安装的是  connect的 ping-pong
        vxState.installPinger(vertx, config);
        vertx.createNetClient().connect(port, host, res -> {
            if (res.failed()) {
                LOGGER.warn("unable to connect to the proxy server {}:{}, cause: {}", host, port, res.cause());
                vxState.setClosed(true);
                vxState.setCloseDate(new Date());
                vxState.stopPinger();
                vxState.sendProxyCloseToChannel();
            } else {
                // 这里可以建立 ssh 连接
                NetSocket sock = res.result();
                vxState.setSock(sock);
                // 从sock读取数据后发送个数据到channel
                // 获取响应的buffer
                sock.handler(buffer -> {
                    LOGGER.debug("receive data: {}, to {}:{}", buffer, sock.remoteAddress().host(), sock.remoteAddress().port());
                    vxState.setRecvCount(vxState.getRecvCount() + buffer.length());
                    vxState.setLastDataDate(new Date());
                    long curSendId = ++channelSendId;
                    // 数据传输 需要再附带 8字节的当前 编码
                    vxState.sendProxyDataToChannel(buffer, curSendId);
                    vxState.pausePinger();
                });
                // 将[第一次]请求的 真实数据的数值 写入httpVx真实转发代理
                // 这里的解析正确的 目标地址 好像没什么用
                if (leftBuf != null) {
                    vxState.setSendCount(vxState.getSendCount() + leftBuf.length());
                    if (!vxState.isTargetIpParsed()) {
                        VxUtils.parseTargetIpPort(leftBuf, vxState);
                    }
                    sock.write(leftBuf);

                }
                // 将 [第一次以后] 要代理的 具体值 代理到对应的地址
                vxState.intallChannelDataConsumer(true);
                AbstractConsumer<String> channelCloseAddrConsumer = new ChannelCloseAddrConsumer(vertx, channelId);
                AbstractConsumer<String> connectCloseAddrConsumer = new ConnectCloseAddrConsumer(vertx, connectId);

                channelCloseAddrConsumer.consume(buffer -> {
                    LOGGER.warn("The channel {} was closed, try to pause the connect {}", channelId, connectId);
                    //channel关闭则暂停接收代理连接数据
                    sock.pause();
                    AbstractConsumer channelReconnectedConsumer = new ChannelReconnectedConsumer(vertx, channelId);

                    //超过10秒channel连接还没有恢复则关闭代理连接
                    long closeTimeId = vertx.setTimer(config.getChannelCloseResumeTimeout(), id -> {
                        LOGGER.error("channel {} closed exceed more than {} second" + ",close the connect {}",
                                channelId,
                                config.getChannelCloseResumeTimeout() / 1000,
                                connectId);
                        new ConnectCloseAddrSender<>(vertx, connectId).send("channel closed exceed more than " + config.getChannelCloseResumeTimeout() / 1000 + " second");
                        connectCloseAddrConsumer.unregister();
                        channelReconnectedConsumer.unregister();
                    });

                    //channel连接恢复则取消关闭连接定时器
                    channelReconnectedConsumer.consume(reconnectedMsg -> {
                        sock.resume();
                        vertx.cancelTimer(closeTimeId);
                        channelReconnectedConsumer.unregister();
                        LOGGER.warn("The channel {} reconnected, resume the connect {}", channelId, connectId);
                    });
                });

                connectCloseAddrConsumer.consume(buffer -> {
                    LOGGER.info("receive close connectId: {}, {}", connectId, buffer.body());
                    connectCloseAddrConsumer.unregister();
                    channelCloseAddrConsumer.unregister();
                    vxState.doClose();
                    sock.close();
                    LOGGER.info("success close connectId: {}", connectId);
                });

                // 关闭连接发送到channel
                sock.closeHandler(v -> {
                    connectCloseAddrConsumer.unregister();
                    channelCloseAddrConsumer.unregister();
                    vxState.doClose();
                    LOGGER.info("send connect close command: {}", connectId);
                });

                // 代理转发
                vxState.sendProxyConnectedToChannel();
            }
        });
    }

    private void consumerQueryList() {
        monitorRemoteConnectCloseAddrConsumer = new MonitorRemoteConnectCloseAddrConsumer(vertx);
        monitorRemoteConnectCloseAddrConsumer.consume(buffer -> {
            Buffer bodyBuf = buffer.body();
            String connectIds = bodyBuf.getString(0, bodyBuf.length());
            String[] ids = connectIds.split(",");
            for (String id : ids) {
                VxState state = hVxState.get(id);
                if (state != null) {
                    LOGGER.info("try to close and remove the connectId:{},shortId:{}"
                            , state.getConnectId()
                            , state.getConnectShortId());
                    new ConnectCloseAddrSender<String>(vertx, id).send("remote monitor close");
                    hVxState.remove(id);
                } else {
                    LOGGER.warn("can't find the hvstate connectId:{}", id);
                }
            }
        });

        monitorRemoteConnectDoAddrConsumer = new MonitorRemoteConnectDoAddrConsumer<Buffer>(vertx);
        monitorRemoteConnectDoAddrConsumer.consume(buffer -> {
            byte[] bytes = buffer.body().getBytes();
            ProxyQueryParams params = VxUtils.b2o(bytes);
            LOGGER.info("queryParams:{}", params);
            VxState[] states = hVxState.values().toArray(new VxState[0]);
            JsonObject json = new JsonObject();
            VxState[] filterRs = filterStates(states, params.getSearch());
            json.put("total", filterRs.length);
            JsonArray arr = new JsonArray();
            int start = params.getOffset() == -1 ? 0 : params.getOffset();
            int end = params.getLimit() == -1 ? filterRs.length
                    : Math.min(filterRs.length, start + params.getLimit());
            start = 0;
            end = filterRs.length;
            for (int i = start; i < end; i++) {
                VxState state = filterRs[i];
                JsonObject row = new JsonObject();
                row.put("connectId", state.getConnectId());
                //row.put("connectShortId", state.getConnectShortId());
                row.put("channelId", state.getChannelId());
                //row.put("channelShortId", state.getChannelShortId());
                //row.put("sourceIp", state.getClientIp());
                row.put("targetIpPort", state.getTargetIpPort());
                row.put("startDate", DateUtil.toStr(state.getInitDate()));
                row.put("lastDataDate", DateUtil.toStr(state.getLastDataDate()));
                row.put("closeDate", DateUtil.toStr(state.getCloseDate()));
                row.put("sendCount", state.getSendCount());
                row.put("recvCount", state.getRecvCount());
                row.put("closed", state.isClosed() ? "Yes" : "No");
                arr.add(row);
            }
            //String channelId = ProxyUtils.getChannelId(vertx);
            json.put("rows", arr);
            Buffer respBuf = Buffer.buffer();
            CommandId cid = CommandId.REMOTE_CONNECT_RESP;
            respBuf.appendInt(cid.getId());
            respBuf.appendInt(0);//设置占位符
            respBuf.appendString(json.toString(), "UTF-8");
            respBuf.setInt(4, respBuf.length());//设置真实大小
            new ChannelDataSender(vertx, channelId).send(respBuf);
        });
    }

    @Override
    public void start(VxConfig config) {
        setClearTimer();
        consumerQueryList();
        // client 不会调用到, server 代理给 client
        // 第一次 连接的时候 会消费
        new ConnectNewAddrConsumer<Buffer>(vertx).consume(buffer -> {
            Buffer dataBuf = buffer.body();
            String connectId = dataBuf.getString(Constants.commandHeadLength, Constants.commandHeadWithIdLength);

            VxState vxState = new VxState(connectId, channelId, vertx);
            hVxState.put(connectId, vxState);
            long recvId = dataBuf.getLong(Constants.commandHeadWithIdLength);
            LOGGER.debug("receive sendId: {}", recvId);

            // 这里 的剩余 数值 是真实传递的值
            Buffer leftBuf = dataBuf.slice(Constants.dataCommandHeadLength, dataBuf.length());
            handlerConnectMsg(config.getProxyHost(), config.getProxyPort(), leftBuf, vxState);
        });

        // client 调用到, client 代理给 具体的 地址IP
        new DirectConnectNewAddrConsumer<Buffer>(vertx).consume(buffer -> {
            Buffer dataBuf = buffer.body();
            String connectId = dataBuf.getString(Constants.commandHeadLength
                    , Constants.commandHeadWithIdLength);

            VxState vxState = new VxState(connectId, channelId, vertx);
            hVxState.put(connectId, vxState);
            long recvId = dataBuf.getLong(Constants.commandHeadWithIdLength);
            LOGGER.debug("receive sendId: {}", recvId);

            Buffer leftBuf = dataBuf.slice(Constants.dataCommandHeadLength, dataBuf.length());
            VxUtils.parseTargetIpPort(leftBuf, vxState);
            LOGGER.info("direct connect: {}, to {}:{}", connectId,
                    vxState.getTargetIp(), vxState.getTargetPort());
            handlerConnectMsg(vxState.getTargetIp(), vxState.getTargetPort(), null, vxState);
        });
        LOGGER.info("ProxyServer is started,for channelId: {}", channelId);
    }
}
