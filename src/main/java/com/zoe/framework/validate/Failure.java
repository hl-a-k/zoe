package com.zoe.framework.validate;


/**
 * @author lpw
 */
public enum Failure {
    /**
     * sql 执行异常。
     */
    SqlError("com.zoe.framework.validate.sql-error", 9995),
    /**
     * 参数校验失败。
     */
    ValidateError("com.zoe.framework.validate.not-valid", 9996),
    /**
     * 无权限。
     */
    NotPermit("com.zoe.framework.validate.not-permit", 9997),
    /**
     * 系统繁忙。
     */
    Busy("com.zoe.framework.validate.busy", 9998),
    /**
     * 运行期异常。
     */
    Exception("com.zoe.framework.validate.exception", 9999);

    private String messageKey;
    private int errorCode;

    Failure(String messageKey, int errorCode) {
        this.messageKey = messageKey;
        this.errorCode = errorCode;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
