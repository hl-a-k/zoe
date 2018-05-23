package com.zoe.framework.sql2o;

import com.zoe.framework.regex.Match;
import com.zoe.framework.sql2o.paging.PagingHelper;
import com.zoe.framework.sql2o.paging.SQLParts;
import com.zoe.framework.sql2o.quirks.OracleQuirks;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by caizhicong on 2016/12/9.
 */
public class SqlTest {

    private String parseSql(String sql) {
        Map<String, List<Integer>> mapToFill = new HashMap<>();
        OracleQuirks quirks = new OracleQuirks();
        String parsedQuery = quirks.getSqlParameterParsingStrategy().parseSql(sql, mapToFill);
        return parsedQuery;
    }

    @Test
    public void testSqlParse1() {
        String sql = "SELECT id, description, duedate " +
                "FROM tasks " +
                "WHERE duedate >= :fromDate AND duedate < :toDate";
        System.out.println(parseSql(sql));

        sql = "SELECT id, description, duedate " +
                "FROM tasks " +
                "WHERE duedate >= :0 AND duedate < :1";
        System.out.println(parseSql(sql));
    }

    @Test
    public void test01() {
        String sql = "SELECT COUNT(*) FROM\n" +
                "\tpatient_info_record p\n" +
                "INNER JOIN disease_manage_master d ON p.ID = d.USER_ID\n" +
                "WHERE 1 = 1 \n" +
                "AND D.VALID_FLAG = '1'\n" +
                "AND P.VALID_FLAG ='1'\n" +
                " AND p.`NAME` LIKE :name\n" +
                " ORDER BY d.DIAGNOSIS_TIME DESC";
        long t1 = System.currentTimeMillis();
        Match m = new PagingHelper().rxOrderBy.Match(sql);
        long t2 = System.currentTimeMillis();
        System.out.println(t2-t1);
    }
}
