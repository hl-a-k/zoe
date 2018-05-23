package com.zoe.framework.sql4o;

import com.zoe.framework.sql2o.data.PocoColumn;
import com.zoe.framework.sql2o.data.PojoData;
import com.zoe.framework.sql2o.data.TableInfo;
import com.zoe.framework.util.SqlFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Tool for programmatically constructing SQL select statements. This class aims
 * to simplify the task of juggling commas and SQL keywords when building SQL
 * statements from scratch, but doesn't attempt to do much beyond that. Here are
 * some relatively complex examples:
 * <p>
 * <pre>
 * String sql = new SelectBuilder()
 * .column(&quot;e.id&quot;)
 * .column(&quot;e.name as empname&quot;)
 * .column(&quot;d.name as deptname&quot;)
 * .column(&quot;e.salary&quot;)
 * .from((&quot;Employee e&quot;)
 * .join(&quot;Department d on e.dept_id = d.id&quot;)
 * .where(&quot;e.salary &gt; 100000&quot;)
 * .orderBy(&quot;e.salary desc&quot;)
 * .toString();
 * </pre>
 * <p>
 * <pre>
 * String sql = new SelectBuilder()
 * .column(&quot;d.id&quot;)
 * .column(&quot;d.name&quot;)
 * .column(&quot;sum(e.salary) as total&quot;)
 * .from(&quot;Department d&quot;)
 * .join(&quot;Employee e on e.dept_id = d.id&quot;)
 * .groupBy(&quot;d.id&quot;)
 * .groupBy(&quot;d.name&quot;)
 * .having(&quot;total &gt; 1000000&quot;).toString();
 * </pre>
 * <p>
 * Note that the methods can be called in any order. This is handy when a base
 * class wants to create a simple query but allow subclasses to augment it.
 * <p>
 * It's similar to the Squiggle SQL library
 * (http://code.google.com/p/squiggle-sql/), but makes fewer assumptions about
 * the internal structure of the SQL statement, which I think makes for simpler,
 * cleaner code. For example, in Squiggle you would write...
 * <p>
 * <pre>
 * select.addCriteria(new MatchCriteria(orders, &quot;status&quot;, MatchCriteria.EQUALS, &quot;processed&quot;));
 * </pre>
 * <p>
 * With SelectBuilder, we assume you know how to write SQL expressions, so
 * instead you would write...
 * <p>
 * <pre>
 * select.where(&quot;status = 'processed'&quot;);
 * </pre>
 *
 * @author John Krasnay <john@krasnay.ca>
 */
public class Sql2oSelectBuilder implements Cloneable, Serializable {

    private static final long serialVersionUID = 1;
    private static Logger logger = LoggerFactory.getLogger(Sql2oSelectBuilder.class);
    private List<TableInfo> tableInfos = new ArrayList<>();
    private boolean fromModel = false;
    private boolean autoFixField = true;
    private boolean ignoreNullValue = true;
    private SelectBuilder builder = new SelectBuilder();

    public Sql2oSelectBuilder() {

    }

    /**
     * Copy constructor. Used by {@link #clone()}.
     *
     * @param other SelectBuilder being cloned.
     */
    protected Sql2oSelectBuilder(Sql2oSelectBuilder other) {
        this.builder = other.builder.clone();
    }

    public static Sql2oSelectBuilder newInstance() {
        return new Sql2oSelectBuilder();
    }

    public static String getTableName(Class<?> model) {
        return PojoData.forClass(model).getTableInfo().getTableName();
    }

    public SelectBuilder getBuilder() {
        return builder;
    }

    public Map<String, Object> getParameterMap() {
        return builder.getParameterMap();
    }

    public Sql2oSelectBuilder setAutoFixField(boolean autoFixField) {
        this.autoFixField = autoFixField;
        return this;
    }

    public Sql2oSelectBuilder setIgnoreNullValue(boolean ignoreNullValue) {
        this.ignoreNullValue = ignoreNullValue;
        return this;
    }

    /**
     * Alias for {@link #where(String)}.
     */
    public Sql2oSelectBuilder and(String expr) {
        return where(expr);
    }

    public Sql2oSelectBuilder select(String... names) {
        builder.select(names);
        return this;
    }

    public Sql2oSelectBuilder column(String name) {
        builder.columns(name);
        return this;
    }

    public Sql2oSelectBuilder columns(String... names) {
        builder.columns(names);
        return this;
    }

    public Sql2oSelectBuilder column(SubSelectBuilder subSelect) {
        builder.column(subSelect);
        return this;
    }

    public Sql2oSelectBuilder column(String name, boolean groupBy) {
        name = fixColumn(name);
        builder.column(name, groupBy);
        return this;
    }

    @Override
    public Sql2oSelectBuilder clone() {
        return new Sql2oSelectBuilder(this);
    }

    public Sql2oSelectBuilder distinct() {
        builder.distinct();
        return this;
    }

    public Sql2oSelectBuilder forUpdate() {
        builder.forUpdate();
        return this;
    }

    public Sql2oSelectBuilder from(String table) {
        return from(table, null);
    }

    public Sql2oSelectBuilder from(String table, String alias) {
        builder.from(table, alias);
        return this;
    }

    public Sql2oSelectBuilder from(Class<?> table) {
        return from(table, null);
    }

    public Sql2oSelectBuilder from(Class<?> table, String alias) {
        fromModel = true;
        TableInfo tableInfo = PojoData.forClass(table).getTableInfo();
        builder.from(tableInfo.getTableName(), alias);
        tableInfos.add(tableInfo);
        return this;
    }

    public List<UnionSql2oSelectBuilder> getUnions() {
        List<UnionSql2oSelectBuilder> unions = new ArrayList<UnionSql2oSelectBuilder>();
        for (SelectBuilder unionSB : builder.getUnions()) {
            unions.add(new UnionSql2oSelectBuilder(this, unionSB));
        }
        return unions;
    }

    public Sql2oSelectBuilder groupBy(String name) {
        builder.groupBy(fixColumn(name));
        return this;
    }

    public Sql2oSelectBuilder groupBys(String... names) {
        for (String name : names) {
            builder.groupBys(fixColumn(name));
        }
        return this;
    }

    public Sql2oSelectBuilder having(String expr) {
        builder.having(expr);
        return this;
    }

    public Sql2oSelectBuilder join(String join) {
        builder.join(join);
        return this;
    }

    public Sql2oSelectBuilder leftJoin(String join) {
        builder.leftJoin(join);
        return this;
    }

    public Sql2oSelectBuilder leftJoin(Class<?> table, String alias, String left, String right) {
        builder.leftJoin(getTableName(table), alias, left, right);
        return this;
    }

    public Sql2oSelectBuilder noWait() {
        builder.noWait();
        return this;
    }

    public Sql2oSelectBuilder orderBy(String name) {
        builder.orderBy(fixColumn(name));
        return this;
    }

    public Sql2oSelectBuilder orderBys(String... names) {
        for (String name : names) {
            builder.orderBy(fixColumn(name));
        }
        return this;
    }

    /**
     * Adds an ORDER BY item with a direction indicator.
     *
     * @param name      Name of the column by which to sort.
     * @param ascending If true, specifies the direction "asc", otherwise, specifies
     *                  the direction "desc".
     */
    public Sql2oSelectBuilder orderBy(String name, boolean ascending) {
        name = fixColumn(name);
        if(name != null) {
            builder.orderBy(name, ascending);
        }
        return this;
    }

    /**
     * Adds a "union" select builder. The generated SQL will union this query
     * with the result of the main query. The provided builder must have the
     * same columns as the parent select builder and must not use "order by" or
     * "for update".
     */
    public Sql2oSelectBuilder union(Sql2oSelectBuilder unionBuilder) {
        SelectBuilder unionSelectBuilder = new SelectBuilder();
        unionBuilder.getBuilder().union(unionSelectBuilder);
        return this;
    }

    public SubSql2oSelectBuilder subSelectColumn(String alias) {
        SubSelectBuilder subSelectBuilder = new SubSelectBuilder(alias);
        builder.column(subSelectBuilder);
        return new SubSql2oSelectBuilder(this, subSelectBuilder);
    }

    public String toSQL(){
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(builder.toString());
        List<String> params = new ArrayList<String>(builder.getParameterMap().keySet());
        Collections.sort(params);
        for (String s : params) {
            sb.append(", ").append(s).append("=").append(builder.getParameterMap().get(s));
        }
        return sb.toString();
    }

    public UnionSql2oSelectBuilder union() {
        SelectBuilder unionSelectBuilder = new SelectBuilder();
        builder.union(unionSelectBuilder);
        return new UnionSql2oSelectBuilder(this, unionSelectBuilder);
    }

    public Sql2oSelectBuilder where(String expr) {
        builder.where(expr);
        return this;
    }

    public Sql2oSelectBuilder and(Predicate predicate) {
        builder.where(predicate);
        return this;
    }

    public Sql2oSelectBuilder where(Predicate predicate) {
        builder.where(predicate);
        return this;
    }

    public Sql2oSelectBuilder whereEquals(String column, Object value) {
        column = fixColumn(column);
        builder.whereEquals(column, value);
        return this;
    }

    public Sql2oSelectBuilder whereIn(String column, List<?> values) {
        column = fixColumn(column);
        builder.whereIn(column, values);
        return this;
    }

    private String fixField(String field) {
        if (!autoFixField) return field;
        int dotIndex = field.indexOf(".");
        if (dotIndex > 0) {
            return field.substring(dotIndex + 1);
        }
        return field;
    }

    private String fixColumn(String field) {
        SqlColumn column = getColumn(field);
        if (column != null) return column.getColumnName();
        return field;
    }

    private SqlColumn getColumn(String field) {
        if (!autoFixField) return null;
        if(field == null || field.isEmpty()) return null;
        String alias = null;
        int dotIndex = field.indexOf(".");
        if (dotIndex > 0) {
            alias = field.substring(0, dotIndex);
            field = field.substring(dotIndex + 1);
        }
        for (TableInfo tableInfo : tableInfos) {
            PocoColumn column = tableInfo.getColumn(field);
            if (column == null && (autoFixField && (column = tableInfo.getColumns().get(field.toUpperCase())) == null)) {
                logger.warn("column for field: {} was not exists", field);
                return null;
            }
            return new SqlColumn(alias, column.ColumnName, column.PropertyInfo.getField().getType());
        }
        return null;
    }

    private String getColumnName(String field) {
        field = fixField(field);
        for (TableInfo tableInfo : tableInfos) {
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

    private String getArgName(String argName, boolean fixArg) {
        if (fixArg) {
            argName = fixField(argName);
        }
        //统一使用方法 allocateParameter 来获取参数名，省的判断参数是否已存在
        return builder.allocateParameter(argName);
    }

    public Sql2oSelectBuilder where(String field, byte op, Object value) {
        boolean isEmpty = (value == null || value.toString().isEmpty());
        if(ignoreNullValue && isEmpty && (op != Op.nvl && op != Op.notnvl)) return this;
        boolean isInOrNotIn = op == Op.in || op == Op.notin;
        boolean valueIsString = value instanceof String;
        String val = valueIsString ? value.toString() : "";
        if (isInOrNotIn && valueIsString) {//check sql injection for in
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
            columnName = column.getColumnName();
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
        if (op == Op.eq && isEmpty) {
            sql = columnName + " IS NULL ";
        } else if (op == Op.nvl) {
            sql = columnName + " IS NULL ";
        } else if (op == Op.notnvl) {
            sql = columnName + " IS NOT NULL ";
        } else {
            if (fieldType == String.class) {
                if (op == Op.like) {
                    opStr = " like ";
                    if (val.contains("%")) {
                        val = val.replace("%", "\\%");
                    } else if (val.contains("_")) {
                        val = val.replace("_", "\\_");
                    }
                    value = "%" + val + "%";
                } else if (op == Op.llike) {
                    opStr = " like ";
                    value = "%" + value;
                } else if (op == Op.rlike) {
                    opStr = " like ";
                    value = value + "%";
                }
                if (isInOrNotIn) {//[in|not in] 就不使用参数化了
                    if (valueIsString) {
                        val = StringUtils.join(StringUtils.split(val, ","), "','");
                    } else if (value instanceof List) {
                        val = StringUtils.join(value, "','");
                    }
                    sql = columnName + (op == Op.notin ? " NOT" : "") + " IN ('" + val + "')";
                } else {
                    String argName = getArgName(columnName, true);
                    sql = columnName + opStr + ":" + argName;
                    builder.setParameter(argName, value);
                }
            } else {
                if (op == Op.neq) {
                    opStr = " <> ";
                } else if (op == Op.gt) {
                    opStr = " > ";
                } else if (op == Op.ge) {
                    opStr = " >= ";
                } else if (op == Op.lt) {
                    opStr = " < ";
                } else if (op == Op.le) {
                    opStr = " <= ";
                }
                if (isInOrNotIn) {//[in|not in] 就不使用参数化了
                    if (valueIsString) {
                        val = StringUtils.join(StringUtils.split(val, ","), ",");
                    } else if (value instanceof List) {
                        val = StringUtils.join(value, ",");
                    }
                    sql = columnName + (op == Op.notin ? " NOT" : "") + " IN (" + val + ")";
                } else {
                    String argName = getArgName(columnName, true);
                    sql = columnName + opStr + ":" + argName;
                    builder.setParameter(argName, value);
                }
            }
        }
        builder.where("(" + sql + ")");
        return this;
    }

    public Sql2oSelectBuilder where(String field, Object value) {
        return where(field, Op.eq, value);
    }

    public Sql2oSelectBuilder whereLike(String field, Object value) {
        return where(field, Op.like, value);
    }

    public Sql2oSelectBuilder whereLLike(String field, Object value) {
        return where(field, Op.llike, value);
    }

    public Sql2oSelectBuilder whereRLike(String field, Object value) {
        return where(field, Op.rlike, value);
    }

    public Sql2oSelectBuilder whereIn(String field, Object value) {
        return where(field, Op.in, value);
    }

    public Sql2oSelectBuilder whereIsNull(String field) {
        return where(field, Op.nvl, null);
    }

    public Sql2oSelectBuilder whereIsNotNull(String field) {
        return where(field, Op.notnvl, null);
    }

    public Sql2oSelectBuilder whereFormat(String sql, Object... args) {
        sql = String.format(sql, args);
        builder.where("(" + sql + ")");
        return this;
    }

//    public class SqlJoinClause {
//
//        private Sql2oSelectBuilder __sql;
//
//        public SqlJoinClause(Sql2oSelectBuilder sql) {
//            __sql = sql;
//        }
//
//        public Sql2oSelectBuilder on(String onClause) {
//            __sql.joins.add("ON " + onClause);
//            return __sql;
//        }
//
//        public Sql2oSelectBuilder on(String left, String right) {
//            __sql.joins.add("ON " + left + " = " + right);
//            return __sql;
//        }
//    }

    public class SqlColumn {
        public String alias;
        public Class dataType;
        private String columnName;

        public SqlColumn(String columnName, Class<?> type) {
            this.columnName = columnName;
            this.dataType = type;
        }

        public SqlColumn(String alias, String columnName, Class<?> type) {
            this.alias = alias;
            this.columnName = columnName;
            this.dataType = type;
        }

        public String getColumnName() {
            if (alias != null) return alias + "." + columnName;
            return columnName;
        }
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
        public static final byte notin = 10;
        public static final byte between = 11;
        public static final byte nvl = 12;
        public static final byte notnvl = 13;

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
}