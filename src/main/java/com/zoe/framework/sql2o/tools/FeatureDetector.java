package com.zoe.framework.sql2o.tools;

/**
 * Detects whether optional sql2o features are available.
 *
 * @author Alden Quimby
 */
@SuppressWarnings("UnusedDeclaration")
public final class FeatureDetector {

    private FeatureDetector()
    {}

    static {
        setCacheUnderscoreToCamelcaseEnabled(true); // enabled by default
    }

    private static Boolean jodaTimeAvailable;
    private static Boolean slf4jAvailable;
    private static Boolean oracleAvailable;
    private static Boolean mysqlAvailable;
    private static boolean cacheUnderscoreToCamelcaseEnabled;
    private static Boolean springJdbcAvailable;

    /**
     * @return {@code true} if Joda-Time is available, {@code false} otherwise.
     */
    public static boolean isJodaTimeAvailable() {
        if (jodaTimeAvailable == null) {
            jodaTimeAvailable = ClassUtils.isPresent("org.joda.time.DateTime");
        }
        return jodaTimeAvailable;
    }

    /**
     * @return {@code true} if Slf4j is available, {@code false} otherwise.
     */
    public static boolean isSlf4jAvailable() {
        if (slf4jAvailable == null) {
            slf4jAvailable = ClassUtils.isPresent("org.slf4j.Logger");
        }
        return slf4jAvailable;
    }

    /**
     * @return {@code true} if oracle.sql is available, {@code false} otherwise.
     */
    public static boolean isOracleAvailable() {
        if (oracleAvailable == null) {
            oracleAvailable = ClassUtils.isPresent("oracle.jdbc.driver.OracleDriver");
            //oracleAvailable = ClassUtils.isPresent("oracle.sql.TIMESTAMP");
        }
        return oracleAvailable;
    }

    /**
     * @return {@code true} if mysql driver is available, {@code false} otherwise.
     */
    public static boolean isMySqlAvailable() {
        if (mysqlAvailable == null) {
            mysqlAvailable = ClassUtils.isPresent("com.mysql.jdbc.Driver");
        }
        return mysqlAvailable;
    }

    /**
     *
     * @return {@code true} if caching of underscore to camelcase is enabled.
     */
    public static boolean isCacheUnderscoreToCamelcaseEnabled() {
        return cacheUnderscoreToCamelcaseEnabled;
    }

    /**
     * Turn caching of underscore to camelcase on or off.
     */
    public static void setCacheUnderscoreToCamelcaseEnabled(boolean cacheUnderscoreToCamelcaseEnabled) {
        FeatureDetector.cacheUnderscoreToCamelcaseEnabled = cacheUnderscoreToCamelcaseEnabled;
    }

    /**
     * 判断是否有引用了 spring-jdbc
     * @return
     */
    public static boolean isSpringJdbcAvailable() {
        if (springJdbcAvailable == null) {
            springJdbcAvailable = ClassUtils.isPresent("org.springframework.jdbc.datasource.DataSourceUtils");
        }
        return springJdbcAvailable;
    }
}