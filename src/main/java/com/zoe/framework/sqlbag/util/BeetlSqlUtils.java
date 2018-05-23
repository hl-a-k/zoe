package com.zoe.framework.sqlbag.util;

import com.zoe.framework.sqlbag.SqlBag;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.StringTemplateResourceLoader;
import org.beetl.sql.core.SQLResult;
import org.beetl.sql.core.engine.SQLParameter;
import org.beetl.sql.core.engine.SqlTemplateResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * BeetlSqlUtils
 * Created by caizhicong on 2017/7/6.
 */
public final class BeetlSqlUtils {

    private static final Logger log = LoggerFactory
            .getLogger(BeetlSqlUtils.class);
    /**
     * 用于建立十六进制字符的输出
     */
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};
    private static MessageDigest md5Encoder = null;
    private static GroupTemplate sqlTemplate;
    private static GroupTemplate stringTemplate;

    static {
        try {
            md5Encoder = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.error("BeetlSqlUtils初始化错误！", e);
        }
    }

    public static void init(SqlBag sqlBag) {
        Configuration cfg = getConfiguration();
        if (sqlTemplate == null) {
            SqlTemplateResourceLoader resourceLoader = new SqlTemplateResourceLoader(sqlBag);
            sqlTemplate = new GroupTemplate(resourceLoader, cfg);
        }
        if (stringTemplate == null) {
            StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader();
            stringTemplate = new GroupTemplate(resourceLoader, cfg);
        }
    }

    private static Configuration getConfiguration() {
        Configuration cfg = null;
        try {
            Properties properties = loadDefaultConfig();
            Properties ext = loadExtConfig();
            properties.putAll(ext);

            cfg = new Configuration(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cfg;
    }

    /***
     * 加载cfg自定义配置
     *
     * @return
     */
    private static Properties loadDefaultConfig() {
        Properties ps = new Properties();
        InputStream ins = BeetlSqlUtils.class.getResourceAsStream("/beetlsql.properties");
        if (ins == null) return ps;
        try {
            ps.load(ins);
            ins.close();
        } catch (IOException e) {
            throw new RuntimeException("默认配置文件加载错:/beetlsql.properties");
        }
        return ps;
    }

    private static Properties loadExtConfig() {
        Properties ps = new Properties();
        InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(
                "beetl-ext.properties");
        if (ins == null) {
            return ps;
        }

        try {
            ps.load(ins);
            ins.close();
        } catch (IOException e) {
            throw new RuntimeException("默认配置文件加载错:/beetl-ext.properties");
        }
        return ps;
    }

    /**
     * 处理SQL语句（参数化）
     *
     * @param sqlId  sql语句id
     * @param sql    sql语句模板
     * @param params 参数集合
     * @return 参数化的SQL语句
     */
    public static String processSql(String sqlId, String sql, Map<String, Object> params) {
        return processSql(sqlId, sql, params, true);
    }

    /**
     * 处理SQL文本（直接文本替换，无参数化）
     *
     * @param sql    sql语句模板
     * @param params 参数集合
     * @return 处理后的SQL文本
     */
    public static String processText(String sqlId, String sql, Map<String, Object> params) {
        return processSql(sqlId, sql, params, false);
    }

    private static String processSql(String sqlId, String sql, Map<String, Object> params, boolean sqlParameterized) {
        long t1 = 0;
        if (log.isDebugEnabled()) {
            t1 = System.currentTimeMillis();
        }
        List<SQLParameter> jdbcPara = new LinkedList<>();
        Template t;
        if (sqlId != null) {
            t = sqlTemplate.getTemplate(sqlId);
        } else if (sql != null) {
            sqlId = getSqlId(sql);
            t = stringTemplate.getTemplate(sql);
        } else {
            throw new RuntimeException("sqlId 、 sql 至少指定一个参数");
        }
        if(sqlParameterized) {
            params.put("SYS_USER_PARAMETER", true);
        }
        t.fastBinding(params);
        t.binding("_paras", jdbcPara);

        String jdbcSql = t.render();
        SQLResult result = new SQLResult();
        result.jdbcSql = jdbcSql;
        result.jdbcPara = jdbcPara;
        if (log.isDebugEnabled()) {
            long t2 = System.currentTimeMillis();
            log.debug("process Velocity SQL( sqlId = {} ) takes {} ms.", sqlId, (t2 - t1));
        }
        return result.jdbcSql;
    }

    //region 字节数组转换为十六进制字符串

    public static String getSqlId(String sql) {
        String sqlId;
        try {
            long t1 = System.nanoTime();
            md5Encoder.update(sql.getBytes());
            sqlId = encodeHexString(md5Encoder.digest());
            long t2 = System.nanoTime();
            System.out.println("md5计算耗时：" + (t2 - t1));
        } catch (Exception e) {
            sqlId = String.valueOf(sql.hashCode());
            log.error(e.getMessage(), e);
        }
        return sqlId;
    }

    /**
     * 将字节数组转换为十六进制字符串。
     * <p>
     * 因为使用两个字符表示一个字节，所以返回的的字符串长度将是参数byte[]长度的两倍。
     *
     * @param data 用于转换为十六进制字符的byte[]
     * @return 十六进制字符串
     */
    public static String encodeHexString(final byte[] data) {
        return new String(encodeHex(data, DIGITS_LOWER));
    }

    /**
     * 将字节数组转换为十六进制字符数组。
     * <p>
     * 因为使用两个字符表示一个字节，所以返回的char[]长度将是参数byte[]长度的两倍。
     *
     * @param data     用于转换为十六进制字符的byte[]
     * @param toDigits 用于控制输出的字母表
     * @return 包含十六进制字符的char[]
     */
    private static char[] encodeHex(final byte[] data, final char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    //endregion
}
