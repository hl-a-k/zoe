package com.zoe.framework.validation.results;

/**
 * Created by z_wu on 2014/12/4.
 */
public class ValidationFailure{
    private Integer errorCode;
    private String _propertyName;
    private String _errorMessage;
    private Object _attemptedValue;

    public ValidationFailure(String propertyName, String error) {
        this(propertyName, error, null);
    }

    public ValidationFailure(String propertyName, String error, Object attemptedValue) {
        _propertyName = propertyName;
        _errorMessage = error;
        _attemptedValue = attemptedValue;
    }

    public ValidationFailure(String propertyName, String error, Object attemptedValue,Integer errorCode) {
        _propertyName = propertyName;
        _errorMessage = error;
        _attemptedValue = attemptedValue;
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String propertyName(){
        return this._propertyName;
    }

    public String errorMessage(){
        return this._errorMessage;
    }

    public Object attemptedValue(){
        return this._attemptedValue;
    }

    /*private Object _customState;
    public Object customState(){
        return this._customState;
    }*/

    public String toString() {
        return _errorMessage;
    }

}
