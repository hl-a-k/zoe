package com.zoe.framework.db.schema.reader;

import com.zoe.framework.db.schema.*;
import com.zoe.framework.db.schema.query.QueryHelper;
import com.zoe.framework.db.schema.reader.SchemaReaderBase;
import com.zoe.framework.db.schema.utils.CastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caizhicong on 2016/3/21.
 */
public class OracleSchemaReader extends SchemaReaderBase {

    private static final String GET_FOREIGN_KEY_SQL = "SELECT d.table_name " + " FROM user_cons_columns c, user_constraints d, user_constraints e " + " WHERE d.constraint_name = e.r_constraint_name " + " AND c.constraint_name = e.constraint_name " + " AND c.column_name = :columnName " + " AND e.table_name = :tableName ";

    private static final String GET_PRIMARY_KEY_SQL = "SELECT e.table_name, c.column_name " + "  FROM user_cons_columns c, user_cons_columns d, user_constraints e " + " WHERE d.constraint_name = e.r_constraint_name " + "   AND c.constraint_name = e.constraint_name " + "   AND d.table_name = :tableName ";

    private static final String GET_TABLE_SQL = "SELECT b.table_name " + "  FROM user_constraints a, user_cons_columns b " + " WHERE a.constraint_name = b.constraint_name " + "   AND a.constraint_type IN ('R', 'P') " + "   AND b.column_name = :columnName " + "   AND a.constraint_type = 'P' ";

    private static final String INDEX_SQL = "SELECT b.table_name, b.column_name, " + "       DECODE (a.constraint_type, " + "               'R', 'FOREIGN KEY', " + "               'P', 'PRIMARY KEY', " + "               'UNKNOWN' " + "              ) constraint_type " + "  FROM user_constraints a, user_cons_columns b " + " WHERE a.constraint_name = b.constraint_name " + "   AND a.constraint_type IN ('R', 'P') " + "   AND b.table_name = :tableName ";

    private static final String MANY_TO_MANY_LIST = "SELECT b.table_name FROM user_constraints a, user_cons_columns b " + "WHERE a.table_name = :tableName " + "AND a.constraint_type = 'R' " + "AND a.r_constraint_name = b.constraint_name " + "AND b.table_name like '%' + :mapSuffix";

    private static final String SP_PARAM_SQL = "SELECT a.object_name, a.object_type, b.position, b.in_out, " + "\r\n" +
            "                                    b.argument_name, b.data_type, b.char_length, b.data_precision, b.data_scale " + "\r\n" +
            "                                    FROM user_objects a, user_arguments b " + "\r\n" +
            "                                    WHERE a.object_type IN ('PROCEDURE', 'PACKAGE') " + "\r\n" +
            "                                    AND a.object_id = b.object_id " + "\r\n" +
            "                                    AND a.object_name = :objectName";

    private static final String SP_SQL = "SELECT a.object_name, a.object_type, a.created, a.last_ddl_time " + "\r\n" +
            "                                FROM user_objects a " + "\r\n" +
            "                                WHERE a.object_type IN ('PROCEDURE', 'PACKAGE') " + "\r\n" +
            "                                ORDER BY a.object_name";

    private static final String SP_SQL_DBA = "SELECT a.owner, a.object_name, a.object_type, a.created, a.last_ddl_time " + "\r\n" +
            "                                FROM dba_objects a " + "\r\n" +
            "                                WHERE a.object_type IN ('PROCEDURE', 'PACKAGE') " + "\r\n" +
            "                                ORDER BY a.object_name";

    private static final String TABLE_COLUMN_SQL = "SELECT user, a.table_name, a.column_name, a.column_id, a.data_default, " + "       a.nullable, a.data_type, a.char_length, a.data_precision, a.data_scale " + "  FROM user_tab_columns a " + " WHERE a.table_name = :tableName";

    private static final String TABLE_SQL = "SELECT a.table_name FROM user_tables a";

    private static final String VIEW_SQL = "SELECT a.view_name FROM user_views a";

    public OracleSchemaReader(QueryHelper queryHelper) {
        super(queryHelper);
    }

    @Override
    public ServerType getServerType() {
        return ServerType.Oracle;
    }

