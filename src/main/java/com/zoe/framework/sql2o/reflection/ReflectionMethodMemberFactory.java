package com.zoe.framework.sql2o.reflection;

import java.lang.reflect.Method;

/**
 * Created by caizhicong on 2016/10/3.
 *
 * @author dimzon
 * @author mdelapenya
 * @author caizhicong
 */
public class ReflectionMethodMemberFactory implements MethodMemberFactory{
    @Override
    public IMember newMember(Method getMethod,Method setMethod) {
        return new MethodMember(getMethod, setMethod);
    }
}
