package cd.blog.humbird.vertx.hm.beans;

import java.io.Serializable;

public class ChannelClientInfo implements Serializable {

    private String computerName;
    private String channelClientName;
    private String clientIps;
    private String computerUserName;
    private String clientPid;
    private String domainName;


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
