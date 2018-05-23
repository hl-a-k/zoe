package com.zoe.framework.db.schema.generator;

import com.zoe.framework.db.schema.Column;
import com.zoe.framework.db.schema.DbType;
import com.zoe.framework.db.schema.IStoredProcedure;
import com.zoe.framework.db.schema.Table;
import com.zoe.framework.db.schema.conversion.OracleConvert;
import com.zoe.framework.db.schema.generator.ANSISchemaGenerator;

/**
 * oracle schema 生成类
 * Created by caizhicong on 2016/5/14.
 */
public class OracleSchemaGenerator extends ANSISchemaGenerator {

    @Override
    public String QualifyTableName(Table tbl) {
        return null;
    }

    @Override
    public String QualifySPName(IStoredProcedure sp) {
        return null;
    }

    /**
     * Builds a CREATE TABLE statement.
     *
     * @param table
     * @return String
     */
    @Override
    public String BuildCreateTableStatement(Table table) {
        return null;
    }

    /**
     * Generates the columns.
     *
     * @param table Table containing the columns.
     * @return SQL fragment representing the supplied columns.
     */
    @Override
    public String GenerateColumns(Table table) {
        return null;
    }

    /**
     * Sets the column attributes.
     *
     * @param column The column.
     * @return String
     */
    @Override
    public String GenerateColumnAttributes(Column column) {
        return null;
    }

    @Override
    public DbType GetDbType(String sqlType) {
        return OracleConvert.GetDbType(sqlType);
    }

    /**
     * Gets the type of the native.
     *
     * @param dbType Type of the db.
     * @return String
     */
    @Override
    public String GetNativeType(DbType dbType) {
        return OracleConvert.GetNativeType(dbType);
    }
}
