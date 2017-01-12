package cd.blog.humbird.vertx.vx;

import cd.blog.humbird.vertx.vx.beans.VxState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 17/1/8.
 */
public abstract class VxBase extends VxAbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(VxBase.class);
    protected Map<String, VxState> hVxState = new HashMap();

    /**
     * 定时检查连接, 超过时长 则删除
     */
    protected void setClearTimer() {
        vertx.setPeriodic(60 * 1000, tid -> {
            VxState[] states = hVxState.values().toArray(new VxState[0]);
            int i = 0;
            for (VxState state : states) {
                if (state.isClosed() && (state.getLastDataDate().getTime() + 60 * 60 * 1000 < System.currentTimeMillis() || i++ > 5000)) {
                    LOGGER.info("The connect closed more than 1 hour or size more than 5000, remove it :{}", state.getConnectId());
                    hVxState.remove(state.getConnectId());
                }
            }
        });
    }
}
