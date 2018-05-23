package com.zoe.framework.data;

/**
 * 数据异常基类
 * Created by Administrator on 2016/10/2.
 */
public class DataException extends RuntimeException {

    public DataException(String message) {
        super(message);
    }
}
