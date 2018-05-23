package com.zoe.framework.util;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.zoe.framework.util.DateUtil.DateType.*;

public class DateUtil {
    private static Calendar calendar = Calendar.getInstance();

    public DateUtil() {
    }

    public static Date trunc(Date date) throws Exception {
        return toDate(toStringFormat(date, "yyyy-MM-dd"), "yyyy-MM-dd");
    }

    public static String toShortStringFormat(Date date) throws Exception {
        return toStringFormat(date, "yyyy-MM-dd");
    }

    public static String toLongStringFormat(Date date) throws Exception {
        return toStringFormat(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String toStringFormat(Date date, String pattern) throws Exception {
        try {
            SimpleDateFormat e = new SimpleDateFormat(pattern);
            return e.format(date);
        } catch (Exception var3) {
            throw new Exception("日期转换成文本格式失败！");
        }
    }

    public static String tryToString(Date date, String pattern) {
        if (date == null) return "";
        String defaultPattern = "yyyy-MM-dd HH:mm:ss";
        if (pattern == null) pattern = defaultPattern;
        try {
            SimpleDateFormat e = new SimpleDateFormat(pattern);
            return e.format(date);
        } catch (Exception var3) {
            var3.printStackTrace();
            SimpleDateFormat e = new SimpleDateFormat(defaultPattern);
            return e.format(date);
        }
    }

    public static Date getDate(String str) {
        try {
            return toDate(str, "yyyy-MM-dd HH:mm:ss");
        } catch (Exception ex) {
            return null;
        }
    }

    public static Date toDate(String str, String pattern) throws Exception {
        try {
            SimpleDateFormat e = new SimpleDateFormat(pattern);
            return e.parse(str);
        } catch (Exception var3) {
            throw new Exception("日期转换格式失败！");
        }
    }
    public static Date tryToDate(String str) {
        return tryToDate(str, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date tryToDate(String str, String pattern)  {
        try {
            SimpleDateFormat e = new SimpleDateFormat(pattern);
            return e.parse(str);
        } catch (Exception var3) {
            return null;
        }
    }

    public static long betweenDays(Date start, Date end) throws Exception {
        return between(start, end, Days);
    }

    public static long between(Date start, Date end, DateType dateType) throws Exception {
        long dividend;
        switch (dateType) {
            case Days:
                dividend = 86400000L;
                break;
            case Hours:
                dividend = 3600000L;
                break;
            case Minutes:
                dividend = 60000L;
                break;
            case Seconds:
                dividend = 1000L;
                break;
            case Milliseconds:
                dividend = 1L;
                break;
            default:
                throw new Exception("无效的日期类型枚举值！");
        }

        return (end.getTime() - start.getTime()) / dividend;
    }

    public static Date addYears(Date date, int years) throws Exception {
        return add(date, years, 1);
    }

    public static Date addMonths(Date date, int months) throws Exception {
        return add(date, months, 2);
    }

    public static Date addDays(Date date, int days) throws Exception {
        return add(date, days, 6);
    }

    public static Date add(Date date, int adds, int field) throws Exception {
        calendar.setTime(date);

        try {
            calendar.add(field, adds);
        } catch (Exception var4) {
            throw new Exception("无效的日期加减类型！");
        }

        return calendar.getTime();
    }

    public enum DateType {
        Days,
        Hours,
        Minutes,
        Seconds,
        Milliseconds
    }
}

