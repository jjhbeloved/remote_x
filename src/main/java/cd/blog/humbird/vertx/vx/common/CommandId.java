/**
 * Copyright (c) 2014 Asiainfo-Linkage Communication Software Co.,
 * Ltd. All Rights Reserved.
 * <p>
 * This software is the confidential and proprietary information of
 * Asiainfo-Linkage Communication Software Co., Ltd.
 * ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with ailk.
 * <p>
 * ailk MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ailk SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package cd.blog.humbird.vertx.vx.common;

public enum CommandId {
    CLOSE(0),
    DATA(1),
    NEW_CONNECT(2),
    PING(3),
    SEND_FEEDBACK(4),
    CONNECT_PING(5),
    REMOTE_CONNECT_REQ(6),
    REMOTE_CONNECT_RESP(7),
    REMOTE_CONNECT_CLOSE(8),
    CHANNEL_PING(9),
    CHANNEL_PING_RESP(10),
    CONNECT_PING_RESP(11),
    CHANNEL_INFO(12),
    CHANNEL_INFO_RESP(13),
    NEW_DIRECT_CONNECT(14),
    PROXY_CONNECTED(15);
    
    private int id;

    private CommandId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CommandId valueOf(int id) {
        switch (id) {
            case 0:
                return CLOSE;
            case 1:
                return DATA;
            case 2:
                return NEW_CONNECT;
            case 3:
                return PING;
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
                return CHANNEL_PING;
            case 10:
                return CHANNEL_PING_RESP;
            case 11:
                return CONNECT_PING_RESP;
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
