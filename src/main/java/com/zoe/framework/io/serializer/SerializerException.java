package com.zoe.framework.io.serializer;

/**
 * Created by caizhicong on 2016/7/22.
 */
public class SerializerException extends RuntimeException {

    public SerializerException(Throwable cause) {
        super(cause);
    }

    public SerializerException(String message) {
        super(message);
    }

    public SerializerException(String message, Throwable cause) {
        super(message, cause);
    }
}
