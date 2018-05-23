package com.zoe.framework.sql4o;

import com.zoe.framework.sql2o.data.PocoColumn;
import com.zoe.framework.sql2o.data.PojoData;
import com.zoe.framework.sql2o.data.TableInfo;
import com.zoe.framework.sql2o.quirks.ServerType;
import com.zoe.framework.util.SqlFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.zoe.framework.sql4o.Sql2.Op.*;

/**
 * Sql组装工具类
 * Created by caizhicong on 2016/3/25.
 */
@Deprecated
public class Sql2 {

    private static Logger logger = LoggerFactory.getLogger(Sql2.class);

    private String _sql;
    private Sql2 _rhs;
    private String _sqlFinal;
    private int _pageSize;
    private int _pageNumber;
    private Map<String, Object> args;
    private TableInfo tableInfo;
    private boolean fromModel = false;
    private ServerType serverType;
    private List<String> columns;

    private Sql2() {
        args = new LinkedHashMap<>();
    }

    private Sql2(String sql) {
        _sql = sql;
    }

    public static Sql2 create() {
        return new Sql2();
    }

    private static boolean Is(Sql2 sql, String sqlType) {
        return sql != null && sql._sql != null
                && StringUtils.startsWithIgnoreCase(sql._sql, sqlType);
    }

    public static String getTableName(Class<?> model) {
        return PojoData.forClass(model).getTableInfo().getTableName();
    }

    private void build() {
        // already built?
        if (_sqlFinal != null)
            return;

        // build it
        StringBuilder sb = new StringBuilder();
        build(sb, null);
        _sqlFinal = sb.toString();
    }

    public String getSql() {
        build();
        return _sqlFinal;
    }

    public String toString() {
        return getSql();
    }

    public Map<String, Object> getArgs() {
        build();
        return args;
    }

    public int getPageSize() {
        return _pageSize;
    }

    public Sql2 setPageSize(int pageSize) {
        _pageSize = pageSize;
        return this;
    }

    public int getPageNumber() {
        return _pageNumber;
    }

    public Sql2 setPageNumber(int pageNumber) {
        _pageNumber = pageNumber;
        return this;
    }

    /*public Sql2 append(Sql2 sql) {
        if (_sqlFinal != null)
            _sqlFinal = null;

        if (_rhs != null) {
            _rhs.append(sql);
        } else if (_sql != null) {
            _rhs = sql;
        } else {
            _sql = sql._sql;
            _rhs = sql._rhs;
        }

        return this;
    }*/
    public Sql2 append(Sql2 sql) {
        if (_rhs != null) {
            _rhs.append(sql);
        } else {
            _rhs = sql;
        }
        _sqlFinal = null;
        return this;
    }

    public Sql2 append(String sql) {
        return append(new Sql2(sql));
    }

    public Sql2 appendFormat(String sql, Object... args) {
        return append(String.format(sql, args));
    }

    private void build(StringBuilder sb, Sql2 lhs) {
        if (!StringUtils.isBlank(_sql)) {
            // Add SQL to the String
            if (sb.length() > 0) {
                sb.append("\n");
            }

            String sql = _sql;//!important: necessary to copy string value

            if (Is(lhs, "WHERE ") && Is(this, "WHERE "))
                sql = "AND " + sql.substring(6);
            if (Is(lhs, "ORDER BY ") && Is(this, "ORDER BY "))
                sql = ", " + sql.substring(9);
            // add set clause
            if (Is(lhs, "SET ") && Is(this, "SET "))
                sql = ", " + sql.substring(4);

            sb.append(sql);
        }

        // Now do rhs
        if (_rhs != null) {
            _rhs.build(sb, this);
        }
    }

    private String fixField(String field) {
        int dotIndex = field.indexOf(".");
        if (dotIndex > 0) {
            return field.substring(dotIndex + 1);
        }
        return field;
    }

    private SqlColumn getColumn(String field) {
        if (tableInfo != null) {
            field = fixField(field);
            PocoColumn column = tableInfo.getColumn(field);
            if (column == null) {
                logger.warn("column for field: {} was not exists", field);
                return null;
            }
            String columnName = column.ColumnName;
            return new SqlColumn(column.ColumnName, column.PropertyInfo.getField().getType());
        }
        return null;
    }

    private String getColumnName(String field) {
        if (tableInfo != null) {
            field = fixField(field);
            String columnName = tableInfo.getColumnName(field);
            if (columnName != null) return columnName;
        }
        return field;
    }

