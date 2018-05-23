package com.zoe.framework.db.schema.reader;

import com.zoe.framework.db.schema.query.QueryHelper;
import com.zoe.framework.db.schema.*;
import com.zoe.framework.db.schema.reader.ISchemaReader;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caizhicong on 2016/3/21.
 */
public abstract class SchemaReaderBase implements ISchemaReader {
    private static final Pattern rxCleanUp = Pattern.compile("[^\\w\\d_]");
    /**
     * 获取表列表
     */
    private static final String TABLE_SQL = "SELECT TABLE_CATALOG AS [Database], TABLE_SCHEMA AS Owner, TABLE_NAME AS Name, TABLE_TYPE " + "\r\n" +
            "                                        FROM INFORMATION_SCHEMA.TABLES" + "\r\n" +
            "                                        WHERE (TABLE_TYPE = 'BASE TABLE') AND (TABLE_NAME <> N'sysdiagrams') AND (TABLE_NAME <> N'dtproperties')";
    /**
     * 获取视图列表
     */
    private static final String VIEW_SQL = "SELECT TABLE_CATALOG AS [Database], TABLE_SCHEMA AS Owner, TABLE_NAME AS Name, TABLE_TYPE" + "\r\n" +
            "                                        FROM INFORMATION_SCHEMA.TABLES" + "\r\n" +
            "                                        WHERE (TABLE_TYPE = 'VIEW') AND (TABLE_NAME <> N'sysdiagrams')";
    public LinkedHashMap<String, Table> schemaCollection = new LinkedHashMap<>();
    private QueryHelper queryHelper;
    private String _databaseVersion;

    public SchemaReaderBase(QueryHelper queryHelper) {
        this.queryHelper = queryHelper;
    }

    protected static final String CleanUp(String str) {
        Matcher matcher = rxCleanUp.matcher(str);
        str = matcher.replaceAll("_");
        if (Character.isDigit(str.charAt(0))) {
            str = "_" + str;
        }
        return str;
    }

    public QueryHelper getQueryHelper() {
        return queryHelper;
    }

    public abstract ServerType getServerType();

    /**
     * Gets the database version.
     * <p>
     * <value>The database version.</value>
     */
    public final String getDatabaseVersion() {
        if (StringUtils.isBlank(_databaseVersion)) {
            _databaseVersion = GetDatabaseVersion();
        }
        return _databaseVersion;
    }

    /**
     * Gets the database version.
     *
     * @return
     */
    protected abstract String GetDatabaseVersion();

    public final void AddSchema(Table table) {
        String tableName = table.getName(true);
        if (!this.schemaCollection.containsKey(tableName)) {
            synchronized (schemaCollection) {
                if (!this.schemaCollection.containsKey(tableName)) {
                    this.schemaCollection.put(tableName, table);
                }
            }
        }
    }

    public final Table GetSchema(Table table) {
        String tableName = table.getName(true);
        if (this.schemaCollection.containsKey(tableName)) {
            synchronized (schemaCollection) {
                if (this.schemaCollection.containsKey(tableName)) {
                    return this.schemaCollection.get(tableName);
                }
            }
        }
        return null;
    }

    public Table getTableSchema(String owner, String tableName) {
        return getTableSchema(owner, tableName, TableType.Table);
    }

    /**
     * Gets the table schema.
     *
     * @param tableName Name of the table.
     * @param tableType Type of the table.
     * @return
     */
    public Table getTableSchema(String owner, String tableName, TableType tableType) {
        Table table = new Table(tableName);
        table.setOwner(owner);
        table.setTableType(tableType);

        Table tbl = GetSchema(table);
        if (tbl != null) {
            return tbl;
        }
        getTableColumns(table);

        AddSchema(table);
        return table;
    }

    /**
     * Gets the SP list.
     *
     * @return
     */
    public abstract List<String> getSPList();

    /**
     * Gets the SP list.
     *
     * @return
     */
    public List<String> getSPList(boolean includeSchema) {
        return getSPList();
    }

    /**
     * Gets the SP params.
     *
     * @param spName Name of the sp.
     * @return
     */
    public abstract List<Map> getSPParams(String spName);

    /**
     * Gets the table name by primary key.
     *
     * @param pkName       Name of the pk.
     * @param providerName Name of the provider.
     * @return
     */
    public abstract String getTableNameByPrimaryKey(String pkName, String providerName);

