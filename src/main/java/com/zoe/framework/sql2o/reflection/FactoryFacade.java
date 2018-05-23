package com.zoe.framework.sql2o.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("Unsafe")
public class FactoryFacade {
    private final static FactoryFacade instance;

    static {
        MethodMemberFactory m;
        ObjectConstructorFactory o;
        try {
            m = (MethodMemberFactory) Class
                    .forName("com.zoe.framework.sql2o.reflection.MethodAccessorsGenerator")
                    .newInstance();
            o = (ObjectConstructorFactory) m;
        } catch (Throwable ex) {
            m = new ReflectionMethodMemberFactory();
            o = null;
        }
        FieldMemberFactory f;
        try {
            Class cls = Class.forName("com.zoe.framework.sql2o.reflection.UnsafeFieldMemberFactory");
            f = (FieldMemberFactory) cls.newInstance();
            if(o==null) o = (ObjectConstructorFactory) f;
        } catch (Throwable ex) {
            f = new ReflectionFieldMemberFactory();
            o = new ReflectionObjectConstructorFactory();
        }
        instance = new FactoryFacade(f, m, o);
    }

    private final FieldMemberFactory fieldMemberFactory;
    private final MethodMemberFactory methodMemberFactory;
    private final ObjectConstructorFactory objectConstructorFactory;

    public FactoryFacade(
            FieldMemberFactory fieldMemberFactory, MethodMemberFactory methodMemberFactory,
        ObjectConstructorFactory objectConstructorFactory) {

        this.fieldMemberFactory = fieldMemberFactory;
        this.methodMemberFactory = methodMemberFactory;
        this.objectConstructorFactory = objectConstructorFactory;
    }

    public static FactoryFacade getInstance() {
        return instance;
    }

    public IMember newMember(Field field) {
        return fieldMemberFactory.newMember(field);
    }

    public IMember newMember(Method getMethod,Method setMethod) {
        return methodMemberFactory.newMember(getMethod, setMethod);
    }

    public ObjectConstructor newConstructor(Class<?> cls) {
        return objectConstructorFactory.newConstructor(cls);
    }
}

