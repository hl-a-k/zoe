package com.zoe.framework.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL注入过滤
 *
 * @author caizhicong
 */
public final class SqlFilter {

    static final String regex = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"
            + "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";

    static final Pattern sqlPattern = Pattern.compile(regex,
            Pattern.CASE_INSENSITIVE);

    /**
     * SQL 特殊字符过滤,防SQL注入
     *
     * @param input 进行注入过滤的字符串
     * @return
     */
    public static String filter(String input) {
        return filter(input, false);
    }

    /**
     * SQL 特殊字符过滤,防SQL注入
     *
     * @param input          进行注入过滤的字符串
     * @param setNullIfFound 如果发现注入就返回null
     * @return
     */
    public static String filter(String input, boolean setNullIfFound) {
        if (StringUtils.isBlank(input))
            return StringUtils.EMPTY;
        Matcher matcher = sqlPattern.matcher(input);
        if (matcher.find()) {
            if (setNullIfFound) {
                return null;
            }
            input = matcher.replaceAll("");
        }
        return input;
    }

    public static void main(String[] args) {
        System.out.println(String.format("select=%s", filter("select")));
        System.out.println(String.format("select1=%s", filter("select1")));
        System.out.println(String.format("1select=%s", filter("1select")));
        System.out.println(String.format("1 select=%s", filter("1 select")));
        System.out.println(String.format("select 1=%s", filter("select 1")));
        System.out.println(String.format("--select=%s", filter("--select")));
        System.out.println(String.format("-- select=%s", filter("-- select")));
        System.out.println(String.format("/**/select=%s", filter("/**/select")));
    }
}
