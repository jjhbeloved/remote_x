package cd.blog.humbird.vertx.hm.commons;

/**
 * Created by david on 17/1/16.
 */
public class EventAddress {

    public static String ping(String channelId) {
        return "/channel/ping/" + channelId;
    }

    public static String pong(String channelId) {
        return "/channel/pong/" + channelId;
    }

    /**
     * 关闭 ping-pong sock
     *
     * @param channelId
     * @return
     */
    public static String close(String channelId) {
        return "/channel/close/" + channelId;
    }

    /**
     * 关闭 公网代理端口
     *
     * @param channelId
     * @return
     */
    public static String closePublic(String channelId) {
        return "/channel/closePublic/" + channelId;
    }

    /**
     * 监控 已连接信息 连接
     *
     * @return
     */
    public static String connectedAddr(String channelId) {
        return "/channel/connected/" + channelId;
    }

    /**
     * 命令传输通道
     *
     * @param channelId
     * @return
     */
    public static String command(String channelId) {
        return "/channel/command/" + channelId;
    }

    /**
     * 创建新连接
     *
     * @return
     */
    public static String newAddr() {
        return "/connect/new";
    }

    /**
     * 选择连接
     *
     * @return
     */
    public static String chooseAddr() {
        return "/connect/choose";
    }

    /**
     * 取消连接
     *
     * @return
     */
    public static String removeAddr() {
        return "/connect/remove";
    }

    /**
     * 关闭连接
     *
     * @return
     */
    public static String closeAddr(String connectId) {
        return "/connect/close/" + connectId;
    }

    /**
     * 数据初始接收通信
     *
     * @return
     */
    public static String recAddr(String connectId) {
        return "/connect/rec/" + connectId;
    }

    /**
     * 连接ping
     *
     * @return
     */
    public static String pingAddr(String connectId) {
        return "/connect/ping/" + connectId;
    }

    /**
     * 连接pong
     *
     * @return
     */
    public static String pongAddr(String connectId) {
        return "/connect/pong/" + connectId;
    }
}
