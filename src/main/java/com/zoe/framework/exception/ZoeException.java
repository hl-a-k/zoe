package com.zoe.framework.exception;

/**
 * 框架异常基类
 *
 * Created by caizhicong on 2016/2/3.
 */
public class ZoeException extends RuntimeException {

    public ZoeException() {
        super();
    }

    public ZoeException(String message) {
        super(message);
    }
}
