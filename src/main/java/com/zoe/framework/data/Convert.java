package com.zoe.framework.data;

/**
 * Created by Administrator on 2016/10/2.
 */
public class Convert {
    public static double toDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public static String toString(int i) {
        return i + "";
    }
}
