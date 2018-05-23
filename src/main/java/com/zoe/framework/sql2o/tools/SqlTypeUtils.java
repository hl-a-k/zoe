package com.zoe.framework.sql2o.tools;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;

/**
 * Created by caizhicong on 2016/2/23.
 */
public class SqlTypeUtils {
    public static <V> int getSqlType(Class<V> returnType) {
        if (returnType == Integer.class || returnType == int.class) {
            return Types.INTEGER;
        } else if (returnType == Long.class || returnType == long.class) {
            return Types.BIGINT;
        } else if (returnType == String.class) {
            return Types.VARCHAR;
        } else if (returnType == Date.class || returnType == java.sql.Date.class
                || returnType == java.sql.Timestamp.class || returnType == java.sql.Time.class) {
            return Types.DATE;
        } else if (returnType == Double.class || returnType == double.class) {
            return Types.DOUBLE;
        } else if (returnType == Float.class || returnType == float.class) {
            return Types.FLOAT;
        } else if (returnType == Short.class || returnType == short.class) {
            return Types.SMALLINT;
        } else if (returnType == Byte.class || returnType == byte.class) {
            return Types.TINYINT;
        } else if (returnType == BigDecimal.class) {
            return Types.DECIMAL;
        }
        return Types.VARCHAR;
    }

    public static Class<?> getJavaType(int sqlType) {
        switch (sqlType) {
            case Types.INTEGER:
                return Integer.class;
            case Types.BIGINT:
                return Long.class;
            case Types.VARCHAR:
                return String.class;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return Date.class;
            case Types.DOUBLE:
                return Double.class;
            case Types.FLOAT:
                return Float.class;
            case Types.DECIMAL:
            case Types.NUMERIC:
                return BigDecimal.class;
            case Types.SMALLINT:
                return Short.class;
            case Types.TINYINT:
                return Byte.class;
        }
        return String.class;
    }
}
