package com.zoe.framework.db.schema;

import com.zoe.framework.db.schema.query.QueryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 实现查询接口
 * Created by caizhicong on 2016/5/20.
 */
@Component
public class QueryHelperImpl implements QueryHelper {

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Connection getConnection() {
        try {
            return getJdbcTemplate().getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 列表查询
     *
     * @param sql    SQL语句
     * @param params 参数
     * @return 列表查询结果集
     */
    @Override
    public List<Map> findMap(String sql, Map<String, Object> params) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        return Arrays.asList(namedParameterJdbcTemplate.queryForList(sql, params).toArray(new Map[0]));
    }

    /**
     * 获得结果集
     *
     * @param clazz  返回的实体类型
     * @param sql    SQL语句
     * @param params 参数
     * @return 结果集
     */
    @SuppressWarnings("unchecked")
    @Override
    public <A> A findFirst(Class<A> clazz, String sql, Map<String, Object> params) {
        List<A> list = findList(clazz, sql, params);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 获得结果集
     *
     * @param clazz  返回的实体类型
     * @param sql    SQL语句
     * @param params 参数
     * @return 结果集
     */
    @SuppressWarnings("unchecked")
    @Override
    public <A> List<A> findList(Class<A> clazz, String sql, Map<String, Object> params) {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            if (clazz == String.class) {
                return (A) rs.getString(1);
            }
            if (clazz == Integer.class) {
                return (A) new Integer(rs.getInt(1));
            }
            return null;
        });
    }
}
