package com.zoe.framework.sqlbag;
/**
 * Created by Administrator on 2015/6/23.
 */
public class BuilderException extends RuntimeException{

    public BuilderException() {
        super();
    }

    public BuilderException(String message) {
        super(message);
    }

    public BuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuilderException(Throwable cause) {
        super(cause);
    }
}
