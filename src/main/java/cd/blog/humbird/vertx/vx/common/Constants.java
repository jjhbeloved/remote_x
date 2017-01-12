package cd.blog.humbird.vertx.vx.common;

/**
 * Created by david on 17/1/5.
 */
public class Constants {

    /**
     * 命令编码(1个整形=4个byte)+命令编码长度(1个整形=4个byte)
     */
    public static final int commandHeadLength = 8;

    /**
     * 命令编码(1个整形=4个byte)+命令长度(1个整形=4个byte)+ChannelID/ConnectId长度(36byte的UUID)
     */
    public static final int commandHeadWithIdLength = commandHeadLength + 36;

    /**
     * 发送Id长度为8个字节的long类型
     */
    public static final int sendIdLength = 8;

    /**
     * data命令长度=commandHeadWithIdLength+8位（long类型表示sendId）
     */
    public static final int dataCommandHeadLength = commandHeadWithIdLength+sendIdLength;

}
