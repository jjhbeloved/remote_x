package cd.blog.humbird.vertx.hm;

import cd.blog.humbird.vertx.demo.util.Runner;
import cd.blog.humbird.vertx.hm.beans.Config;
import cd.blog.humbird.vertx.hm.beans.ProxyState;
import io.vertx.core.net.NetServer;
import io.vertx.core.streams.Pump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/17.
 */
public class Proxy extends BaseVertx {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

    private NetServer server;

    public static void main(String[] args) {
        Runner.runExample(Proxy.class);
    }

    @Override
    public void start(Config config) {
        server = vertx.createNetServer().connectHandler(sock -> {
            ProxyState state = new ProxyState();
            sock.handler(buffer -> {
                if (state.appendBuf(buffer)) {
                    LOGGER.info("try to connect to {}, client {} ", state.getTargetRemote(), sock.remoteAddress());
                    vertx.createNetClient().connect(state.getTargetPort(), state.getTargetIp(), result -> {
                        if (result.succeeded()) {
                            LOGGER.info("success connect to {}", state.getTargetRemote());
                            state.setTargetSock(result.result());
                            Pump.pump(sock, result.result()).start();
                            Pump.pump(result.result(), sock).start();
                            // 只有 url 需要传递头信息, ssh CONNECT 连接不需要
                            if (state.isNeedSendHead()) {
                                result.result().write(state.getBuf());
                            } else {
                                sock.write("HTTP/1.0 200 Connection established\r\n\r\n");
                            }
                            result.result().closeHandler(v -> {
                                LOGGER.info("target sock closed: {}", state.getTargetRemote());
                                sock.close();
                            });
                        } else {
                            LOGGER.warn("fail connect to {}", state.getTargetRemote());
                            sock.close();
                        }
                    });
                }
            });
        }).listen(config.getProxyPort(), config.getProxyIp());
        LOGGER.info("started proxy on {}", config.getProxyPort());
    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("close proxy");
    }

}
