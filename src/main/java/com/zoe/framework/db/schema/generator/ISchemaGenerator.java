package com.zoe.framework.db.schema.generator;

import com.zoe.framework.db.schema.Column;
import com.zoe.framework.db.schema.DbType;
import com.zoe.framework.db.schema.IStoredProcedure;
import com.zoe.framework.db.schema.Table;

import java.util.List;

/**
 * Created by caizhicong on 2016/5/14.
 */
public interface ISchemaGenerator {
    String getClientName();

    void setClientName(String value);

    String getProviderName();

    //SQL formatting
    String QualifyTableName(Table tbl);

    String QualifyColumnName(Column column);

    String QualifySPName(IStoredProcedure sp);

    String QualifyName(String name);

    /**
     * Builds a CREATE TABLE statement.
     *
     * @param table
     * @return
     */
    String BuildCreateTableStatement(Table table);

    /**
     * Builds a DROP TABLE statement.
     *
     * @param tableName Name of the table.
     * @return
     */
    String BuildDropTableStatement(String tableName);

    /**
     * Adds the column.
     *
     * @param tableName Name of the table.
     * @param column    The column.
     */
    String BuildAddColumnStatement(String tableName, Column column);

    /**
     * Alters the column.
     *
     * @param column The column.
     */
    String BuildAlterColumnStatement(Column column);

    /**
     * Removes the column.
     *
     * @param tableName  Name of the table.
     * @param columnName Name of the column.
     * @return
     */
    String BuildDropColumnStatement(String tableName, String columnName);

    /**
     * Generates the columns.
     *
     * @param table Table containing the columns.
     * @return SQL fragment representing the supplied columns.
     */
    String GenerateColumns(Table table);

    /**
     * Sets the column attributes.
     *
     * @param column The column.
     * @return
     */
    String GenerateColumnAttributes(Column column);

    /**
     * Adds the index.
     *
     * @param tableName Name of the table.
     * @param column    The column.
     */
    String BuildAddIndexStatement(String tableName, Column column, String indexName);

    /**
     * Adds the index.
     *
     * @param tableName Name of the table.
     * @param columns   The columns.
     */
    String BuildAddIndexStatement(String tableName, List<Column> columns, String indexName);

    /**
     * Gets the type of the native.
     *
     * @param dbType Type of the db.
     * @return
     */
    String GetNativeType(DbType dbType);

    /**
     * Get DbType from the type of the native
     *
     * @param sqlType
     * @return
     */
    DbType GetDbType(String sqlType);
}