    /**
     * Return all table details based on table and owner.
     *
     * @return
     */
    @Override
    public List<Column> getTableColumns(Table table) {
        List<Column> columns = new ArrayList<>();
        String sql = "select column_name," + "\r\n" +
                "       data_type," + "\r\n" +
                "       nullable," + "\r\n" +
                "       sum(constraint_type) constraint_type," + "\r\n" +
                "       data_length," + "\r\n" +
                "       data_precision," + "\r\n" +
                "       data_scale," + "\r\n" +
                "       comments" + "\r\n" +
                "  from (SELECT tc.column_name AS column_name," + "\r\n" +
                "               tc.data_type AS data_type," + "\r\n" +
                "               tc.nullable AS NULLABLE," + "\r\n" +
                "               decode(c.constraint_type, 'P', 1, 'R', 2, 'U', 4, 'C', 8, 16) AS constraint_type," + "\r\n" +
                "               data_length," + "\r\n" +
                "               data_precision," + "\r\n" +
                "               data_scale," + "\r\n" +
                "               column_id," + "\r\n" +
                "               cm.COMMENTS" + "\r\n" +
                "          from all_tab_columns tc" + "\r\n" +
                "          left outer join(all_cons_columns cc" + "\r\n" +
                "          join all_constraints c" + "\r\n" +
                "            on (c.owner = cc.owner and" + "\r\n" +
                "               c.constraint_name = cc.constraint_name)) on (tc.owner = cc.owner and tc.table_name = cc.table_name and tc.column_name = cc.column_name)" + "\r\n" +
                "        " + "\r\n" +
                "          left join all_col_comments cm" + "\r\n" +
                "            on cm.owner = tc.OWNER" + "\r\n" +
                "           and cm.table_name = tc.TABLE_NAME" + "\r\n" +
                "           and cm.COLUMN_NAME = tc.COLUMN_NAME" + "\r\n" +
                "        " + "\r\n" +
                "         where tc.table_name = :table_name" + "\r\n" +
                "           and tc.owner = :owner" + "\r\n" +
                "         order by tc.table_name, cc.position nulls last, tc.column_id)" + "\r\n" +
                " group by column_name," + "\r\n" +
                "          data_type," + "\r\n" +
                "          nullable," + "\r\n" +
                "          data_length," + "\r\n" +
                "          data_precision," + "\r\n" +
                "          data_scale," + "\r\n" +
                "          column_id," + "\r\n" +
                "          comments" + "\r\n" +
                " order by column_id";

        Map<String, Object> params = new HashMap<>();
        params.put("table_name", table.getName());
        params.put("owner", table.getOwner());
        List<Map> list = getQueryHelper().findMap(sql, params);
        for (Map item : list) {
            int constraintType = CastUtils.cast(item.get("CONSTRAINT_TYPE"), Integer.class);
            Integer dataLength = item.get("DATA_LENGTH") == null ? 0 : CastUtils.cast(item.get("DATA_LENGTH"), Integer.class);
            Integer dataPrecision = item.get("DATA_PRECISION") == null ? 0 : CastUtils.cast(item.get("DATA_PRECISION"), Integer.class);
            Integer dataScale = item.get("DATA_SCALE") == null ? 0 : CastUtils.cast(item.get("DATA_SCALE"), Integer.class);

            Column column = new Column(table);
            column.setName(item.get("COLUMN_NAME").toString());
            column.setTypeName(item.get("DATA_TYPE").toString());
            column.setDbType(GetDbType(column.getTypeName()));
            column.setIsNullable("Y".equalsIgnoreCase(item.get("NULLABLE").toString()));
            column.setIsPrimaryKey(ConstraintTypeResolver.IsPrimaryKey(constraintType));
            column.setIsForeignKey(ConstraintTypeResolver.IsForeignKey(constraintType));
            column.setIsUnique(ConstraintTypeResolver.IsUnique(constraintType));
            column.setLength(dataLength);
            column.setPrecision(dataPrecision);
            column.setScale(dataScale);
            column.setComment(String.valueOf(item.get("COMMENTS")));
            columns.add(column);
        }
        table.setColumns(columns);
        table.setPrimaryKey(determinePrimaryKeys(table));
        return columns;
    }

    /**
     * 获取表列表，包含表注释，不包含表字段列表
     *
     * @param owner 表的所有者
     * @return
     */
    public List<Table> getTablesBySql(String owner) {
        String sql = "select m.table_name, n.COMMENTS" + "\r\n" +
                "    from all_tables m, all_TAB_COMMENTS n" + "\r\n" +
                "   where m.OWNER = n.OWNER" + "\r\n" +
                "     and m.TABLE_NAME = n.TABLE_NAME" + "\r\n" +
                "     and m.owner = :owner" + "\r\n" +
                "   order by m.table_name";
        Map<String, Object> params = new HashMap<>();
        params.put("owner", owner);
        List<Map> mapList = getQueryHelper().findMap(sql, params);
        ArrayList<Table> tables = new ArrayList<>(mapList.size());
        for (Map map : mapList) {
            Table tempVar = new Table();
            tempVar.setName(map.get("TABLE_NAME").toString());
            tempVar.setComment(String.valueOf(map.get("COMMENTS")));
            tables.add(tempVar);
        }
        return tables;
    }

