package cd.blog.humbird.vertx.hm.commons;

/**
 * Created by david on 17/1/17.
 */
public class Constant {

    /**
     * 命令头字节长度（两个整形）
     */
    public static final int commandHeadLength = 8;
    /**
     * 命令头部+channelId或者connectId长度为8+36位的uuid
     */
    public static final int commandHeadWithIdLength = commandHeadLength + 36;

    /**
     * 发送Id长度为8个字节的long类型
     */
    public static final int sendIdLength = 8;

    /**
     * data命令长度=commandHeadWithIdLength+8位（long类型表示sendId）
     */
    public static final int dataCommandHeadLength = commandHeadWithIdLength + sendIdLength;


}
