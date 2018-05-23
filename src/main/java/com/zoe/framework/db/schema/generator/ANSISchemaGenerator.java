package com.zoe.framework.db.schema.generator;

/**
 * Created by caizhicong on 2016/5/14.
 */

import com.zoe.framework.db.schema.Column;
import com.zoe.framework.db.schema.DbType;
import com.zoe.framework.db.schema.IStoredProcedure;
import com.zoe.framework.db.schema.Table;
import com.zoe.framework.db.schema.generator.ISchemaGenerator;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A schema generator for your DB
 */
public abstract class ANSISchemaGenerator implements ISchemaGenerator {
    protected String ADD_COLUMN;
    protected String ALTER_COLUMN;
    protected String CREATE_TABLE;
    protected String DROP_COLUMN;
    protected String DROP_TABLE;

    protected String UPDATE_DEFAULTS;
    protected String GET_COLUMNS;
    protected String ADD_INDEX;

    private String ClientName;
    private String ProviderName;

    public String getClientName() {
        return ClientName;
    }

    public void setClientName(String value) {
        ClientName = value;
    }

    public String getProviderName() {
        return ProviderName;
    }

    private void setProviderName(String value) {
        ProviderName = value;
    }

    public String QualifyTableName(Table tbl) {
        String qualifiedFormat = StringUtils.isBlank(tbl.getSchemaName()) ? "[%2$s]" : "[%1$s].[%2$s]";
        String qualifiedTable = String.format(qualifiedFormat, tbl.getSchemaName(), tbl.getName());
        return qualifiedTable;
    }

    public String QualifyColumnName(Column column) {
        String qualifiedFormat = StringUtils.isBlank(column.getTable().getSchemaName()) ? "[%2$s].[%3$s]" : "[%1$s].[%2$s].[%3$s]";
        return String.format(qualifiedFormat, column.getTable().getSchemaName(), column.getTable().getName(), column.getName());
    }

    public String QualifySPName(IStoredProcedure sp) {
        String qualifiedFormat = "[%1$s].[%2$s]";
        return String.format(qualifiedFormat, sp.getSchemaName(), sp.getName());
    }

    public String QualifyName(String name) {
        String qualifiedFormat = "[%1$s]";
        return String.format(qualifiedFormat, name);
    }

    /**
     * Builds a CREATE TABLE statement.
     *
     * @param table
     * @return
     */
    public String BuildCreateTableStatement(Table table) {
        String columnSql = GenerateColumns(table);
        return String.format(CREATE_TABLE, table.getName(), columnSql);
    }

    /**
     * Builds a DROP TABLE statement.
     *
     * @param tableName Name of the table.
     * @return
     */
    public String BuildDropTableStatement(String tableName) {
        return String.format(DROP_TABLE, tableName);
    }

    /**
     * Adds the column.
     *
     * @param tableName Name of the table.
     * @param column    The column.
     * @return
     */
    public String BuildAddColumnStatement(String tableName, Column column) {
        StringBuilder sql = new StringBuilder();

        //if we're adding a Non-null column to the DB schema, there has to be a default value
        //otherwise it will result in an error'
        if (!column.isNullable() && column.getDefaultSetting() == null) {
            SetColumnDefaults(column);
        }

        sql.append(String.format(ADD_COLUMN, tableName, column.getName(), GenerateColumnAttributes(column)));

        //if the column isn't nullable and there are records already
        //the default setting won't be honored and a null value could be entered (in SQLite for instance)
        //enforce the default setting here
        if (!column.isNullable()) {
            sql.append("\r\n");

            Object defaultValue = column.getDefaultSetting();

            if (column.getDbType().isString() || column.getDbType().isDate()) {
                defaultValue = String.format("'%1$s'", column.getDefaultSetting());
            }

            sql.append(String.format(UPDATE_DEFAULTS, tableName, column.getName(), defaultValue));
        }

        return sql.toString();
    }

