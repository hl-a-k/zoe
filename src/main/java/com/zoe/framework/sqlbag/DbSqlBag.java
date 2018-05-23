package com.zoe.framework.sqlbag;

import com.zoe.framework.sqlbag.util.JdbcUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by caizhicong on 2016/7/12.
 */
public class DbSqlBag extends SqlBag {
    /**
     * 缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "zoe_dynamic_sql_cache:";
    private static final Logger logger = LoggerFactory.getLogger(DbSqlBag.class);
    private DataSource dataSource;
    private String getSqlContentBySqlCode = "select sql_content from sys_dynamic_sql where sql_code = ? and valid_flag = '1'";
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setGetSqlContentBySqlCode(String getSqlContentBySqlCode) {
        this.getSqlContentBySqlCode = getSqlContentBySqlCode;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Cache getCache() {
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(CACHE_KEY_PREFIX);
            return cache;
        }
        return null;
    }

    /**
     * 获取报表语法（优先读取缓存）
     *
     * @param sqlName
     * @return
     * @throws Exception
     */
    private String tryGetFromCache(String sqlName) {
        if (isDebug()) {
            return getSqlByCode(sqlName);
        } else {
            Cache cache = getCache();
            if (cache != null) {
                String value = cache.get(sqlName, String.class);
                if (value != null) {
                    return value;
                }
            }

            String sqlString = getSqlByCode(sqlName);
            if (cache != null && !StringUtils.isBlank(sqlString)) {
                cache.put(sqlName, sqlString);
            }
            return sqlString;
        }
    }

    @Override
    public String get(String sqlId) {
        return tryGetFromCache(sqlId);
    }

    private String getSqlByCode(String sqlId) {
        String sql = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getDataSource().getConnection();
            ps = conn.prepareStatement(getSqlContentBySqlCode);
            ps.setString(1, sqlId);

            // Execute query
            rs = ps.executeQuery();

            // Loop over results - although we are only expecting one result, since sql_name should be unique
            boolean foundResult = false;
            while (rs.next()) {

                // Check to ensure only one row is processed
                if (foundResult) {
                    throw new RuntimeException("More than one sql row found for sql_name [" + sqlId + "]. sql_name must be unique.");
                }

                byte[] bytes = rs.getBytes(1);
                if (bytes != null) {
                    sql = new String(bytes, "UTF-8");
                }
                foundResult = true;
            }
            if (StringUtils.isBlank(sql)) {
                if (logger.isErrorEnabled()) {
                    logger.error("sql content should not be empty! sql_code [" + sqlId +"] ");
                }
                throw new RuntimeException("sql content should not be empty! sql_code [" + sqlId +"] ");
            }
        } catch (SQLException e) {
            final String message = "There was a SQL error while get sql_content by sql_name [" + sqlId + "]";
            if (logger.isErrorEnabled()) {
                logger.error(message, e);
            }
            // Rethrow any SQL errors as an runtime exception
            // throw new SQLException(message, e);
        } catch (UnsupportedEncodingException e) {
            final String message = "There was a encoding error while get sql_content by sql_name [" + sqlId + "]";
            if (logger.isErrorEnabled()) {
                logger.error(message, e);
            }
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            JdbcUtils.closeConnection(conn);
        }
        return sql;
    }

    @Override
    public void set(String sqlId, String sql) {
        Cache cache = getCache();
        if (cache != null) {
            cache.put(sqlId, sql);
        }
    }

    @Override
    public void remove(String sqlId) {
        Cache cache = getCache();
        if (cache != null) {
            cache.evict(sqlId);
        }
    }
}
