package com.zoe.framework.validate;

import javax.servlet.http.HttpServletRequest;

/**
 * ValidateWrapper2
 * Created by caizhicong on 2017/7/31.
 */
public class ValidateWrapper2 {

    private HttpServletRequest request;
    private ValidateRequestMapping current;
    private ValidateRequestMapping parent;

    public ValidateWrapper2(HttpServletRequest request, ValidateRequestMapping current, ValidateRequestMapping parent) {
        this.request = request;
        this.current = current;
        this.parent = parent;
    }

    public ValidateRequestMapping getCurrent() {
        return current;
    }

    public ValidateRequestMapping getParent() {
        return parent;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public Integer getErrorCode(String propertyName) {
        if(current.validates().length > 0){
            for (Validate validate : current.validates()) {
                if(propertyName.equals(validate.parameter())){
                    return validate.failureCode();
                }
            }
        }
        //参数错误
        return Failure.ValidateError.getErrorCode();
    }
}