    private List<String> getColumnNames(String... fields) {
        List<String> columns = new ArrayList<>();
        for (String field : fields) {
            String columnName = getColumnName(field);
            columns.add(columnName);
        }
        return columns;
    }

    public Sql2 server(ServerType serverType) {
        this.serverType = serverType;
        return this;
    }

    private void checkColumns() {
        if (columns == null) {
            columns = new ArrayList<>();
        }
    }

    public Sql2 select(String... fields) {
        checkColumns();
        if (fields != null && fields.length > 0) {
            columns.addAll(getColumnNames(fields));
        }
        if (columns.size() == 0) {
            columns.add("*");
        }
        String selects = StringUtils.join(columns, ",");
        return append("SELECT " + selects);
    }

    public Sql2 column(String field) {
        checkColumns();
        String columnName = getColumnName(field);
        if (columnName != null) {
            columns.add(columnName);
        }
        return this;
    }

    public Sql2 of(Class<?> model) {
        this.fromModel = true;
        this.tableInfo = PojoData.forClass(model).getTableInfo();
        return this;
    }

    public Sql2 from(Class<?> model) {
        return from(model, null);
    }

    public Sql2 from(Class<?> model, String alias) {
        this.fromModel = true;
        this.tableInfo = PojoData.forClass(model).getTableInfo();
        String sql = "FROM " + this.tableInfo.getTableName();
        if (!StringUtils.isBlank(alias)) {
            sql += " " + alias;
        }
        return append(sql);
    }

    public Sql2 from(String table) {
        return append("FROM " + table);
    }

    public Sql2 set(String field, Object value) {
        String columnName;
        SqlColumn column = getColumn(field);
        if (column != null) {
            columnName = column.columnName;
        } else {
            if (fromModel) {
                return this;
            }
            columnName = field;
        }
        args.put(columnName, value);
        return append("SET " + columnName + " = :" + columnName);
    }

    public Sql2 where(String field, byte op, Object value) {
        String val = value != null ? value.toString() : "";
        boolean isEq = op == eq;
        boolean isEmpty = (value == null || val.equals(""));
        if (!isEq && !isEmpty && value instanceof String) {
            val = SqlFilter.filter(val, true);
            if (StringUtils.isBlank(val)) {
                logger.warn("sql injection detected!!! field={},value={}", field, value);
                return this;
            }
        }
        String columnName;
        Class<?> fieldType;
        SqlColumn column = getColumn(field);
        if (column != null) {
            columnName = column.columnName;
            fieldType = column.dataType;
        } else {
            if (fromModel) {
                return this;
            }
            columnName = field;
            fieldType = (value != null ? value.getClass() : String.class);
        }

        String sql;
        String opStr = " = ";
        if (isEq && isEmpty) {
            sql = columnName + " IS NULL ";
        } else {
            if (fieldType == String.class) {
                switch (op) {
                    case eq:
                        opStr = " = ";
                        break;
                    case like:
                        opStr = " like ";
                        value = "%" + value + "%";
                        break;
                    case llike:
                        opStr = " like ";
                        value = "%" + value;
                        break;
                    case rlike:
                        opStr = " like ";
                        value = value + "%";
                        break;
                }
                if (op == in) {
                    val = StringUtils.join(StringUtils.split(val,","), "','");
                    sql = columnName + " IN ('" + val + "')";
                } else {
                    sql = columnName + opStr + ":" + columnName;
                    args.put(columnName, value);
                }
            } else {
                switch (op) {
                    case eq:
                        opStr = " = ";
                        break;
                    case neq:
                        opStr = " <> ";
                        break;
                    case gt:
                        opStr = " > ";
                        break;
                    case ge:
                        opStr = " >= ";
                        break;
                    case lt:
                        opStr = " < ";
                        break;
                    case le:
                        opStr = " <= ";
                        break;
                }
                if (op == in) {
                    val = StringUtils.join(StringUtils.split(val,","), ",");
                    sql = columnName + " IN (" + val + ")";
                } else {
                    sql = columnName + opStr + ":" + columnName;
                    args.put(columnName, value);
                }
            }
        }
        return append("WHERE (" + sql + ")");
    }

    public Sql2 where(String field, Object value) {
        return where(field, eq, value);
    }

    public Sql2 whereLike(String field, Object value) {
        return where(field, like, value);
    }