    /**
     * Alters the column.
     *
     * @param column The column.
     */
    public String BuildAlterColumnStatement(Column column) {
        StringBuilder sql = new StringBuilder();
        sql.append(String.format(ALTER_COLUMN, column.getTable().getName(), column.getName(), GenerateColumnAttributes(column)));
        return sql.toString();
    }

    /**
     * Removes the column.
     *
     * @param tableName  Name of the table.
     * @param columnName Name of the column.
     * @return
     */
    public String BuildDropColumnStatement(String tableName, String columnName) {
        StringBuilder sql = new StringBuilder();
        sql.append(String.format(DROP_COLUMN, tableName, columnName));
        return sql.toString();
    }

    /**
     * Generates the columns.
     *
     * @param table Table containing the columns.
     * @return SQL fragment representing the supplied columns.
     */
    public String GenerateColumns(Table table) {
        StringBuilder createSql = new StringBuilder();

        for (Column col : table.getColumns()) {
            createSql.append(String.format("\r\n  [%1$s]%2$s,", col.getName(), GenerateColumnAttributes(col)));
        }
        String columnSql = createSql.toString();
        return Chop(columnSql, ",");
    }

    /**
     * Strips the last specified chars from a string.
     *
     * @param sourceString The source string.
     * @param backDownTo   The back down to.
     * @return
     */
    public String Chop(String sourceString, String backDownTo) {
        int removeDownTo = sourceString.lastIndexOf(backDownTo);
        int removeFromEnd = 0;
        if (removeDownTo > 0) {
            removeFromEnd = sourceString.length() - removeDownTo;
        }

        String result = sourceString;

        if (sourceString.length() > removeFromEnd - 1) {
            result = result.substring(0, removeDownTo) + result.substring(removeDownTo + removeFromEnd);
        }

        return result;
    }

    /**
     * Sets the column attributes.
     *
     * @param column The column.
     * @return
     */
    public abstract String GenerateColumnAttributes(Column column);

    @Override
    public String BuildAddIndexStatement(String tableName, Column column, String indexName) {
        return BuildAddIndexStatement(tableName, Arrays.asList(column), indexName);
    }

    @Override
    public String BuildAddIndexStatement(String tableName, List<Column> columns, String indexName) {
        StringBuilder sql = new StringBuilder();
        Column column = columns.get(0);
        String indexTypeAndName;
        if (column.isPrimaryKey()) {
            indexTypeAndName = "PRIMARY KEY";
        } else if (column.isUnique()) {
            indexTypeAndName = "UNIQUE";
        } else {
            if (StringUtils.isBlank(indexName)) {
                indexName = "idx";
                for (Column col : columns) {
                    indexName += "_" + col.getName();
                }
            }
            indexTypeAndName = "INDEX " + indexName;
        }
        List<String> columnNames = new ArrayList<>();
        for (Column col : columns) {
            columnNames.add(QualifyName(col.getName()));
        }

        String columnsText = "";
        for (Column col : columns) {
            columnsText += QualifyName(col.getName()) + ",";
        }
        columnsText = columnsText.substring(0, columnsText.length() - 1);
        sql.append(String.format(ADD_INDEX, tableName, indexTypeAndName, columnsText));
        return sql.toString();
    }

    /**
     * Gets the type of the db.
     *
     * @param sqlType Type of SQL.
     * @return
     */
    public abstract DbType GetDbType(String sqlType);


    /**
     * Gets the type of the native.
     *
     * @param dbType Type of the db.
     * @return
     */
    public abstract String GetNativeType(DbType dbType);

    public void SetColumnDefaults(Column column) {
        if (column.getDbType().isNumeric()) {
            column.setDefaultSetting(0);
        } else if (column.getDbType().isDate()) {
            column.setDefaultSetting(java.time.LocalDateTime.parse("1/1/1900"));
        } else if (column.getDbType().isString()) {
            column.setDefaultSetting("");
        } else if (column.getDbType() == DbType.BOOLEAN) {
            column.setDefaultSetting(0);
        }
    }
}