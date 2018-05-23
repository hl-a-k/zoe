/*
 * Copyright (c) 2014 Lars Aaberg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.zoe.framework.sql2o.quirks;

import com.zoe.framework.sql2o.converters.Converter;
import com.zoe.framework.sql2o.paging.IPagingHelper;
import com.zoe.framework.sql2o.paging.OraclePagingHelper;
import com.zoe.framework.sql2o.paging.SQLParts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OracleQuirks extends NoQuirks {
    public OracleQuirks() {
        super();
    }

    public OracleQuirks(Map<Class, Converter> converters) {
        super(converters);
    }

    @Override
    public Object getRSVal(ResultSet rs, int idx) throws SQLException {
        Object o = super.getRSVal(rs, idx);
        // oracle timestamps are not always convertible to a java Date. If ResultSet.getTimestamp is used instead of
        // ResultSet.getObject, a normal java.sql.Timestamp instance is returnd.
        if (o != null && o.getClass().getCanonicalName().startsWith("oracle.sql.TIMESTAMP")) {
            //TODO: use TIMESTAMP.dateValue
            o = rs.getTimestamp(idx);
        }
        return o;
    }

    @Override
    public boolean returnGeneratedKeysByDefault() {
        return false;
    }

    @Override
    public String buildPageQuery(SQLParts parts) {
        // 匹配的字符串越长，速度越慢！！！
        parts.sqlSelectRemoved = getPagingHelper().getRxOrderBy().Replace(parts.sqlSelectRemoved, "",
                1);
        if (getPagingHelper().getRxDistinct().IsMatch(parts.sqlSelectRemoved)) {
            parts.sqlSelectRemoved = "peta_inner.* FROM (SELECT "
                    + parts.sqlSelectRemoved + ") peta_inner";
        }

		/* +ALL_ROWS */// 默认方式
        /* +FIRST_ROWS */
        String sqlPage = String
                .format("SELECT * FROM (SELECT peta_paged.*, ROWNUM AS ROWNUMS FROM (select %s %s) peta_paged) WHERE (ROWNUMS > :%s) AND (ROWNUMS <= :%s)",
                        parts.sqlSelectRemoved,
                        parts.sqlOrderBy != null ? parts.sqlOrderBy : "",
                        PAGING_SKIP_KEY, PAGING_TAKE_KEY);
        return sqlPage;
    }

    @Override
    public String buildPageQuery(long skip, long take, SQLParts parts) {
        // 匹配的字符串越长，速度越慢！！！
        parts.sqlSelectRemoved = getPagingHelper().getRxOrderBy().Replace(parts.sqlSelectRemoved, "",
                1);
        if (getPagingHelper().getRxDistinct().IsMatch(parts.sqlSelectRemoved)) {
            parts.sqlSelectRemoved = "peta_inner.* FROM (SELECT "
                    + parts.sqlSelectRemoved + ") peta_inner";
        }

		/* +ALL_ROWS */// 默认方式
        /* +FIRST_ROWS */
        String sqlPage = String
                .format("SELECT * FROM (SELECT peta_paged.*, ROWNUM AS ROWNUMS FROM (select %s %s) peta_paged) WHERE (ROWNUMS > %d ) AND (ROWNUMS <= %d)",
                        parts.sqlSelectRemoved,
                        parts.sqlOrderBy != null ? parts.sqlOrderBy : "",
                        skip,
                        skip + take);
        return sqlPage;
    }

    @Override
    public String buildPageQuery(long skip, long take, SQLParts parts, List<Object> args) {
        /*if (parts.sqlSelectRemoved.startsWith("*")) {
            try {
                throw new Exception(
                        "Query must alias '*' when performing a paged query.\neg. select t.* from table t order by t.id");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }*/

        // 匹配的字符串越长，速度越慢！！！
        parts.sqlSelectRemoved = getPagingHelper().getRxOrderBy().Replace(parts.sqlSelectRemoved, "",
                1);
        if (getPagingHelper().getRxDistinct().IsMatch(parts.sqlSelectRemoved)) {
            parts.sqlSelectRemoved = "peta_inner.* FROM (SELECT "
                    + parts.sqlSelectRemoved + ") peta_inner";
        }

		/* +ALL_ROWS */// 默认方式
        /* +FIRST_ROWS */
        String sqlPage = String
                .format("SELECT * FROM (SELECT peta_paged.*, ROWNUM AS ROWNUMS FROM (select %s %s) peta_paged) WHERE (ROWNUMS > :%d ) AND (ROWNUMS <= :%d)",
                        parts.sqlSelectRemoved,
                        parts.sqlOrderBy != null ? parts.sqlOrderBy : "",
                        args.size(),
                        args.size() + 1);
        args.add(skip);
        args.add(skip + take);
        return sqlPage;
    }

    @Override
    public void addPageArgs(long skip, long take, Map<String, Object> params) {
        params.put(PAGING_SKIP_KEY, skip);
        params.put(PAGING_TAKE_KEY, skip + take);
    }

    @Override
    public String getNowSql() {
        return "select sysdate from dual";
    }

    /**
     * 获取数据库类型
     *
     * @return 数据库类型
     */
    @Override
    public ServerType getServerType() {
        return ServerType.Oracle;
    }

    IPagingHelper pagingHelper = new OraclePagingHelper();

    @Override
    public IPagingHelper getPagingHelper() {
        return pagingHelper;
    }
}
