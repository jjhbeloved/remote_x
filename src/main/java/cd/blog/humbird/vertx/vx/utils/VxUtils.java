package cd.blog.humbird.vertx.vx.utils;

import cd.blog.humbird.vertx.vx.beans.VxState;
import cd.blog.humbird.vertx.vx.common.AddressTemplate;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by david on 17/1/6.
 */
public class VxUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxUtils.class);

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
     * @param vxState
     */
    public static void parseTargetIpPort(Buffer buf, VxState vxState) {
        if (buf.length() >= 10) {
            //socket5
            if (buf.getByte(0) == 0x05
                    && buf.getByte(1) == 0x01
                    && buf.getByte(2) == 0x00) {
                vxState.setProxyType("socks 5");
                //
                if (buf.getByte(3) == 0x01) {
                    String ip = findHost(buf.getBytes(4, 8), 0, 3);
                    int port = findPort(buf.getBytes(8, 10), 0, 1);
                    vxState.setTargetIpParsed(true);
                    vxState.setTargetIpPort(ip + ":" + port);
                    vxState.setTargetIp(ip);
                    vxState.setTargetPort(port);
                }
                if (buf.getByte(3) == 0x03) {
                    String ip = buf.getString(4, buf.length() - 2);
                    int port = findPort(buf.getBytes(buf.length() - 2, buf.length()), 0, 1);
                    vxState.setTargetIpParsed(true);
                    vxState.setTargetIpPort(ip + ":" + port);
                    vxState.setTargetIp(ip);
                    vxState.setTargetPort(port);
                }
            } else if ("CONNECT ".equalsIgnoreCase(buf.getString(0, 8, "UTF-8"))) {
                vxState.setProxyType("https");
                String connectStr = buf.getString(8, buf.length(), "UTF-8");
                String[] datas = connectStr.split(" ");
                String ipPort = datas[0];
                vxState.setTargetIpParsed(true);
                vxState.setTargetIpPort(ipPort);
                String[] aTmp = ipPort.split(":");
                vxState.setTargetIp(aTmp[0]);
                vxState.setTargetPort(Integer.parseInt(aTmp[1]));
                String sec = datas[datas.length - 1];
                sec = sec.substring(0, sec.length() - 4);
                if (!vxState.validate(sec)) {
                    LOGGER.error("Auth validate failed. user:passwd {}, from: {}", new Base64().decode(sec), vxState.getSock().remoteAddress().toString());
                }
            }
            if (vxState.isTargetIpParsed()) {
                LOGGER.info("try to connect to {}, clientIp:{}, connectId:{}"
                        , vxState.getTargetIpPort(), vxState.getClientIp()
                        , vxState.getConnectId());
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

    public static byte[] o2b(Serializable o) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(o);
            oos.flush();
            byte[] bytes = bos.toByteArray();
            return bytes;
        } catch (Exception ex) {
            return null;
        }
    }

    public static <T> T b2o(byte[] b) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(b);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (T) ois.readObject();
        } catch (Exception ex) {
            return null;
        }
    }

    public static LocalMap<String, Boolean> getChannelPauseIdsMap(Vertx vertx) {
        return vertx.sharedData().getLocalMap("channelPauseIds");
    }

    public static LocalMap<String, String> getChannelIdsMap(Vertx vertx) {
        return vertx.sharedData().getLocalMap("channelIds");
    }

    public static LocalMap<String, String> getActiveConnectMap(Vertx vertx) {
        return vertx.sharedData().getLocalMap("activeConnectMap");
    }

    public static void sendChannelData(String channelId, Vertx vertx, Buffer buf) {
        vertx.eventBus().send(AddressTemplate.getChannelSendAddr(channelId), buf);
    }

    public static JsonObject toJsonObj(Object o) {
        if (o == null) {
            return new JsonObject();
        }
        String strJson = Json.encode(o);
        JsonObject json = new JsonObject(strJson);
        return json;
    }

    /**
     * 获取泛型的Collection Type
     *
     * @param jsonStr         json字符串
     * @param collectionClass 泛型的Collection
     * @param elementClasses  元素类型
     */
    public static <T> T readJson(String jsonStr, Class<?> collectionClass, Class<?>... elementClasses) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaType javaType = mapper.getTypeFactory().constructParametrizedType(collectionClass,
                    collectionClass, elementClasses);
            return mapper.readValue(jsonStr, javaType);
        } catch (Exception ex) {
            LOGGER.error("decore json ex:" + jsonStr, ex);
            return null;
        }
    }
}
