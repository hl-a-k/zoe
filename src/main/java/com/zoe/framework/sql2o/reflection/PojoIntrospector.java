package com.zoe.framework.sql2o.reflection;

import static java.lang.reflect.Modifier.isPrivate;
import static java.lang.reflect.Modifier.isStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;

import com.zoe.framework.sql2o.logging.LocalLoggerFactory;
import com.zoe.framework.sql2o.logging.Logger;
import com.zoe.framework.sql2o.tools.AbstractCache;

/**
 * User: dimzon Date: 4/9/14 Time: 1:10 AM
 */

// TODO: move introspection code from PojoMetadata to PojoIntrospector

@SuppressWarnings("UnusedDeclaration")
public class PojoIntrospector {

    private final static Logger logger = LocalLoggerFactory
            .getLogger(PojoIntrospector.class);

    private static final AbstractCache<Class<?>, Map<String, PojoProperty>, Void> pCache = new AbstractCache<Class<?>, Map<String, PojoProperty>, Void>() {
        @Override
        protected Map<String, PojoProperty> evaluate(Class<?> key, Void param) {
            return collectReadableProperties(key);
        }
    };

    public static Map<String, PojoProperty> collectProperties(Class<?> cls) {
        return pCache.get(cls, null);
    }

    private static Map<String, PojoProperty> collectReadableProperties(Class<?> cls) {
        Map<String, PojoProperty> map = new HashMap<String, PojoProperty>();
        List<Class<?>> classList = classInheritanceHierarhy(cls, Object.class);
        for (Class<?> aClass : classList) {
            collectReadableProperties(map, aClass);
        }
        return Collections.unmodifiableMap(map);
    }

    private static boolean isStaticOrPrivate(Member m) {
        final int modifiers = m.getModifiers();
        return isStatic(modifiers) || isPrivate(modifiers);
    }

    private static void collectReadableProperties(Map<String, PojoProperty> map,
                                          Class<?> cls) {
        //Method[] methods = cls.getDeclaredMethods();
        //Field[] fields = cls.getDeclaredFields();
        for (final Method m : cls.getDeclaredMethods()) {
            //if (isStaticOrPrivate(m)) continue;
            if (isStatic(m.getModifiers())) continue;
            if (1 != m.getParameterTypes().length)// get=0,set=1
                continue;
            Class<?> returnType = m.getReturnType();
            if (returnType == Object.class || returnType.getName().equals("groovy.lang.MetaClass")) {
                continue;
            }
            Class<?> propType = m.getParameterTypes()[0];
            if (returnType == Void.TYPE && propType.getName().equals("groovy.lang.MetaClass")) {
                continue;
            }

            String name = m.getName();
            String propName = null;
            String originalPropName = null;
            boolean isBool = false;
            if (name.startsWith("set") && name.length() > 3) {
                originalPropName = name.substring(3);
                propName = decapitalize(originalPropName, true);
                if (propType == Boolean.TYPE) {
                    isBool = true;
                }
            }
            if (propName == null) continue;

            String getMethodName = (isBool ? "is" : "get") + originalPropName;
            try {
                Field field;
                try {
                    field = cls.getDeclaredField(propName);
                } catch (NoSuchFieldException e) {
                    propName = decapitalize(originalPropName, false);
                    field = cls.getDeclaredField(propName);
                }
                if (map.containsKey(propName))
                    continue;

                m.setAccessible(true);
                Method getMethod;
                getMethod = cls.getDeclaredMethod(getMethodName);
                getMethod.setAccessible(true);

                PojoProperty rp = new PojoProperty(propName, propType);
                rp.setGetMethod(getMethod);
                rp.setSetMethod(m);
                rp.setField(field);
                map.put(propName, rp);
            } catch (NoSuchMethodException | NoSuchFieldException e) {
                //e.printStackTrace();
                logger.warn(String.format("实体方法：%s 或 字段：%s 不存在", getMethodName, propName));
            }
        }

        for (final Field m : cls.getDeclaredFields()) {
            //if (isStaticOrPrivate(m)) continue;
            if(isStatic(m.getModifiers())) continue;
            String propName = m.getName();
            if (map.containsKey(propName)) continue;
            Class<?> returnType = m.getType();
            m.setAccessible(true);
            PojoProperty rp = new PojoProperty(propName, returnType);
            rp.setField(m);
            map.put(propName, rp);
        }
    }

    public static String decapitalize(String name, boolean staysUpper) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (staysUpper && name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
                Character.isUpperCase(name.charAt(0))){
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    private static List<Class<?>> classInheritanceHierarhy(Class<?> cls,
                                                           Class<Object> stopAt) {
        ArrayList<Class<?>> list = new ArrayList<Class<?>>();
        while (cls != null && cls != stopAt) {
            list.add(cls);
            cls = cls.getSuperclass();
        }
        return list;
    }
}
