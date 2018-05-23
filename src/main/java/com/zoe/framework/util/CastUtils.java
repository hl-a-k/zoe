package com.zoe.framework.util;

import com.alibaba.fastjson.util.TypeUtils;

/**
 * 类型转化帮助类
 *
 * @author caizhicong
 */
public class CastUtils {

    public static <T> T cast(Object obj, Class<T> clazz) {
        return cast(obj, clazz, getDefaultValue(clazz));
    }

    public static <T> T cast(Object obj, Class<T> clazz, T defaultValue) {
        if (obj != null) {
            try {
                T value = TypeUtils.castToJavaBean(obj, clazz);
                if (value == null) {
                    return defaultValue;
                }
                return value;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDefaultValue(Class<T> clazz) {
        if (clazz == boolean.class || clazz == Boolean.class) {
            return (T) TypeUtils.castToBoolean(false);
        }

        if (clazz == byte.class || clazz == Byte.class) {
            return (T) TypeUtils.castToByte(0);
        }

        if (clazz == short.class || clazz == Short.class) {
            return (T) TypeUtils.castToShort(0);
        }

        if (clazz == int.class || clazz == Integer.class) {
            return (T) TypeUtils.castToInt(0);
        }

        if (clazz == long.class || clazz == Long.class) {
            return (T) TypeUtils.castToLong(0);
        }

        if (clazz == float.class || clazz == Float.class) {
            return (T) TypeUtils.castToFloat(0);
        }

        if (clazz == double.class || clazz == Double.class) {
            return (T) TypeUtils.castToDouble(0);
        }

        if (clazz == String.class) {
            return null;
        }
        return null;
    }
}
