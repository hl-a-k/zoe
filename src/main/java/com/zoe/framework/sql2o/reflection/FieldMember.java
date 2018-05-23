package com.zoe.framework.sql2o.reflection;

import com.zoe.framework.sql2o.Sql2oException;

import java.lang.reflect.Field;

/**
 * Field Member
 * used internally to get or set property values directly into the field. Only used if no setter method is found.
 * Created by caizhicong on 2016/10/3.
 */
public class FieldMember implements IMember {

    private Field field;

    public FieldMember(Field field) {
        this.field = field;
        this.field.setAccessible(true);
    }

    public Object getProperty(Object obj) {
        try {
            return this.field.get(obj);
        } catch (IllegalAccessException e) {
            throw new Sql2oException("could not get field " + this.field.getName() + " on class " + obj.getClass().toString(), e);
        }
    }

    public void setProperty(Object obj, Object value) {
        if (value == null && this.field.getType().isPrimitive()) {
            return; // don't try set null to a primitive field
        }

        try {
            this.field.set(obj, value);
        } catch (IllegalAccessException e) {
            throw new Sql2oException("could not set field " + this.field.getName() + " on class " + obj.getClass().toString(), e);
        }
    }

    public Class getType() {
        return field.getType();
    }
}
