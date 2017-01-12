package cd.blog.humbird.vertx.vx.beans;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Date;

public class HttpVxState {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpVxState.class);
    private boolean connected = false;
    private Date startDate;

    private Date lastDataDate = new Date();
    private Date closeDate;

    private long sendCount = 0L;
    private long recvCount = 0L;
    private boolean targetIpParsed = false;
    private String targetIpPort;
    private boolean closed = false;

    private Buffer buf = Buffer.buffer();
    private String targetIp;
    private int targetPort;

    private NetSocket sock;
    private NetSocket targetSock;

    private boolean needSendHead = false;

    /**
     * 只能解析 HTTP 请求的信息
     *
     * @param tmpBuf
     * @return
     */
    public boolean appendBuf(Buffer tmpBuf) {
        buf.appendBuffer(tmpBuf);
        String head = buf.getString(0, buf.length());
        if (head.indexOf("\r\n\r\n") != -1) {
            LOGGER.debug(head);
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
            this.setTargetIpPort(targetIp + ":" + targetPort);
            this.setTargetIpParsed(true);
            return true;
        }
        return false;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getLastDataDate() {
        return lastDataDate;
    }

    public void setLastDataDate(Date lastDataDate) {
        this.lastDataDate = lastDataDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
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

    public boolean isTargetIpParsed() {
        return targetIpParsed;
    }

    public void setTargetIpParsed(boolean targetIpParsed) {
        this.targetIpParsed = targetIpParsed;
    }

    public String getTargetIpPort() {
        return targetIpPort;
    }

    public void setTargetIpPort(String targetIpPort) {
        this.targetIpPort = targetIpPort;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public boolean isNeedSendHead() {
        return needSendHead;
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
}
