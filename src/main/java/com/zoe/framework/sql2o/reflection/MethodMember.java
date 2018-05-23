package com.zoe.framework.sql2o.reflection;

import com.zoe.framework.sql2o.Sql2oException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Method Member
 * used internally to get or set property values via its getter method.
 * Created by caizhicong on 2016/10/3.
 *
 * @author mdelapenya
 * @author caizhicong
 */
public class MethodMember implements IMember{

    private Method getMethod;
    private Method setMethod;
    private Class<?> type;

    public MethodMember(Method getMethod,Method setMethod) {
        if (getMethod != null) {
            this.getMethod = getMethod;
            this.getMethod.setAccessible(true);
            type = getMethod.getReturnType();
        }
        if (setMethod != null) {
            this.setMethod = setMethod;
            this.setMethod.setAccessible(true);
            type = setMethod.getReturnType();
            if(type == null && setMethod.getParameterTypes().length > 0) {
                type = setMethod.getParameterTypes()[0];
            }
        }
    }

    public Method getGetMethod(){
        return getMethod;
    }

    public Method getSetMethod(){
        return setMethod;
    }

    public Object getProperty(Object obj) {
        try {
            return this.getMethod.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Sql2oException("error while calling getter method with name " + getMethod.getName() + " on class " + obj.getClass().toString(), e);
        }
    }

    public void setProperty(Object obj, Object value) {
        if (value == null && type.isPrimitive()){
            return; // don't try to set null to a setter to a primitive type.
        }
        try {

            this.setMethod.invoke(obj, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Sql2oException("error while calling setter method with name " + setMethod.getName() + " on class " + obj.getClass().toString(), e);
        }
    }

    public Class getType() {
        return type;
    }
}
