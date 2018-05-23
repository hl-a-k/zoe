package com.zoe.framework.sql2o;

import com.zoe.framework.sql2o.util.CastUtils;

import java.util.Date;

/**
 * 存储过程参数
 * Created by caizhicong on 2016/2/19.
 */
public class CallableParameter {

    private String name;

    private int index;

    private boolean isOutParameter;

    private Object value;

    private Class parameterType;

    public CallableParameter(Class parameterType, Object value) {
        this(parameterType, value, false);
    }

    public CallableParameter(Class parameterType, Object value, boolean isOutParameter) {
        this.parameterType = parameterType;
        this.value = value;
        this.isOutParameter = isOutParameter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 索引从1开始
     * @return
     */
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Class getParameterType() {
        return parameterType;
    }

    public void setParameterType(Class parameterType) {
        this.parameterType = parameterType;
    }

    public boolean isOutParameter() {
        return isOutParameter;
    }

    public void setIsOutParameter(boolean isOutParameter) {
        this.isOutParameter = isOutParameter;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Integer getInt() {
        return CastUtils.cast(value, Integer.class, null);
    }

    public Long getLong() {
        return CastUtils.cast(value, Long.class, null);
    }

    public Double getDouble() {
        return CastUtils.cast(value, Double.class, null);
    }

    public String getString() {
        return CastUtils.cast(value, String.class, null);
    }

    public Date getDate() {
        return CastUtils.cast(value, Date.class, null);
    }
}
