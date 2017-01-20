package cd.blog.humbird.vertx.hm.commons;

public enum CmdId {
    CLOSE(0), DATA(1), NEW_CONNECT(2), X_PING(3), SEND_FEEDBACK(4), CONNECT_PING(5), REMOTE_CONNECT_REQ(6), REMOTE_CONNECT_RESP(7), REMOTE_CONNECT_CLOSE(8), PING(9), PONG(10), CONNECT_PONG(11), CHANNEL_INFO(12), CHANNEL_INFO_RESP(13), NEW_DIRECT_CONNECT(14), PROXY_CONNECTED(15);
    private int id;

    private CmdId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CmdId valueOf(int id) {
        switch (id) {
            case 0:
                return CLOSE;
            case 1:
                return DATA;
            case 2:
                return NEW_CONNECT;
            case 3:
                return X_PING;
            case 4:
                return SEND_FEEDBACK;
            case 5:
                return CONNECT_PING;
            case 6:
                return REMOTE_CONNECT_REQ;
            case 7:
                return REMOTE_CONNECT_RESP;
            case 8:
                return REMOTE_CONNECT_CLOSE;
            case 9:
                return PING;
            case 10:
                return PONG;
            case 11:
                return CONNECT_PONG;
            case 12:
                return CHANNEL_INFO;
            case 13:
                return CHANNEL_INFO_RESP;
            case 14:
                return NEW_DIRECT_CONNECT;
            case 15:
                return PROXY_CONNECTED;
            default:
                return null;
        }
    }
}