    /**
     * Gets the primary key table names.
     *
     * @param tableName Name of the table.
     * @return
     */
    public abstract List<Map> getPrimaryKeyTableNames(String tableName);

    /**
     * Gets the primary key tables.
     *
     * @param tableName Name of the table.
     * @return
     */
    public abstract List<Table> getPrimaryKeyTableList(String tableName);

    public void reloadSchema() {
        schemaCollection.clear();
    }

    private List<Table> getTables(String schemaPattern,
                                  String tableNamePattern, String types[]) {
        Connection con = null;
        List<Table> tables = new ArrayList<>();
        try {
            con = queryHelper.getConnection();
            DatabaseMetaData meta = con.getMetaData();
            if (StringUtils.isBlank(tableNamePattern)) {
                tableNamePattern = "%";
            }
            ResultSet rs = meta.getTables(null, schemaPattern, tableNamePattern, types);
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                String tableSchema = rs.getString("TABLE_SCHEM");
                String tableType = rs.getString("TABLE_TYPE");
                String remarks = rs.getString("REMARKS");
                Table table = new Table();
                if (StringUtils.isBlank(tableSchema) && !StringUtils.isBlank(schemaPattern)) {
                    tableSchema = schemaPattern;
                }
                table.setSchemaName(tableSchema);
                table.setName(tableName);
                table.setTableType(TableType.forValue(tableType));
                table.setComment(remarks);
                tables.add(table);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tables;
    }

    private List<Column> getColumns(String schemaPattern,
                                    String tableNamePattern, String columnNamePattern) {
        Connection con = null;
        List<Column> columns = new ArrayList<>();
        Table table = new Table(schemaPattern, tableNamePattern);
        try {
            con = queryHelper.getConnection();
            DatabaseMetaData meta = con.getMetaData();
            if (StringUtils.isBlank(columnNamePattern)) {
                columnNamePattern = "%";
            }
            ResultSet rs = meta.getColumns(null, schemaPattern, tableNamePattern, columnNamePattern);
            while (rs.next()) {
                //columns：1..17
                //TABLE_CAT，TABLE_SCHEM，TABLE_NAME，COLUMN_NAME，DATA_TYPE，TYPE_NAME，COLUMN_SIZE，BUFFER_LENGTH，DECIMAL_DIGITS
                //NUM_PREC_RADIX，NULLABLE，REMARKS，COLUMN_DEF，SQL_DATA_TYPE，SQL_DATETIME_SUB，CHAR_OCTET_LENGTH，ORDINAL_POSITION
                String columnName = rs.getString("COLUMN_NAME");
                Column column = new Column(columnName, table);
                String typeName = rs.getString("TYPE_NAME");
                column.setTypeName(typeName);
                //DbType.forValue(rs.getInt("DATA_TYPE"))//这种方式会由于java.sql.Types类型覆盖不够全面，会出现longblob映射到LONGVARBINARY
                //使用typeName存储的sql类型（字符串）进行转换会比较全
                column.setDbType(GetDbType(typeName));
                //COLUMN_SIZE int => column size. For char or date types this is the maximum number of characters, for numeric or decimal types this is precision.
                int columnSize = rs.getInt("COLUMN_SIZE");
                if (column.getDbType().isString()) {
                    column.setLength(columnSize);
                } else if (column.getDbType().isDate()) {
                    column.setLength(GetDbTypeLength(column.getDbType(), columnSize));
                } else if (column.getDbType().isNumeric()) {
                    column.setPrecision(columnSize);
                }
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                column.setScale(decimalDigits);
                /**
                 *  0 (columnNoNulls) - 该列不允许为空
                 *  1 (columnNullable) - 该列允许为空
                 *  2 (columnNullableUnknown) - 不确定该列是否为空
                 */
                int nullable = rs.getInt("NULLABLE");  //是否允许为null
                column.setIsNullable(nullable == 1);
                column.setComment(rs.getString("REMARKS"));
                column.setDefaultSetting(rs.getString("COLUMN_DEF"));
                columns.add(column);
            }
            rs.close();

            ResultSet pkRSet = meta.getPrimaryKeys(null, schemaPattern, tableNamePattern);
            while (pkRSet.next()) {
                /*System.err.println("****** Comment ******");
                System.err.println("TABLE_CAT : " + pkRSet.getObject(1));
                System.err.println("TABLE_SCHEM: " + pkRSet.getObject(2));
                System.err.println("TABLE_NAME : " + pkRSet.getObject(3));
                System.err.println("COLUMN_NAME: " + pkRSet.getObject(4));
                System.err.println("KEY_SEQ : " + pkRSet.getObject(5));
                System.err.println("PK_NAME : " + pkRSet.getObject(6));
                System.err.println("****** ******* ******");*/
                String columnName = pkRSet.getString(4);
                for (Column column : columns) {
                    if (column.getName().equalsIgnoreCase(columnName)) {
                        column.setIsPrimaryKey(true);
                        break;
                    }
                }
            }
            pkRSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return columns;
    }

    /**
     * 获取表列表，包含表注释，不包含表字段列表
     *
     * @param owner     表的所有者
     * @param tableName 表名称
     * @return
     */
    public List<Column> getColumns(String owner, String tableName) {
        List<Column> columns = getColumns(owner, tableName, null);
        return columns;
    }

    /**
     * 获取表列表
     */
    public List<String> getTableNameList(String schemaName) {
        String[] types = {"TABLE"};
        return getTablesNameList(schemaName, types);
    }

    /**
     * 获取视图列表
     *
     * @return
     */
    public List<String> getViewNameList(String schemaName) {
        String[] types = {"VIEW"};
        return getTablesNameList(schemaName, types);
    }

    /**
     * 获取表或者视图的名称列表
     *
     * @param schemaName
     * @param types
     * @return
     */
    private List<String> getTablesNameList(String schemaName, String[] types) {
        List<Table> tables = getTables(schemaName, null, types);
        List<String> tableNames = new ArrayList<>(tables.size());
        for (Table table : tables) {
            tableNames.add(table.getName());
        }
        Collections.sort(tableNames);
        return tableNames;
    }

    public PrimaryKey determinePrimaryKeys(Table table) {
        List<Column> primaryKeys = table.getPrimaryKeys();
        int size = primaryKeys.size();
        if (size > 0) {
            PrimaryKey primaryKey = new PrimaryKey();
            primaryKey.setType(size == 1 ? PrimaryKeyType.PrimaryKey : PrimaryKeyType.CompositeKey);
            primaryKey.setColumns(primaryKeys);
            return primaryKey;
        }
        return null;
    }

    public List<IStoredProcedure> getStoredProcedures(String owner) {
        throw new UnsupportedOperationException();
    }

    public List<String> getOwners() {
        throw new UnsupportedOperationException();
    }

    public List<String> getSequences(String owner) {
        throw new UnsupportedOperationException();
    }

    public List<Column> getTableColumns(Table table) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取表列表，包含表注释，不包含表字段列表
     *
     * @param owner 表的所有者
     * @return
     */
    public List<Table> getTables(String owner) {
        String[] types = {"TABLE"};
        List<Table> tables = getTables(owner, null, types);
        return tables;
    }

    /**
     * 获取表列表，包含表注释，不包含表字段列表
     *
     * @param owner     表的所有者
     * @param tableName 表名称
     * @return
     */
    public Table getTable(String owner, String tableName) {
        String[] types = {"TABLE"};
        return getTable(owner, tableName, types);
    }

    /**
     * 获取视图列表，包含视图注释，不包含视图字段列表
     *
     * @param owner 视图的所有者
     * @return
     */
    public List<Table> getViews(String owner) {
        String[] types = {"VIEW"};
        List<Table> tables = getTables(owner, null, types);
        return tables;
    }

    /**
     * 获取视图列表，包含视图注释，不包含视图字段列表
     *
     * @param owner    视图的所有者
     * @param viewName 视图名称
     * @return
     */
    public Table getView(String owner, String viewName) {
        String[] types = {"VIEW"};
        return getTable(owner, viewName, types);
    }

    /**
     * 获取表或者视图的名称列表
     *
     * @param schemaName
     * @param types
     * @return
     */
    private Table getTable(String schemaName, String tableName, String[] types) {
        List<Table> tables = getTables(schemaName, tableName, types);
        if (tables.size() > 0) {
            return tables.get(0);
        }
        return null;
    }

    public ArrayList<IDBObject> getDbObjects(String owner) {
        throw new UnsupportedOperationException();
    }

    public DbType GetDbType(String sqlType) {
        return DbType.OTHER;
    }

    public int GetDbTypeLength(DbType dbType, int columnSize) {
        return columnSize;
    }
}