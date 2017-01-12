package cd.blog.humbird.vertx.vx.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static String toStr(Date dt, String fmt) {
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(dt);
    }

    public static Date toDate(String str, String fmt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(fmt);
            return sdf.parse(str);
        } catch (Exception ex) {
            return null;
        }

    }

    public static String toStr(Date dt) {
        if (dt == null) {
            return "";
        }
        return toStr(dt, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date toDate(String str) {
        return toDate(str, "yyyy-MM-dd HH:mm:ss");
    }
}
