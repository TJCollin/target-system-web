package cn.collin.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Collin on 2017/4/25.
 */
public class TransTimestamp {
    public String stampToDate(String s){
        String formats = "yyyy-MM-dd HH:mm:ss";
        Long timestamp = Long.parseLong(s) * 1000;
        String date = new SimpleDateFormat(formats, Locale.CHINA).format(new Date(timestamp));
        return date;
    }
}
