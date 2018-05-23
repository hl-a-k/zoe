package com.zoe.framework.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Administrator on 2016/10/2.
 */
public class DateUtil {
    public static String DEFFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static TimeZone defaultTimeZone  = TimeZone.getDefault();
    public static Locale defaultLocale    = Locale.getDefault();

    public static Date getDate(Object value) {
        String strVal = (String) value;

        if (strVal.startsWith("/Date(") && strVal.endsWith(")/")) {
            String dotnetDateStr = strVal.substring(6, strVal.length() - 2);
            strVal = dotnetDateStr;
        }

        String format;
        if (strVal.length() == DEFFAULT_DATE_FORMAT.length()) {
            format = DEFFAULT_DATE_FORMAT;
        } else if (strVal.length() == 10) {
            format = "yyyy-MM-dd";
        } else {
            format = "yyyy-MM-dd HH:mm:ss.SSS";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(format, defaultLocale);
        dateFormat.setTimeZone(defaultTimeZone);
        try {
            return dateFormat.parse(strVal);
        } catch (ParseException e) {
            throw new RuntimeException("can not cast to Date, value : " + strVal);
        }
    }

    public static String format(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, defaultLocale);
        dateFormat.setTimeZone(defaultTimeZone);
        return dateFormat.format(date);
    }
}
