package cd.blog.humbird.vertx.hm.beans;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Date;

/**
 * Created by david on 17/1/17.
 */
public class ProxyState {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyState.class);

    private boolean connected = false;

    private Date startDate;

    private Date lastDataDate = new Date();

    private Date closeDate;

    private long sendCount = 0L;

    private long recvCount = 0L;

    private boolean targetIpParsed = false;

    private String targetRemote;

    private String targetIp;

    private int targetPort;

    private boolean init = false;

    private Buffer buf = Buffer.buffer();

    private NetSocket sock;

    private NetSocket targetSock;

    private boolean needSendHead = false;

    public int getTargetPort() {
        return targetPort;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public String getTargetRemote() {
        return targetRemote;
    }

    public Buffer getBuf() {
        return buf;
    }

    public NetSocket getTargetSock() {
        return targetSock;
    }

    public void setTargetSock(NetSocket targetSock) {
        this.targetSock = targetSock;
    }

    public NetSocket getSock() {
        return sock;
    }

    public void setSock(NetSocket sock) {
        this.sock = sock;
    }

    public boolean isNeedSendHead() {
        return needSendHead;
    }

    public boolean appendBuf(Buffer tmpBuf) {
        buf.appendBuffer(tmpBuf);
        String head = buf.getString(0, buf.length());
        if (head.indexOf("\r\n\r\n") != -1) {
//            LOGGER.debug(head);
            String connectStr = head.split("[\r\n]+")[0];
            if (connectStr.startsWith("CONNECT")) {
                String ipPort = connectStr.split("[ ]+")[1];
                String[] aTmp = ipPort.split(":");
                String ip = aTmp[0];
                int port = Integer.parseInt(aTmp[1]);
                this.targetIp = ip;
                this.targetPort = port;
            } else {
                String url = connectStr.split("[ ]+")[1];
                try {
                    URL u = new URL(url);
                    this.targetIp = u.getHost();
                    this.targetPort = u.getPort() == -1 ? u.getDefaultPort() : u.getPort();
                } catch (Exception ex) {
                    LOGGER.error("parse url fail :{}", url);
                }
                needSendHead = true;
            }
            this.targetRemote = targetIp + ":" + targetPort;
            this.targetIpParsed = true;
            return true;
        }
        return false;
    }
}
