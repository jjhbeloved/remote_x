package cd.blog.humbird.vertx.vx;

import cd.blog.humbird.vertx.demo.util.Runner;
import cd.blog.humbird.vertx.vx.beans.HttpVxState;
import io.vertx.core.Handler;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.NetSocket;
import io.vertx.core.streams.Pump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/5.
 */
public class HttpsVx extends VxBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpsVx.class);

    public static void main(String[] args) {
        Runner.runExample(HttpsVx.class);
    }

    @Override
    public void start(VxConfig config) {
        Handler<NetSocket> handler = sock -> {
            HttpVxState httpVxState = new HttpVxState();
            httpVxState.setSock(sock);
            sock.handler(buffer -> {
                if (httpVxState.appendBuf(buffer)) {
                    String ip = httpVxState.getTargetIp();
                    int port = httpVxState.getTargetPort();
                    LOGGER.info("try to connect to {}:{}, client  {}:{} ", ip, port,
                            sock.remoteAddress().host(),
                            sock.remoteAddress().port());
                    vertx.createNetClient().connect(port, ip, tSock -> {
                        if (tSock.succeeded()) {
                            LOGGER.info("success connect to {}:{}", ip, port);
                            NetSocket netSocket = tSock.result();
                            httpVxState.setTargetSock(tSock.result());
                            Pump.pump(sock, netSocket).start();
                            Pump.pump(netSocket, sock).start();
                            // url 的请求/ 非 CONNECT 格式的, 需要传输 协议头信息
                            // 其余的返回 200 即可
                            if (httpVxState.isNeedSendHead()) {
                                netSocket.write(httpVxState.getBuf());
                            } else {
                                sock.write("HTTP/1.1 200 Connection established\r\n\r\n");
                            }
                            netSocket.closeHandler(v -> {
                                LOGGER.info("target sock closed: {}", httpVxState.getTargetIpPort());
                                sock.close();
                            });
                        } else {
                            LOGGER.warn("fail connect to {}:{}", ip, port);
                            sock.close();
                        }
                    });
                }
            });
            sock.closeHandler(v -> {
                LOGGER.info("sock closed: {}", httpVxState.getTargetIpPort());
                if (httpVxState.getTargetSock() != null) {
                    httpVxState.getTargetSock().close();
                }
            });
        };
        NetServerOptions options = new NetServerOptions();
        options.setTcpKeepAlive(true);
        vertx.createNetServer(options).connectHandler(handler).listen(config.getProxyPort(), config.getProxyHost());
        LOGGER.info("started http proxy on {}", config.getProxyPort());
    }
}
