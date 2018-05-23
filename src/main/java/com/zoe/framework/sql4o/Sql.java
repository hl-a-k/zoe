package com.zoe.framework.sql4o;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * sql 语句拼接辅助类
 *
 * @since 1.0
 * @deprecated on 2.0
 */
@Deprecated
public class Sql {
    public Sql() {
    }

    public Sql(String sql, Object... args) {
        _sql = sql;
        _args = args;
    }

    public Sql(boolean isBuilt, String sql, Object... args) {
        _sql = sql;
        _args = args;
        if (isBuilt) {
            _sqlFinal = _sql;
            _argsFinal = _args;
        }
    }

    public static void main(String[] args) {
        Sql sql2 = Sql.create()
                .Select("ID").From("BASE_DICT")
                .Where("dictName", "test")
                .WhereLike("dictName", "test")
                .Where("validFlag", "1")
                .Where("spellCode", "1")
                .OrderBy("sortNo desc");

        System.out.println(sql2.toString());
        System.out.println(sql2.ArgumentMap());
    }

    /**
     * 开始一个SQL查询，默认Oracle
     *
     * @return
     */
    public static Sql Start() {
        return new Sql();
    }

    public static Sql create() {
        return new Sql();
    }

    String _sql;
    Object[] _args;
    Sql _rhs;
    String _sqlFinal;
    Object[] _argsFinal;
    String _prefix = ":";

    private void Build() {
        // already built?
        if (_sqlFinal != null)
            return;

        // Build it
        StringBuilder sb = new StringBuilder();
        List<Object> args = new ArrayList<Object>();
        Build(sb, args, null);
        _sqlFinal = sb.toString();
        _argsFinal = args.toArray();
    }

    public String SQL() {
        Build();
        return _sqlFinal;
    }

    public String toString() {
        Build();
        return _sqlFinal;
    }

    /**
     * 设置SQL参数前缀
     *
     * @param prefix
     * @return
     */
    public Sql Prefix(String prefix) {
        _prefix = prefix;
        return this;
    }

    /**
     * 获取SQL参数前缀
     *
     * @return
     */
    public String getPrefix() {
        return _prefix;
    }

    public Object[] Arguments() {
        Build();
        return _argsFinal;
    }

    public Map<String, Object> ArgumentMap() {
        Build();
        Map<String, Object> map = new LinkedHashMap<>();
        int i = 0;
        for (Object arg : _argsFinal) {
            map.put(String.valueOf(i++), arg);
        }
        return map;
    }

    int _pageSize;

    public Sql setPageSize(int pageSize) {
        _pageSize = pageSize;
        return this;
    }

    public int getPageSize() {
        return _pageSize;
    }

    int _pageNumber;

    public Sql setPageNumber(int pageNumber) {
        _pageNumber = pageNumber;
        return this;
    }

    public int getPageNumber() {
        return _pageNumber;
    }

    public Sql Append(Sql sql) {
        if (_sqlFinal != null)
            _sqlFinal = null;

        if (_rhs != null) {
            _rhs.Append(sql);
        } else if (_sql != null) {
            _rhs = sql;
        } else {
            _sql = sql._sql;
            _args = sql._args;
            _rhs = sql._rhs;
        }

        return this;
    }

    public Sql Append(String sql, Object... args) {
        return Append(new Sql(sql, args));
    }

    public Sql append(String sql, Object... args) {
        return Append(new Sql(sql, args));
    }

    public Sql AppendFormat(String sql, Object... args) {
        return Append(String.format(sql, args));
    }

    public Sql appendFormat(String sql, Object... args) {
        return Append(String.format(sql, args));
    }

    static boolean Is(Sql sql, String sqltype) {
        return sql != null && sql._sql != null
                && StringUtils.startsWithIgnoreCase(sql._sql, sqltype);
    }

    private void Build(StringBuilder sb, List<Object> args, Sql lhs) {
        if (!StringUtils.isBlank(_sql)) {
            // Add SQL to the String
            if (sb.length() > 0) {
                sb.append("\n");
            }

            //String sql = Sql.ProcessParams(_sql, _args, args, _prefix);
            String sql = _sql;

            if (Is(lhs, "WHERE ") && Is(this, "WHERE "))
                sql = "AND " + sql.substring(6);
            if (Is(lhs, "ORDER BY ") && Is(this, "ORDER BY "))
                sql = ", " + sql.substring(9);

            sb.append(sql);
        }

        // Now do rhs
        if (_rhs != null) {
            _rhs.Prefix(_prefix);
            _rhs.Build(sb, args, this);
        }
    }

    public Sql Where(String sql, Object... args) {
        if (args != null) {
            for (Object arg : args) {
                if (arg == null || StringUtils.isBlank(arg.toString())) {
                    return this;
                }
            }
        }
        return Append(new Sql("WHERE (" + sql + ")", args));
    }

