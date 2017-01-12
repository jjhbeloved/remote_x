package cd.blog.humbird.vertx.vx.beans;

import java.io.Serializable;

public class ChannelClientInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String computerName;    // 计算节点(其次使用)
    private String channelClientName;   // 信道客户端节点(优先使用), 作为别名使用(必填)
    private String clientIps;   // 客户端地址群
    private String computerUserName;    // 计算用户
    private String clientPid;   // 客户端PID
    private String domainName;  // 域名(作为回调别名)(必填)


    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }


    /**
     * @return computerName属性
     */
    public String getComputerName() {
        return computerName;
    }

    /**
     * @param computerName 设置computerName属性
     */
    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    /**
     * @return channelClientName属性
     */
    public String getChannelClientName() {
        return channelClientName;
    }

    /**
     * @param channelClientName 设置channelClientName属性
     */
    public void setChannelClientName(String channelClientName) {
        this.channelClientName = channelClientName;
    }

    /**
     * @return clientIps属性
     */
    public String getClientIps() {
        return clientIps;
    }

    /**
     * @param clientIps 设置clientIps属性
     */
    public void setClientIps(String clientIps) {
        this.clientIps = clientIps;
    }

    /**
     * @return computerUserName属性
     */
    public String getComputerUserName() {
        return computerUserName;
    }

    /**
     * @param computerUserName 设置computerUserName属性
     */
    public void setComputerUserName(String computerUserName) {
        this.computerUserName = computerUserName;
    }

    /**
     * @return clientPid属性
     */
    public String getClientPid() {
        return clientPid;
    }

    /**
     * @param clientPid 设置clientPid属性
     */
    public void setClientPid(String clientPid) {
        this.clientPid = clientPid;
    }
}
