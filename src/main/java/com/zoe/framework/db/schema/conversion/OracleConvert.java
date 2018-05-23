package com.zoe.framework.db.schema.conversion;

import com.zoe.framework.db.schema.DbType;

/**
 * Created by caizhicong on 2016/8/18.
 */
public class OracleConvert {
    /**
     * Gets the type of the db.
     *
     * @param sqlType Type of MySQL.
     * @return DbType
     */
    public static DbType GetDbType(String sqlType) {
        switch (sqlType.toLowerCase()) {
            case "char":
                return DbType.CHAR;
            case "varchar2":
                return DbType.VARCHAR;
            case "nvarchar2":
                return DbType.NVARCHAR;
            case "date":
                return DbType.DATE;
            case "timestamp":
                return DbType.TIMESTAMP;
            case "number":
                return DbType.DECIMAL;
            case "float":
                return DbType.FLOAT;
            case "long":
                return DbType.BIGINT;
            case "clob":
            case "nclob":
                return DbType.CLOB;//DbType.BINARY
            case "raw":
            case "long raw":
            case "blob":
                return DbType.BLOB;
            default:
                //For whatever reason, Oracle9i (+ others?) stores the
                //precision with certain datatypes. Ex: "timestamp(3)"
                //So having "timestamp" as a case statement will not work.
                if (sqlType.startsWith("timestamp")) {
                    return DbType.DATE;
                } else if (sqlType.startsWith("interval")) {
                    return DbType.VARCHAR; //No idea how to handle this one
                } else {
                    return DbType.VARCHAR;
                }
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
            case CHAR:
                return "char";
            case VARCHAR:
                return "varchar2";
            case NVARCHAR:
                return "nvarchar2";
            case DATE:
                return "date";
            case TIMESTAMP:
                return "timestamp";
            case DECIMAL:
                return "number";
            case FLOAT:
                return "float";
            case BIGINT:
                return "long";
            case CLOB:
                return "clob";
            case BLOB:
                return "blob";
            default:
                return "varchar2";
        }
    }
}
