package com.zoe.framework.sql2o.quirks;

import com.zoe.framework.sql2o.converters.Converter;
import com.zoe.framework.sql2o.paging.IPagingHelper;
import com.zoe.framework.sql2o.paging.SQLParts;
import com.zoe.framework.sql2o.quirks.parameterparsing.SqlParameterParsingStrategy;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface for JDBC driver specific quirks.
 * See {@link com.zoe.framework.sql2o.quirks.NoQuirks} for defaults.
 *
 * @author aldenquimby@gmail.com
 * @since 4/6/14
 */
public interface Quirks extends Serializable {
    /**
     * @param ofClass
     * @param <E>
     * @return converter for class
     */

    <E> Converter<E> converterOf(Class<E> ofClass);


    /**
     * @return name of column at index {@code colIdx} for result set {@code meta}
     */
    String getColumnName(ResultSetMetaData meta, int colIdx) throws SQLException;

    /**
     * @return true if queries should return generated keys by default, false otherwise
     */
    boolean returnGeneratedKeysByDefault();

    void setParameter(PreparedStatement statement, int paramIdx, Object value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, InputStream value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, int value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, Integer value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, long value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, Long value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, String value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, Timestamp value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, Time value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, boolean value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, Boolean value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, UUID value) throws SQLException;

    void setParameter(PreparedStatement statement, int paramIdx, Array value) throws SQLException;

    Object getRSVal(ResultSet rs, int idx) throws SQLException;

    void closeStatement(Statement statement) throws SQLException;

    SqlParameterParsingStrategy getSqlParameterParsingStrategy();

    String buildPageQuery(SQLParts parts);

    String buildPageQuery(long skip, long take, SQLParts parts);

    String buildPageQuery(long skip, long take, SQLParts parts, List<Object> args);

    void addPageArgs(long skip, long take, Map<String, Object> params);

    String getNowSql();

    /**
     * 获取数据库类型
     *
     * @return 数据库类型
     */
    ServerType getServerType();

    IPagingHelper getPagingHelper();
}
