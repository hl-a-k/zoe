package com.zoe.framework.sql2o.tools;

/**
 * Takes a string formatted like: 'my_string_variable' and returns it as:
 * 'myStringVariable'
 *
 * @author ryancarlson
 * @author dimzon - complete rewrite
 * @author caizhicong - add camelCaseToUnderscore
 */
public class CamelCaseUtils {

    public static String underscoreToCamelCase(String underscore) {
        if (underscore == null || underscore.isEmpty())
            return underscore;
        char[] chars = underscore.toCharArray();
        int write = -1, len = chars.length;
        boolean upper = false;
        for (int read = 0; read < len; ++read) {
            char c = chars[read];
            if ('_' == c) {
                upper = true;
                continue;
            }
            if (upper) {
                upper = false;
                chars[++write] = Character.toUpperCase(c);
            } else {
                chars[++write] = Character.toLowerCase(c);
            }
        }
        return new String(chars, 0, ++write);
    }

    public static String camelCaseToUnderscore(String camelCase) {
        if (camelCase == null || camelCase.isEmpty())
            return camelCase;
        char[] chars = camelCase.toCharArray();
        int write = -1, len = chars.length;
        char[] buffer = new char[len + 10];// 最多10个下划线
        for (int read = 0; read < len; ++read) {
            char c = chars[read];
            if (Character.isUpperCase(c)) {
                buffer[++write] = '_';
                buffer[++write] = Character.toLowerCase(c);
            } else {
                buffer[++write] = c;
            }
        }
        return new String(buffer, 0, ++write);
    }

    public static String toCamelCase(String text) {
        text = underscoreToCamelCase(text);
        return text;
    }

    /**
     * Converts text to pascal case...[ PascalCase ]
     *
     * @param text The text.
     * @return
     */
    public static String toPascalCase(String text) {
        text = underscoreToCamelCase(text);
        char[] chars = text.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars, 0, chars.length);
    }
}
