package com.zoe.framework.regex;

/**
 * Created by Administrator on 2016/7/27.
 */
public class Debug {

    public static void Assert(boolean condition, String message) {
        if (condition)
            return;
        System.out.println(message);
    }
}
