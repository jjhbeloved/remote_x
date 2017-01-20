package cd.blog.humbird.vertx.hm.utils;

import cd.blog.humbird.vertx.hm.beans.State;
import io.vertx.core.buffer.Buffer;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 17/1/19.
 */
public class HmUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HmUtil.class);

    /**
     * 这里是解析 代理软件提供的代理请求的地址信息
     * <p>
     * socks5 协议分为
     * |VER | CMD |　RSV　| ATYP | DST.ADDR | DST.PORT |
     * VER protocol version：X'05'
     * CMD
     * CONNECT X'01'
     * BIND X'02'
     * UDP ASSOCIATE X'03'
     * RSV RESERVED
     * ATYP address type of following address
     * IP V4 address: X'01'
     * DOMAINNAME: X'03'
     * IP V6 address: X'04'
     * DST.ADDR desired destination address
     * DST.PORT desired destination port in network octet order
     * <p>
     * https 协议以 CONNECT 开头
     *
     * @param buf
     * @param state
     */
    public static void parseTargetIpPort(Buffer buf, State state) {
        if (buf.length() >= 10) {
            //socket5
            if (buf.getByte(0) == 0x05
                    && buf.getByte(1) == 0x01
                    && buf.getByte(2) == 0x00) {
                state.setProxyType("socks 5");
                //
                if (buf.getByte(3) == 0x01) {
                    String ip = findHost(buf.getBytes(4, 8), 0, 3);
                    int port = findPort(buf.getBytes(8, 10), 0, 1);
                    state.setTargetIpParsed(true);
                    state.setTargetRemote(ip + ":" + port);
                    state.setTargetIp(ip);
                    state.setTargetPort(port);
                }
                if (buf.getByte(3) == 0x03) {
                    String ip = buf.getString(4, buf.length() - 2);
                    int port = findPort(buf.getBytes(buf.length() - 2, buf.length()), 0, 1);
                    state.setTargetIpParsed(true);
                    state.setTargetRemote(ip + ":" + port);
                    state.setTargetIp(ip);
                    state.setTargetPort(port);
                }
            } else if ("CONNECT ".equalsIgnoreCase(buf.getString(0, 8, "UTF-8"))) {
                state.setProxyType("https");
                String connectStr = buf.getString(8, buf.length(), "UTF-8");
                String[] datas = connectStr.split(" ");
                String ipPort = datas[0];
                state.setTargetIpParsed(true);
                state.setTargetRemote(ipPort);
                String[] aTmp = ipPort.split(":");
                state.setTargetIp(aTmp[0]);
                state.setTargetPort(Integer.parseInt(aTmp[1]));
                String sec = datas[datas.length - 1];
                sec = sec.substring(0, sec.length() - 4);
                if (!state.validate(sec)) {
                    LOGGER.error("Auth validate failed. user:passwd {}, from: {}", new Base64().decode(sec), state.getSock().remoteAddress().toString());
                }
            }
            if (state.isTargetIpParsed()) {
                LOGGER.info("try to connect to {}, clientIp: {}, connectId: {}"
                        , state.getTargetRemote(), state.getClientIp()
                        , state.getConnectId());
            }
        }
    }

    /**
     * 4字节, 每个字节代表0-255
     *
     * @param bArray
     * @param begin
     * @param end
     * @return
     */
    public static String findHost(byte[] bArray, int begin, int end) {
        StringBuffer sb = new StringBuffer();
        for (int i = begin; i <= end; i++) {
            sb.append(Integer.toString(0xFF & bArray[i]));
            sb.append(".");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * 2字节, 总长 65536
     * 00000000 00000000 = 65536
     *
     * @param bArray
     * @param begin
     * @param end
     * @return
     */
    public static int findPort(byte[] bArray, int begin, int end) {
        int port = 0;
        for (int i = begin; i <= end; i++) {
            port <<= 8;
            port += bArray[i];
        }
        return port;
    }
}
