package com.zoe.framework.sqlbag;

import com.zoe.framework.sqlbag.util.BeetlSqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * sqlbag 抽象类
 * Created by caizhicong on 2015/6/23.
 */
public abstract class SqlBag {

    private static final Logger logger = LoggerFactory.getLogger(SqlBag.class);

    private boolean debug;

    public SqlBag(){
        BeetlSqlUtils.init(this);
    }

    public boolean isDebug() {
        return debug;//true || ;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public abstract String get(String sqlId);

    public abstract void set(String sqlId, String sql);

    public abstract void remove(String sqlId);

    protected String getSqlId(Object host, String sqlId) {
        return host.getClass().getName() + "." + sqlId;
    }

    public String getSqlId(Class clazz, String sqlId) {
        if(clazz == null) return sqlId;
        return clazz.getName() + "." + sqlId;
    }

    public String get(Object host, String sqlId) {
        sqlId = getSqlId(host, sqlId);
        return get(sqlId);
    }

    public String get(Class clazz, String sqlId) {
        sqlId = getSqlId(clazz, sqlId);
        return get(sqlId);
    }

    public String get(String sqlId, Map<String, Object> params) {
        return BeetlSqlUtils.processSql(sqlId, null, params);
    }

    public String get(Object host, String sqlId, Map<String, Object> params) {
        sqlId = getSqlId(host, sqlId);
        return BeetlSqlUtils.processSql(sqlId, null, params);
    }

    public String get(Class clazz, String sqlId, Map<String, Object> params) {
        sqlId = getSqlId(clazz, sqlId);
        return BeetlSqlUtils.processSql(sqlId, null, params);
    }

    public String getText(String sqlId, Map<String, Object> params) {
        return BeetlSqlUtils.processText(sqlId, null, params);
    }

    public String getText(Object host, String sqlId, Map<String, Object> params) {
        sqlId = getSqlId(host, sqlId);
        return BeetlSqlUtils.processText(sqlId, null, params);
    }

    public String getText(Class clazz, String sqlId, Map<String, Object> params) {
        sqlId = getSqlId(clazz, sqlId);
        return BeetlSqlUtils.processText(sqlId, null, params);
    }

    public void remove(Class clazz, String sqlId) {
        sqlId = getSqlId(clazz, sqlId);
        remove(sqlId);
        logger.info("移除SQL缓存:{}", sqlId);
    }

    public void remove(Object host, String sqlId) {
        sqlId = getSqlId(host, sqlId);
        remove(sqlId);
        logger.info("移除SQL缓存:{}", sqlId);
    }
}
