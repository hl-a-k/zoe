package com.zoe.framework.db.schema.query;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * 查询接口
 * Created by caizhicong on 2016/5/17.
 */
public interface QueryHelper {

    Connection getConnection();

    /**
     * 列表查询
     *
     * @param sql    SQL语句
     * @param params 参数
     * @return 列表查询结果集
     */
    List<Map> findMap(String sql, Map<String, Object> params);

    /**
     * 获得结果集
     *
     * @param classz 返回的实体类型
     * @param sql    SQL语句
     * @param params 参数
     * @return 结果集
     */
    <A> A findFirst(Class<A> classz, String sql, Map<String, Object> params);

    /**
     * 获得结果集
     *
     * @param classz 返回的实体类型
     * @param sql    SQL语句
     * @param params 参数
     * @return 结果集
     */
    <A> List<A> findList(Class<A> classz, String sql, Map<String, Object> params);
}
