package cd.blog.humbird.vertx.vx.common;

/**
 * Created by david on 17/1/6.
 */
public class AddressTemplate {

    /**
     * 信道传输数据
     *
     * @param channelId
     * @return
     */
    public static String getChannelSendAddr(String channelId) {
        return "/channel/send/" + channelId;
    }

    public static String getChannelClientInfoAddr(String channelId) {
        return "/channel/client_info/" + channelId;
    }

    public static String getChannelClosedAddr(String channelId){
        return "/channel/closed/"+channelId;
    }

    public static String getChannelChooseAddr(){
        return "/channel/choose";
    }

    public static String getChannelConnectedAddr(String channelId) {
        return "/channel/connected/" + channelId;
    }

    public static String getChannelPingAddr(String channelId){
        return "/channel/ping/"+channelId;
    }

    public static String getChannelPingRespAddr(String channelId){
        return "/channel/ping_resp/"+channelId;
    }

    /**
     * 连接关闭，返回channel通道
     * @return
     */
    public static String getChannelReturnAddr(){
        return "/channel/return";
    }

    public static String getConnectReceiveAddr(String connectId){
        return "/connect/receive/"+connectId;
    }

    public static String getConnectCloseAddr(String connectId){
        return "/connect/close/"+connectId;
    }

    public static String getConnectPingAddr(String connectId) {
        return "/connect/ping/" + connectId;
    }

    public static String getConnectPingRespAddr(String connectId) {
        return "/connect/ping_resp/" + connectId;
    }

    public static String getConnectNewAddr() {
        return "/connect/new";
    }

    public static String getDirectConnectNewAddr() {
        return "/direct_connect/new";
    }

    public static String getDirectConnectedAddr(String connectId) {
        return "/direct_connect/connected/" + connectId;
    }

    /**
     * 监控
     */
    public static String getMonitorChannelAddr(){
        return "/proxy_monitor/channel/list";
    }

    public static String getMonitorRemoteConnectAddr(){
        return "/proxy_monitor/remote/connect/list";
    }

    public static String getMonitorRemoteConnectRespAddr() {
        return "/proxy_monitor/remote/connect/list/resp";
    }

    public static String getMonitorRemoteConnectDoAddr() {
        return "/proxy_monitor/remote/connect/list/do";
    }

    public static String getMonitorRemoteConnectCloseAddr() {
        return "/proxy_monitor/remote/connect/close";
    }
}