    private Table GetTableBySql(String owner, String tableName) {
        String sql = "select m.table_name, n.COMMENTS" + "\r\n" +
                "    from all_tables m, all_TAB_COMMENTS n" + "\r\n" +
                "   where m.OWNER = n.OWNER" + "\r\n" +
                "     and m.TABLE_NAME = n.TABLE_NAME" + "\r\n" +
                "     and m.owner = :owner" + "\r\n" +
                "     and m.table_name = :table_name";
        Map<String, Object> params = new HashMap<>();
        params.put("owner", owner);
        params.put("table_name", tableName);
        List<Map> tableNames = getQueryHelper().findMap(sql, params);
        for (Map map : tableNames) {
            Table tempVar = new Table();
            tempVar.setOwner(owner);
            tempVar.setName(map.get("TABLE_NAME").toString());
            tempVar.setComment(String.valueOf(map.get("COMMENTS")));
            return tempVar;
        }
        return null;
    }


    @Override
    public List<String> getOwners() {
        List<String> owners = getQueryHelper().findList(String.class, "select username from all_users order by username", null);
        return owners;
    }

    @Override
    public ArrayList<String> getSequences(String owner) {
        ArrayList<String> sequences = new ArrayList<String>();
        //using (var conn = DB.OpenSharedConnection())
        //{
        //    using (var seqCommand = conn.CreateCommand())
        //    {
        //        seqCommand.CommandText = "select sequence_name from all_sequences where sequence_owner = :owner order by sequence_name";
        //        DB.AddParameter(seqCommand, "owner", owner);
        //        using (IDataReader seqReader = seqCommand.ExecuteReader())
        //        {
        //            while (seqReader.Read())
        //            {
        //                string tableName = seqReader.GetString(0);
        //                sequences.Add(tableName);
        //            }
        //        }
        //    }
        //    //DB.CloseSharedConnection();
        //}
        return sequences;
    }

    @Override
    public List<String> getSPList() {
        List<String> list = getSPList(true);
        if (list != null) {
            return list;
        }
        return getSPList(false);
    }

    @Override
    public List<String> getSPList(boolean includeSchema) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Map> getSPParams(String spName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTableNameByPrimaryKey(String pkName, String providerName) {
        return "";
    }

    /**
     * TableName|ColumnName
     *
     * @param tableName
     * @return
     */
    @Override
    public List<Map> getPrimaryKeyTableNames(String tableName) {
        List<Map> arrayList = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);
        List<Map> list = getQueryHelper().findMap(GET_PRIMARY_KEY_SQL, params);
        arrayList.addAll(list);
        return arrayList;
    }

    @Override
    public List<Table> getPrimaryKeyTableList(String tableName) {
        ArrayList<String> names = new ArrayList<String>();
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);
        List<Map> list = getQueryHelper().findMap(GET_PRIMARY_KEY_SQL, params);
        for (Map item : list) {
            names.add(item.get("TABLE_NAME").toString());
        }

        if (names.size() > 0) {
            List<Table> tables = new ArrayList<>(names.size());
            for (int i = 0; i < names.size(); i++) {
                tables.add(getTableSchema(null, names.get(i), TableType.Table));
            }
            return tables;
        }
        return null;
    }

    @Override
    protected String GetDatabaseVersion() {
        return "Unknown";
    }

    @Override
    public DbType GetDbType(String dataType) {
        String type = dataType.toLowerCase();
        switch (type) {
            case "number":
                return DbType.NUMERIC;
            case "integer":
                return DbType.INTEGER;
            case "varchar":
            case "varchar2":
                return DbType.VARCHAR;
            case "date":
                return DbType.DATE;
        }
        return super.GetDbType(dataType);
    }

    public static final class OracleConstraintType {
        public static final OracleConstraintType PrimaryKey = new OracleConstraintType(1, "P");
        public static final OracleConstraintType ForeignKey = new OracleConstraintType(2, "R");
        public static final OracleConstraintType Unique = new OracleConstraintType(4, "U");
        public static final OracleConstraintType Check = new OracleConstraintType(8, "C");
        private String name;
        private int value;

        private OracleConstraintType(int value, String name) {
            this.name = name;
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class ConstraintTypeResolver //: IConstraintTypeResolver
    {
        public static boolean IsPrimaryKey(int constraintType) {
            return (constraintType & OracleConstraintType.PrimaryKey.getValue()) == OracleConstraintType.PrimaryKey.getValue();
        }

        public static boolean IsForeignKey(int constraintType) {
            return (constraintType & OracleConstraintType.ForeignKey.getValue()) == OracleConstraintType.ForeignKey.getValue();
        }

        public static boolean IsUnique(int constraintType) {
            return (constraintType & OracleConstraintType.Unique.getValue()) == OracleConstraintType.Unique.getValue();
        }

        public static boolean IsCheck(int constraintType) {
            return (constraintType & OracleConstraintType.Check.getValue()) == OracleConstraintType.Check.getValue();
        }
    }
}