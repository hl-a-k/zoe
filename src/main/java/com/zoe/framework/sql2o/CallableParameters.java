package com.zoe.framework.sql2o;

import java.util.ArrayList;
import java.util.List;

/**
 * 存储过程参数
 * Created by caizhicong on 2016/2/19.
 */
public class CallableParameters {

    private List<CallableParameter> parameterList = new ArrayList<>();

    public static CallableParameters New() {
        return new CallableParameters();
    }

    public List<CallableParameter> getParameters() {
        return parameterList;
    }

    public int size() {
        return parameterList.size();
    }

    public CallableParameter getParameter(int index) {
        return parameterList.get(index - 1);
    }

    public boolean add(CallableParameter value) {
        return parameterList.add(value);
    }

    public CallableParameters addParameter(Class parameterType, Object value) {
        this.addParameter(parameterType, value, false);
        return this;
    }

    public CallableParameters addParameter(Class parameterType, Object value, boolean isOutParameter) {
        CallableParameter parameter = new CallableParameter(parameterType, value, isOutParameter);
        parameterList.add(parameter);
        parameter.setIndex(parameterList.size());
        parameter.setName("p" + parameter.getIndex());
        return this;
    }
}