    public Sql2 whereLLike(String field, Object value) {
        return where(field, llike, value);
    }

    public Sql2 whereRLike(String field, Object value) {
        return where(field, rlike, value);
    }

    public Sql2 whereIn(String field, Object value) {
        return where(field, in, value);
    }

    public Sql2 whereFormat(String sql, Object... args) {
        sql = String.format(sql, args);
        return append("WHERE (" + sql + ")");
    }

    public Sql2 orderBy(String... columns) {
        if (columns == null || columns.length == 0 || StringUtils.isBlank(columns[0])) {
            return append("ORDER BY NULL");
        }
        List<String> columnList = new ArrayList<>();
        for (String column : columns) {
            String[] arr = column.trim().split(" ");
            if (arr.length == 0) continue;
            String field = arr[0];
            String columnName = getColumnName(field);
            if (arr.length == 2) {
                columnList.add(columnName + " " + arr[1].toUpperCase());
            } else {
                columnList.add(columnName);
            }
        }
        return append("ORDER BY " + StringUtils.join(columnList, ","));
    }

    public Sql2 groupBy(String... fields) {
        List<String> columns = getColumnNames(fields);
        return append("GROUP BY " + StringUtils.join(columns, ","));
    }

    public Sql2 having(String sql) {
        return append("HAVING " + sql);
    }

    public Sql2 forUpdate(String sql) {
        return append(" FOR UPDATE ");
    }

    private SqlJoinClause join(String joinType, String table, String alias) {
        String sql = joinType + table;
        if (!StringUtils.isBlank(alias)) {
            sql += " " + alias;
        }
        return new SqlJoinClause(append(sql));
    }

    private SqlJoinClause join(String JoinType, Class<?> model, String alias) {
        return join(JoinType, getTableName(model), alias);
    }

    public SqlJoinClause innerJoin(String table, String alias) {
        return join("INNER JOIN ", table, alias);
    }

    public SqlJoinClause leftJoin(String table, String alias) {
        return join("LEFT JOIN ", table, alias);
    }

    public SqlJoinClause rightJoin(String table, String alias) {
        return join("RIGHT JOIN ", table, alias);
    }

    public SqlJoinClause fullJoin(String table, String alias) {
        return join("FULL JOIN ", table, alias);
    }

    public SqlJoinClause innerJoin(Class<?> model, String alias) {
        return join("INNER JOIN ", model, alias);
    }

    public SqlJoinClause leftJoin(Class<?> model, String alias) {
        return join("LEFT JOIN ", model, alias);
    }

    public SqlJoinClause rightJoin(Class<?> model, String alias) {
        return join("RIGHT JOIN ", model, alias);
    }

    public SqlJoinClause fullJoin(Class<?> model, String alias) {
        return join("FULL JOIN ", model, alias);
    }

    public class Op {
        /**
         * 使用byte类型替换枚举，性能最好，其次是char。int也差距稍大
         * 测试发现枚举性能太低，与byte有几十上百倍差距
         */
        public static final byte eq = 0;
        public static final byte neq = 1;
        public static final byte gt = 2;
        public static final byte lt = 3;
        public static final byte ge = 4;
        public static final byte le = 5;
        public static final byte like = 6;
        public static final byte rlike = 7;
        public static final byte llike = 8;
        public static final byte in = 9;
        public static final byte between = 10;
        public static final byte nvl = 11;
        public static final byte notnvl = 12;

//    public static final char eq = '0';
//    public static final char neq = '1';
//    public static final char gt = '2';
//    public static final char lt = '3';
//    public static final char ge = '4';
//    public static final char le = '5';
//    public static final char like = '6';
//    public static final char rlike = '7';
//    public static final char llike = '8';
//    public static final char in = '9';
//    public static final char between = 'A';
//    public static final char nvl = 'B';
//    public static final char notnvl = 'C';
    }

    public class SqlJoinClause {

        private Sql2 __sql;

        public SqlJoinClause(Sql2 sql) {
            __sql = sql;
        }

        public Sql2 on(String onClause) {
            return __sql.append("ON " + onClause);
        }

        public Sql2 on(String left, String right) {
            return __sql.append("ON " + left + " = " + right);
        }
    }

    public class SqlColumn {
        public String columnName;
        public Class dataType;

        public SqlColumn(String columnName, Class<?> type) {
            this.columnName = columnName;
            this.dataType = type;
        }
    }
}
