package com.zoe.framework.sql2o.reflection;

import java.lang.reflect.Field;

/**
 * Created by caizhicong on 2016/10/3.
 *
 * @author dimzon
 * @author mdelapenya
 * @author caizhicong
 */
public class ReflectionFieldMemberFactory implements FieldMemberFactory{
    @Override
    public IMember newMember(Field field) {
        return new FieldMember(field);
    }
}
