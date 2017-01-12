package cd.blog.humbird.vertx.vx;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocket;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;

/**
 * Created by david on 17/1/5.
 */
public class VxSock {
    private NetSocket netSock;
    private WebSocket webSock;
    private ServerWebSocket serverWebSock;

    public VxSock(NetSocket netSock) {
        this.netSock = netSock;
    }

    public VxSock(WebSocket webSock) {
        this.webSock = webSock;
    }

    public VxSock(ServerWebSocket serverWebSock) {
        this.serverWebSock = serverWebSock;
    }

    public void close() {
        if (netSock != null) {
            netSock.close();
        } else if (serverWebSock != null) {
            serverWebSock.close();
        } else {
            webSock.close();
        }
    }

    public VxSock write(String str) {
        if (netSock != null) {
            netSock.write(str);
        } else if (serverWebSock != null) {
            serverWebSock.write(Buffer.buffer().appendString(str));
        } else {
            webSock.write(Buffer.buffer().appendString(str));
        }
        return this;
    }

    /**
     * @return the remote address for this socket
     */
    public SocketAddress remoteAddress() {
        if (netSock != null) {
            return netSock.remoteAddress();
        } else if (serverWebSock != null) {
            return serverWebSock.remoteAddress();
        } else {
            return webSock.remoteAddress();
        }
    }

    /**
     * @return the local address for this socket
     */
    public SocketAddress localAddress() {
        if (netSock != null) {
            return netSock.localAddress();
        } else if (serverWebSock != null) {
            return serverWebSock.localAddress();
        } else {
            return webSock.localAddress();
        }
    }

    public VxSock closeHandler(Handler<Void> handler) {
        if (netSock != null) {
            netSock.closeHandler(handler);
        } else if (serverWebSock != null) {
            serverWebSock.closeHandler(handler);
        } else {
            webSock.closeHandler(handler);
        }
        return this;
    }

    public VxSock exceptionHandler(Handler<Throwable> handler) {
        if (netSock != null) {
            netSock.exceptionHandler(handler);
        } else if (serverWebSock != null) {
            serverWebSock.exceptionHandler(handler);
        } else {
            webSock.exceptionHandler(handler);
        }
        return this;
    }

    public VxSock handler(Handler<Buffer> handler) {
        if (netSock != null) {
            netSock.handler(handler);
        } else if (serverWebSock != null) {
            serverWebSock.handler(handler);
        } else {
            webSock.handler(handler);
        }
        return this;
    }

    public VxSock pause() {
        if (netSock != null) {
            netSock.pause();
        } else if (serverWebSock != null) {
            serverWebSock.pause();
        } else {
            webSock.pause();
        }
        return this;
    }

    public VxSock resume() {
        if (netSock != null) {
            netSock.resume();
        } else if (serverWebSock != null) {
            serverWebSock.resume();
        } else {
            webSock.resume();
        }
        return this;
    }

    public VxSock endHandler(Handler<Void> endHandler) {
        if (netSock != null) {
            netSock.endHandler(endHandler);
        } else if (serverWebSock != null) {
            serverWebSock.endHandler(endHandler);
        } else {
            webSock.endHandler(endHandler);
        }
        return this;
    }

    public VxSock write(Buffer data) {
        if (netSock != null) {
            netSock.write(data);
        } else if (serverWebSock != null) {
            //serverWebSock.write(data);
            serverWebSock.writeBinaryMessage(data);
        } else {
            webSock.writeBinaryMessage(data);
        }
        return this;
    }

    public VxSock drainHandler(Handler<Void> handler) {
        if (netSock != null) {
            netSock.drainHandler(handler);
        } else if (serverWebSock != null) {
            serverWebSock.drainHandler(handler);
        } else {
            webSock.drainHandler(handler);
        }
        return this;
    }

    public boolean writeQueueFull() {
        if (netSock != null) {
            return netSock.writeQueueFull();
        } else if (serverWebSock != null) {
            return serverWebSock.writeQueueFull();
        } else {
            return webSock.writeQueueFull();
        }
    }
}
