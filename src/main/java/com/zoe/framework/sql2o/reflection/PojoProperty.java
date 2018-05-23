package com.zoe.framework.sql2o.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * PojoProperty
 * Created by caizhicong on 2015/5/27.
 */
public class PojoProperty {
    public final String name;
    public final Class<?> type;
    private Method getMethod;
    private Method setMethod;
    private Field field;
    private String columnName;

    PojoProperty(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    public void setGetMethod(Method getMethod) {
        this.getMethod = getMethod;
    }

    public void setSetMethod(Method setMethod) {
        this.setMethod = setMethod;
    }

    public Method getGetMethod() {
        return getMethod;
    }

    public Method getSetMethod() {
        return setMethod;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Object get(Object instance) {
        if (getMethod != null) {
            try {
                return getMethod.invoke(instance, (Object[]) null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void set(Object instance, Object value) {
        if (setMethod != null) {
            try {
                setMethod.invoke(instance, value);
                return;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public String columnName() {
        return columnName;
    }

    public void columnName(String columnName) {
        this.columnName = columnName;
    }
}
