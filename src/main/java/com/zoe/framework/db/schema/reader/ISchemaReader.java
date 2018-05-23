package com.zoe.framework.db.schema.reader;

import com.zoe.framework.db.schema.*;

import java.util.List;
import java.util.Map;

/**
 * Created by caizhicong on 2016/3/21.
 */
public interface ISchemaReader {

    ServerType getServerType();

    List<Column> getTableColumns(Table table);

    /**
     * 获取表列表，包含表注释，不包含表字段列表
     *
     * @param owner 表的所有者
     * @return
     */
    List<Table> getTables(String owner);

    /**
     * 获取表列表，包含表注释，不包含表字段列表
     *
     * @param owner     表的所有者
     * @param tableName 表名称
     * @return
     */
    Table getTable(String owner, String tableName);

    /**
     * 获取视图列表，包含视图注释，不包含视图字段列表
     *
     * @param owner 视图的所有者
     * @return
     */
    List<Table> getViews(String owner);

    /**
     * 获取视图列表，包含视图注释，不包含视图字段列表
     *
     * @param owner    视图的所有者
     * @param viewName 视图名称
     * @return
     */
    Table getView(String owner, String viewName);

    /**
     * 获取表字段列表
     *
     * @param owner     表的所有者
     * @param tableName 表名称
     * @return
     */
    List<Column> getColumns(String owner, String tableName);

    List<IDBObject> getDbObjects(String owner);

    List<IStoredProcedure> getStoredProcedures(String owner);

    List<String> getOwners();

    List<String> getSequences(String owner);

    PrimaryKey determinePrimaryKeys(Table table);

    Table getTableSchema(String owner, String tableName);

    /**
     * Gets the table schema.
     *
     * @param tableName Name of the table.
     * @param tableType Type of the table.
     * @return
     */
    Table getTableSchema(String owner, String tableName, TableType tableType);

    /**
     * 获取表名称列表
     */
    List<String> getTableNameList(String schemaName);

    /**
     * 获取视图名称列表
     *
     * @return
     */
    List<String> getViewNameList(String schemaName);

    /**
     * Gets the SP list.
     *
     * @return
     */
    List<String> getSPList();

    /**
     * Gets the SP list.
     *
     * @return
     */
    List<String> getSPList(boolean includeSchema);

    /**
     * Gets the SP params.
     *
     * @param spName Name of the sp.
     * @return
     */
    List<Map> getSPParams(String spName);

    /**
     * Gets the table name by primary key.
     *
     * @param pkName       Name of the pk.
     * @param providerName Name of the provider.
     * @return
     */
    String getTableNameByPrimaryKey(String pkName, String providerName);

    /**
     * Gets the primary key table names.
     *
     * @param tableName Name of the table.
     * @return
     */
    List<Map> getPrimaryKeyTableNames(String tableName);

    /**
     * Gets the primary key tables.
     *
     * @param tableName Name of the table.
     * @return
     */
    List<Table> getPrimaryKeyTableList(String tableName);

    /**
     * Force-reloads a provider's schema
     */
    void reloadSchema();

}