    public Sql WhereLike(String sql, Object arg) {
        if (arg == null || StringUtils.isBlank(arg.toString()))
            return this;
        return Append(new Sql("WHERE (" + sql + ")", "%" + arg + "%"));
    }

    public Sql WhereLLike(String sql, Object arg) {
        if (arg == null || StringUtils.isBlank(arg.toString()))
            return this;
        return Append(new Sql("WHERE (" + sql + ")", "%" + arg));
    }

    public Sql WhereRLike(String sql, Object arg) {
        if (arg == null || StringUtils.isBlank(arg.toString()))
            return this;
        return Append(new Sql("WHERE (" + sql + ")", arg + "%"));
    }

    public Sql WhereFormat(String sql, Object... args) {
        sql = String.format(sql, args);
        return Append(new Sql("WHERE (" + sql + ")", new Object[]{}));
    }

    public Sql OrderBy(String... columns) {
        if (columns == null || StringUtils.isBlank(columns[0])) {
            return Append(new Sql("ORDER BY NULL"));
        }
        return Append(new Sql("ORDER BY " + StringUtils.join(columns, ",")));
    }

    public Sql Select(String... columns) {
        return Append(new Sql("SELECT " + StringUtils.join(columns, ",")));
    }

    public Sql From(String... tables) {
        return Append(new Sql("FROM " + StringUtils.join(tables, ",")));
    }

    public Sql GroupBy(String... columns) {
        return Append(new Sql("GROUP BY " + StringUtils.join(columns, ",")));
    }

    private SqlJoinClause Join(String JoinType, String table) {
        return new SqlJoinClause(Append(new Sql(JoinType + table)).Prefix(
                _prefix));
    }

    public SqlJoinClause InnerJoin(String table) {
        return Join("INNER JOIN ", table);
    }

    public SqlJoinClause LeftJoin(String table) {
        return Join("LEFT JOIN ", table);
    }

    public class SqlJoinClause {
        private Sql __sql;

        public SqlJoinClause(Sql sql) {
            __sql = sql;
        }

        public Sql On(String onClause, Object... args) {
            return __sql.Append("ON " + onClause, args);
        }
    }

    //
    // Helper to handle named parameters from object properties
    private static Pattern rxParams = Pattern.compile("(?<!@)@\\w+");
    private static Pattern rxOracleParams = Pattern.compile("(?<!:):\\w+");
    private static Pattern rxMySQLParams = Pattern.compile("(?<!\\?)\\?\\w+");

    public static String ProcessParams(String _sql, Object[] args_src,
                                       List<Object> args_dest) {
        return ProcessParams(_sql, args_src, args_dest, ":");
    }

    public static String ProcessParams(String _sql, Object[] args_src,
                                       List<Object> args_dest, String parameterPrefix) {
        Pattern regex;
        if ("@".equals(parameterPrefix)) {
            regex = rxParams;
        } else if (":".equals(parameterPrefix)) {
            regex = rxOracleParams;
        } else if ("?".equals(parameterPrefix)) {
            regex = rxMySQLParams;
        } else {
            regex = rxParams;
        }

        Matcher matcher = regex.matcher(_sql);

        StringBuffer sbf = new StringBuffer();
        while (matcher.find()) {
            String paraStr = matcher.group();
            String paramName = paraStr.substring(1);
            // System.out.println(paramName);

            int paramIndex = Integer.parseInt(paramName);
            // Numbered parameter
            if (paramIndex < 0 || paramIndex >= args_src.length)
                try {
                    throw new Exception(
                            String.format(
                                    "Parameter '%s%i' specified but only $i parameters supplied (in `%s`)",
                                    parameterPrefix, paramIndex,
                                    args_src.length, _sql));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            Object arg_val = args_src[paramIndex];
            if (arg_val == null) {
                arg_val = "/*value is null*/";
            }

            String replace = null;
            Class<?> clazz = arg_val.getClass();
            // Expand collections to parameter lists
            if (clazz.isArray() && clazz != String.class) {
                StringBuilder sb = new StringBuilder();
                List tem = new ArrayList();
                for (int i = 0, len = Array.getLength(arg_val); i < len; i++) {
                    Object item = Array.get(arg_val, i);
                    sb.append((sb.length() == 0 ? parameterPrefix : ","
                            + parameterPrefix)
                            + args_dest.size());
                    args_dest.add(item);
                }
                replace = sb.toString();
            } else {
                args_dest.add(arg_val);
                replace = parameterPrefix + (args_dest.size() - 1);
            }
            // _sql = _sql.replace(paraStr, replace);
            matcher.appendReplacement(sbf, replace);
        }
        matcher.appendTail(sbf);
        return sbf.toString();// _sql;
    }
}