package com.zoe.framework.db.schema.reader;

import com.zoe.framework.db.schema.*;
import com.zoe.framework.db.schema.conversion.MySqlConvert;
import com.zoe.framework.db.schema.query.QueryHelper;
import com.zoe.framework.db.schema.query.QueryParams;
import com.zoe.framework.db.schema.reader.SchemaReaderBase;
import com.zoe.framework.db.schema.utils.CastUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by caizhicong on 2016/5/16.
 */
public class MysqlSchemaReader extends SchemaReaderBase {
    private static final String COLUMN_SQL = "" + "\r\n" +
            "select " + "\r\n" +
            "    *" + "\r\n" +
            "from" + "\r\n" +
            "    information_schema.columns" + "\r\n" +
            "where" + "\r\n" +
            "    table_schema = '%s'" + "\r\n" +
            "        and table_name = '%s'";

    public MysqlSchemaReader(QueryHelper queryHelper) {
        super(queryHelper);
    }

    @Override
    public ServerType getServerType() {
        return ServerType.MySQL;
    }

    @Override
    public java.util.List<Column> getTableColumns(Table table) {
        java.util.ArrayList<Column> columns = new java.util.ArrayList<Column>();

        String sql = String.format(COLUMN_SQL, table.getOwner(), table.getName());
        List<Map> list = getQueryHelper().findMap(sql, Collections.emptyMap());

        for (Map row : list) {
            Column col = new Column(table);
            col.setName(row.get("COLUMN_NAME").toString());
            col.setPropertyName(CleanUp(col.getName()));
            col.setIsNullable(CastUtils.cast(row.get("IS_NULLABLE"), String.class, "").equals("YES"));
            col.setIsPrimaryKey(CastUtils.cast(row.get("COLUMN_KEY"), String.class, "").equals("PRI"));
            col.setIsUnique(CastUtils.cast(row.get("COLUMN_KEY"), String.class, "").equals("UNI"));
            col.setAutoIncrement(CastUtils.cast(row.get("EXTRA"), String.class, "").toLowerCase().contains("auto_increment"));
            col.setIsIdentity(col.isAutoIncrement());
            col.setIsReadOnly(false);
            col.setComment(CastUtils.cast(row.get("COLUMN_COMMENT"), String.class, ""));
            col.setTypeName(CastUtils.cast(row.get("DATA_TYPE"), String.class, ""));
            col.setDbType(GetDbType(col.getTypeName()));
            Object temp = row.get("CHARACTER_MAXIMUM_LENGTH");
            if (temp != null) {
                col.setLength(CastUtils.cast(temp, int.class, 0));
            }
            temp = row.get("NUMERIC_PRECISION");
            if (temp != null) {
                col.setPrecision(CastUtils.cast(temp, int.class, 0));
            }
            temp = row.get("NUMERIC_SCALE");
            if (temp != null) {
                col.setScale(CastUtils.cast(temp, int.class, 0));
            }
            col.setIsForeignKey(CastUtils.cast(row.get("COLUMN_KEY"), String.class, "").equals("MUL"));
            columns.add(col);
        }
        table.setColumns(columns);
        table.setOwner(table.getOwner());
        table.setPrimaryKey(determinePrimaryKeys(table));
        return columns;
    }

    /**
     * Gets the type of the db.
     *
     * @param sqlType Type of my SQL.
     * @return
     */
    @Override
    public DbType GetDbType(String sqlType) {
        return MySqlConvert.GetDbType(sqlType);
    }

    @Override
    public int GetDbTypeLength(DbType dbType, int columnSize) {
        if (dbType.isDate()) {
            if (dbType == DbType.YEAR) {
                return 4;
            }
            if (columnSize > 6) {
                return 0;//有遇到mysql日期类型的columnSize为19，但实际最大就6
            }
        }
        return super.GetDbTypeLength(dbType, columnSize);
    }

    @Override
    public java.util.List<String> getOwners() {
        List<String> owners = new ArrayList<>();
        String sql = "select distinct table_schema from information_schema.tables" + "\r\n" +
                "                                                union" + "\r\n" +
                "                                                select schema_name from information_schema.schemata" + "\r\n" +
                "                                                ";
        List<String> list = getQueryHelper().findList(String.class, sql, Collections.emptyMap());
        owners.addAll(list);
        return owners;
    }

    public List<Table> getTablesBySql(String owner) {
        java.util.ArrayList<Table> tables = new java.util.ArrayList<Table>();
        String sql = String.format("select table_name,table_comment from information_schema.tables where table_type like 'BASE TABLE' and TABLE_SCHEMA = '%1$s'", owner);
        List<Map> list = getQueryHelper().findMap(sql, Collections.emptyMap());
        for (Map map : list) {
            String tableName = String.valueOf(map.get("table_name"));
            String tableComment = String.valueOf(map.get("table_comment"));
            Table tempVar = new Table();
            tempVar.setName(tableName);
            tempVar.setSchemaName(owner);
            tempVar.setComment(tableComment);
            tables.add(tempVar);
        }
        java.util.Collections.sort(tables, (x, y) -> x.getName().compareTo(y.getName()));
        return tables;
    }

