package com.zoe.framework.db.schema.utils;

import com.zoe.framework.db.schema.DbType;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;

/**
 * sql类型转换工具类
 * Created by caizhicong on 2016/2/23.
 */
public class SqlTypeUtils {

    public static Class<?> getJavaType(int sqlType) {
        switch (sqlType) {
            case Types.INTEGER:
                return Integer.class;
            case Types.BIGINT:
                return Long.class;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return String.class;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return Date.class;
            case Types.DOUBLE:
                return Double.class;
            case Types.REAL:
            case Types.FLOAT:
                return Float.class;
            case Types.DECIMAL:
            case Types.NUMERIC:
                return BigDecimal.class;
            case Types.SMALLINT:
                return Short.class;
            case Types.TINYINT:
                return Byte.class;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return Byte[].class;
        }
        return String.class;
    }

    public static Class<?> getJavaType(DbType dbType) {
        switch (dbType) {
            case INT:
            case INTEGER:
            case MEDIUMINT:
                return Integer.class;
            case TINYINT:
                return Byte.class;
            case SMALLINT:
                return Short.class;
            case BIGINT:
                return Long.class;
            case REAL:
            case FLOAT:
                return Float.class;
            case DOUBLE:
                return Double.class;
            case DECIMAL:
            case NUMERIC:
                return BigDecimal.class;
            case DATE:
            case TIME:
            case DATETIME:
            case TIMESTAMP:
                return Date.class;
            case YEAR:
                return Integer.class;
            case CHAR:
            case VARCHAR:
            case NVARCHAR:
            case LONGVARCHAR:
            case LONGNVARCHAR:
            case TEXT:
            case TINYTEXT:
            case MEDIUMTEXT:
            case LONGTEXT:
                return String.class;
            case BIT:
                return Boolean.class;
            case BINARY:
            case VARBINARY:
            case LONGVARBINARY:
            case BLOB:
            case TINYBLOB:
            case MEDIUMBLOB:
            case LONGBLOB:
                return Byte[].class;
        }
        return String.class;
    }
}
