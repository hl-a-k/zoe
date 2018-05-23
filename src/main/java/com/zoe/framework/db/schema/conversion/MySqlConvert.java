package com.zoe.framework.db.schema.conversion;

import com.zoe.framework.db.schema.DbType;

/**
 * Created by caizhicong on 2016/8/18.
 */
public class MySqlConvert {
    /**
     * Gets the type of the db.
     *
     * @param sqlType Type of MySQL.
     * @return DbType
     */
    public static DbType GetDbType(String sqlType) {
        switch (sqlType.toLowerCase()) {
            case "int":
                return DbType.INT;
            case "integer":
                return DbType.INTEGER;
            case "tinyint":
                return DbType.TINYINT;
            case "smallint":
                return DbType.SMALLINT;
            case "mediumint":
                return DbType.MEDIUMINT;
            case "bigint":
                return DbType.BIGINT;
            case "real":
                return DbType.REAL;
            case "float":
                return DbType.FLOAT;
            case "double":
                return DbType.DOUBLE;
            case "decimal":
                return DbType.DECIMAL;
            case "numeric":
                return DbType.NUMERIC;
            case "date":
                return DbType.DATE;
            case "time":
                return DbType.TIME;
            case "year":
                return DbType.YEAR;
            case "datetime":
                return DbType.DATETIME;
            case "timestamp":
                return DbType.TIMESTAMP;
            case "char":
                return DbType.CHAR;
            case "varchar":
                return DbType.VARCHAR;
            case "text":
                return DbType.TEXT;
            case "tinytext":
                return DbType.TINYTEXT;
            case "mediumtext":
                return DbType.MEDIUMTEXT;
            case "longtext":
                return DbType.LONGTEXT;
            case "blob":
                return DbType.BLOB;
            case "tinyblob":
                return DbType.TINYBLOB;
            case "mediumblob":
                return DbType.MEDIUMBLOB;
            case "longblob":
                return DbType.LONGBLOB;
            case "bit":
                return DbType.BIT;
            case "binary":
                return DbType.BINARY;
            case "varbinary":
                return DbType.VARBINARY;
            default:
                return DbType.VARCHAR;
        }
    }

    /**
     * Gets the type of the native.
     *
     * @param dbType Type of the db.
     * @return db native type
     */
    public static String GetNativeType(DbType dbType) {
        switch (dbType) {
            case INT:
                return "int";
            case INTEGER:
                return "integer";
            case TINYINT:
                return "tinyint";
            case SMALLINT:
                return "smallint";
            case MEDIUMINT:
                return "mediumint";
            case BIGINT:
                return "bigint";
            case REAL:
                return "real";
            case FLOAT:
                return "float";
            case DOUBLE:
                return "double";
            case DECIMAL:
                return "decimal";
            case NUMERIC:
                return "numeric";
            case DATE:
                return "date";
            case TIME:
                return "time";
            case YEAR:
                return "year";
            case DATETIME:
                return "datetime";
            case TIMESTAMP:
                return "timestamp";
            case CHAR:
                return "char";
            case VARCHAR:
                return "varchar";
            case TEXT:
                return "text";
            case TINYTEXT:
                return "tinytext";
            case MEDIUMTEXT:
                return "mediumtext";
            case LONGTEXT:
                return "longtext";
            case BLOB:
                return "blob";
            case TINYBLOB:
                return "tinyblob";
            case MEDIUMBLOB:
                return "mediumblob";
            case LONGBLOB:
                return "longblob";
            case BIT:
                return "bit";
            case BINARY:
                return "binary";
            case VARBINARY:
                return "varbinary";
            default:
                return "varchar";
        }
    }
}
