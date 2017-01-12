package cd.blog.humbird.vertx.vx.utils;

import cd.blog.humbird.vertx.vx.beans.ChannelClientInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;

public class ChannelClientInfoFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelClientInfoFactory.class);
    public static final String LOCAL_HOST_IP = "127.0.0.1";
    private static ChannelClientInfo baseInfo = null;
    public static String LockStr = "LOCKSTR";
    public static String[] aClientIps = null;

    public static ChannelClientInfo getChannelClientInfo() {
        if (baseInfo == null) {
            synchronized (LockStr) {
                if (baseInfo == null) {
                    RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
                    // format: "ClientPid@ComputerName"
                    String name = runtime.getName();
                    String[] aTmp = name.split("@", 2);
                    ChannelClientInfo tmpInfo = new ChannelClientInfo();
                    tmpInfo.setComputerName(aTmp[1]);
                    tmpInfo.setClientPid(aTmp[0]);
                    tmpInfo.setClientIps(getLocalClientIpsStr());
                    tmpInfo.setComputerUserName(System.getProperty("user.name"));
                    baseInfo = tmpInfo;
                }

            }

        }

        return baseInfo;


    }

    public static String getLocalClientIpsStr() {
        String[] aIps = getLocalClientIps();
        if (aIps == null) {
            return "localhost";
        }
        StringBuilder sbIp = new StringBuilder();
        for (String ip : aIps) {
            sbIp.append(ip).append(",");
        }
        return sbIp.toString();
    }

    /**
     * 获取本地定义的所有IP
     *
     * @return String[]
     */
    public static String[] getLocalClientIps() {
        try {
            synchronized (LockStr) {
                if (aClientIps != null) {
                    String[] aIp = new String[aClientIps.length];
                    System.arraycopy(aClientIps, 0, aIp, 0, aIp.length);
                    return aIp;
                }
            }

            String[] aIp = null;
            java.util.List ipList = new java.util.ArrayList();
            java.util.Enumeration netInterfaces = null;
            try {
                netInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
            }

            boolean isGetIp = false;
            InetAddress ip = null;
            while (netInterfaces != null && netInterfaces.hasMoreElements()) {
                java.net.NetworkInterface ni = (java.net.NetworkInterface)
                        netInterfaces.nextElement();
                java.util.Enumeration list = ni.getInetAddresses();

                while (list.hasMoreElements()) {
                    try {
                        ip = (InetAddress) list.nextElement();
                    } catch (Exception ex1) {
                        continue;
                    }
                    String ipAddr = ip.getHostAddress();
                    if (ipAddr.indexOf(LOCAL_HOST_IP) == -1 && ipAddr.indexOf(':') < 0) {
                        ipList.add(ipAddr);
                    } else {
                        ip = null;
                    }
                }
            }

            if (ipList.size() == 0) {
                InetAddress inet = InetAddress.getLocalHost();
                InetAddress[] aInet = InetAddress.getAllByName(inet.getHostName());
                if (aInet != null) {
                    aIp = new String[aInet.length];
                    for (int i = 0; i < aInet.length; i++) {
                        aIp[i] = aInet[i].getHostAddress();
                    }
                } else {
                    aIp = new String[1];
                    aIp[0] = inet.getHostAddress();
                }
            } else {
                isGetIp = true;
                aIp = new String[ipList.size()];
                for (int i = 0; i < ipList.size(); i++) {
                    aIp[i] = (String) ipList.get(i);
                }
            }

            synchronized (LockStr) {
                if (isGetIp) {
                    aClientIps = new String[aIp.length];
                    System.arraycopy(aIp, 0, aClientIps, 0, aClientIps.length);
                }
            }

            return aIp;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return null;
        }
    }
}
