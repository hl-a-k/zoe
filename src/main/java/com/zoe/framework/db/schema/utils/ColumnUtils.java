package com.zoe.framework.db.schema.utils;

import com.zoe.framework.db.schema.Column;
import com.zoe.framework.db.schema.DbType;

import java.sql.Types;
import java.util.List;

/**
 * Created by caizhicong on 2016/3/24.
 */
public class ColumnUtils {

    public enum Language {
        Java, CSharp
    }

    public static void setMappedDataType(List<Column> columns, Language lng) {
        for (Column column : columns) {
            column.setMappedDataType(getMappedDataType(column, lng));
        }
    }

    private static String getMappedDataType(Column column, Language lng) {
        DbType dbType = column.getDbType();
        if (lng == Language.Java) {
            String type = SqlTypeUtils.getJavaType(dbType).getSimpleName();
            switch (dbType) {
                case DECIMAL:
                case NUMERIC:
                    if (column.getScale() == 0) {
                        if (column.getLength() > 10) {
                            return "Long";
                        }
                        return "Integer";
                    }
                    return "BigDecimal";
            }
            return type;
        } else if (lng == Language.CSharp) {
            switch (dbType) {
                case INTEGER:
                    return "int";
                case NUMERIC:
                case DECIMAL:
                    return "decimal";
                case BIGINT:
                    return "long";
                case VARCHAR:
                    return "string";
                case DATE:
                case TIMESTAMP:
                case DATETIME:
                    return "DateTime";
                case DOUBLE:
                    return "double";
                case FLOAT:
                    return "float";
                case SMALLINT:
                    return "short";
                case TINYINT:
                    return "byte";
            }
        }
        return null;
    }
}