    @Override
    public List<String> getSequences(String owner) {
        return null;
    }

    public final String GetSequences(String tablename, String owner, String column) {
        String sql = "select " + "\r\n" +
                "                b.sequence_name" + "\r\n" +
                "                from" + "\r\n" +
                "                information_schema.columns a" + "\r\n" +
                "                inner join information_schema.sequences b on a.column_default like 'nextval(\\''||b.sequence_name||'%'" + "\r\n" +
                "                where" + "\r\n" +
                "                a.table_schema='" + owner + "' and a.table_name='" + tablename + "' and a.column_name='" + column + "'";
        String sequence_name = getQueryHelper().findFirst(String.class, sql, Collections.emptyMap());
        return sequence_name;
    }

    public List<String> GetSequences(java.util.ArrayList<Table> tables) {
        String sql = "select sequence_name from information_schema.sequences";
        List<String> sequences = getQueryHelper().findList(String.class, sql, Collections.emptyMap());
        return sequences;
    }

    @Override
    protected String GetDatabaseVersion() {
        try {
            Connection conn = getQueryHelper().getConnection();
            String ver = conn.getMetaData().getDatabaseMajorVersion() + "";
            conn.close();
            return ver;
        } catch (java.lang.Exception e) {
            return "UNKNOWN";
        }
    }

    @Override
    public List<String> getSPList() {
        final String sql = "SELECT routine_name FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_SCHEMA = ?databaseName";
        java.util.ArrayList<String> list = new java.util.ArrayList<String>();
        //todo fetch data
        return list;
    }

    @Override
    public List<Map> getSPParams(String spName) {
        return null;
    }

    /**
     * Gets the foreign key tables.
     *
     * @param tableName Name of the table.
     * @return
     */
    public String[] GetForeignKeyTables(String tableName) {
        return new String[]{""};
        //throw new Exception("The method or operation is not implemented.");
    }

    /**
     * Gets the table name by primary key.
     *
     * @param pkName       Name of the pk.
     * @param providerName Name of the provider.
     * @return
     */
    @Override
    public String getTableNameByPrimaryKey(String pkName, String providerName) {
        // TODO: Look in to the use of this method and program if possible.
        return "";
    }

    /**
     * Gets the primary key table names.
     *
     * @param tableName Name of the table.
     * @return
     */
    @Override
    public java.util.ArrayList getPrimaryKeyTableNames(String tableName) {
        // Relationships are only supported in the InnoDB engine.
        // Being that most databases are implemented via MyISAM
        // I will code for majority not minority.
        return new java.util.ArrayList();
    }

    /**
     * Gets the primary key tables.
     *
     * @param tableName Name of the table.
     * @return
     */
    @Override
    public List<Table> getPrimaryKeyTableList(String tableName) {
        return null;
    }


    /**
     * Gets the name of the foreign key table.
     *
     * @param fkColumnName Name of the fk column.
     * @param tableName    Name of the table.
     * @return
     */
    public String GetForeignKeyTableName(String fkColumnName, String tableName) {
        return "";
    }

    /**
     * Gets the name of the foreign key table.
     *
     * @param fkColumnName Name of the fk column.
     * @return
     */
    public String GetForeignKeyTableName(String fkColumnName) {
        return "";
    }

    /**
     * Private helper method to check the version of MySQL.
     * <p>
     * This is important because MySQL versions prior to 5.x
     * did not support the standard INFORMATION_SCHEMA views.
     *
     * @param VersionLine Version line returned from the server.
     * @return
     */
    public static boolean SupportsInformationSchema(String VersionLine) {
        int majorVersion = 0;
        try {
            majorVersion = Integer.parseInt(VersionLine.substring(0, 1));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return majorVersion > 4;
    }

    /**
     * Gets the view name list.
     *
     * @return
     */
    public List<String> getViewNameList2(String schemaName) {
        final String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = ?databaseName";
        try {
            if (!SupportsInformationSchema(GetDatabaseVersion())) {
                return new ArrayList<>();
            }
            List<String> list = getQueryHelper().findList(String.class, sql, QueryParams.create().add("databaseName", getQueryHelper().getConnection().getSchema()));
            Collections.sort(list);
            return list;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Gets the table name list.
     *
     * @return
     */
    public List<String> getTableNameList2(String schemaName) {
        try {
            String sql = "SHOW TABLES";
            if (SupportsInformationSchema(GetDatabaseVersion())) {
                sql = "select table_name from information_schema.tables where table_schema = ?databaseName and table_type <> 'VIEW'";
            }
            List<String> list = getQueryHelper().findList(String.class, sql, QueryParams.create().add("databaseName", getQueryHelper().getConnection().getSchema()));
            Collections.sort(list);
            return list;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }
}