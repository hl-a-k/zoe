package com.zoe.framework.sql2o;

import com.zoe.framework.sql2o.converters.Converter;
import com.zoe.framework.sql2o.converters.ConverterException;
import com.zoe.framework.sql2o.logging.LocalLoggerFactory;
import com.zoe.framework.sql2o.logging.Logger;
import com.zoe.framework.sql2o.quirks.Quirks;
import com.zoe.framework.sql2o.tools.SqlTypeUtils;

import java.io.InputStream;
import java.sql.*;
import java.util.Date;

import static com.zoe.framework.sql2o.converters.Convert.throwIfNull;

/**
 * Created by caizhicong on 2016/2/19.
 */
public class CallableQuery implements AutoCloseable {
    private final static Logger logger = LocalLoggerFactory
            .getLogger(Query.class);

    private Connection connection;
    private final CallableStatement statement;
    private boolean caseSensitive;
    private boolean autoDeriveColumnNames;
    private boolean throwOnMappingFailure = true;
    private String name;
    private final CallableParameters addedParameters;
    private final String parsedQuery;

    public CallableQuery(Connection connection, String queryText) {
        this.connection = connection;
        this.caseSensitive = connection.getSql2o().isDefaultCaseSensitive();

        addedParameters = CallableParameters.New();

        parsedQuery = queryText;
        try {
            statement = connection.getJdbcConnection().prepareCall(
                    parsedQuery);
        } catch (SQLException ex) {
            throw new Sql2oException(String.format(
                    "Error preparing statement - %s", ex.getMessage()), ex);
        }
        connection.registerStatement(statement);

        throwOnMappingFailure(connection.getSql2o().isThrowOnMappingFailure());
    }

    // ------------------------------------------------
    // ------------- Getter/Setters -------------------
    // ------------------------------------------------

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public CallableQuery setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    public boolean isAutoDeriveColumnNames() {
        return autoDeriveColumnNames;
    }

    public CallableQuery setAutoDeriveColumnNames(boolean autoDeriveColumnNames) {
        this.autoDeriveColumnNames = autoDeriveColumnNames;
        return this;
    }

    public CallableQuery throwOnMappingFailure(boolean throwOnMappingFailure) {
        this.throwOnMappingFailure = throwOnMappingFailure;
        return this;
    }

    public boolean isThrowOnMappingFailure() {
        return throwOnMappingFailure;
    }

    public Connection getConnection() {
        return this.connection;
    }

    private String getFormatedName() {
        if (name == null) {
            name = "CallableQuery_" + System.currentTimeMillis();
        }
        return "CallableQuery Name：" + name;
    }

    public String getName() {
        if (name == null) {
            name = "CallableQuery_" + System.currentTimeMillis();
        }
        return name;
    }

    public CallableQuery setName(String name) {
        this.name = name;
        return this;
    }

    public void close() {
        connection.removeStatement(statement);
        try {
            this.getQuirks().closeStatement(statement);
        } catch (Throwable ex) {
            logger.warn("Could not close statement.", ex);
        }
    }

    public CallableQuery addParameter(int paramIdx, Object value) {
        try {
            return value == null ? addParameter(paramIdx, Object.class, null)
                    : addParameter(paramIdx, (Class<Object>) value.getClass(), value);
        } catch (SQLException e) {
            this.connection.onException();
            throw new Sql2oException(
                    "Database error occurred while running addParameter: "
                            + e.getMessage(), e);
        }
    }

    private Object convertParameter(Object value) {
        if (value == null) {
            return null;
        }
        Converter converter = getQuirks().converterOf(value.getClass());
        if (converter == null) {
            // let's try to add parameter AS IS
            return value;
        }
        return converter.toDatabaseParam(value);
    }

    public <T> CallableQuery addParameter(CallableParameter parameter) {
        try {
            addedParameters.add(parameter);
            if (parameter.isOutParameter()) {
                statement.registerOutParameter(parameter.getIndex(), SqlTypeUtils.getSqlType(parameter.getParameterType()));
                return this;
            }
            return this.addParameter(parameter.getIndex(), parameter.getParameterType(), parameter.getValue());
        } catch (SQLException e) {
            this.connection.onException();
            throw new Sql2oException(
                    "Database error occurred while running addParameter: "
                            + e.getMessage(), e);
        }
    }

    public <T> CallableQuery addParameter(int paramIdx, Class<T> parameterClass, T value) throws SQLException {
        // TODO: must cover most of types:
        // BigDecimal,Boolean,SmallInt,Double,Float,byte[]
        if (InputStream.class.isAssignableFrom(parameterClass)) {
            statement.setBinaryStream(paramIdx, (InputStream) value);
            return this;
        }
        if (Integer.class == parameterClass) {
            statement.setInt(paramIdx, (Integer) value);
            return this;
        }
        if (Long.class == parameterClass) {
            statement.setLong(paramIdx, (Long) value);
            return this;
        }
        if (String.class == parameterClass) {
            statement.setString(paramIdx, (String) value);
            return this;
        }
        if (Date.class == parameterClass) {
            statement.setDate(paramIdx, new java.sql.Date(((Date) value).getTime()));
            return this;
        }
        if (Timestamp.class == parameterClass) {
            statement.setTimestamp(paramIdx, (Timestamp) value);
            return this;
        }
        if (Time.class == parameterClass) {
            statement.setTime(paramIdx, (Time) value);
            return this;
        }

        final Object convertedValue = convertParameter(value);
        statement.setObject(paramIdx, convertedValue);
        return this;
    }

    public void executeScalar() {
        long start = System.currentTimeMillis();
        try {
            logExecution();
            statement.execute();
            if (addedParameters.size() > 0) {
                for (CallableParameter parameter : addedParameters.getParameters()) {
                    if (parameter.isOutParameter()) {
                        parameter.setValue(statement.getObject(parameter.getIndex()));
                        /*Class parameterClass = parameter.getParameterType();
                        if (Integer.class == parameterClass) {
                            parameter.setValue(statement.getInt(parameter.getIndex()));
                        } else if (Long.class == parameterClass) {
                            parameter.setValue(statement.getLong(parameter.getIndex()));
                        } else if (Double.class == parameterClass) {
                            parameter.setValue(statement.getDouble(parameter.getIndex()));
                        } else if (String.class == parameterClass) {
                            parameter.setValue(statement.getString(parameter.getIndex()));
                        } else if (Date.class == parameterClass) {
                            parameter.setValue(statement.getDate(parameter.getIndex()));
                        }*/
                    }
                }
            }
            long end = System.currentTimeMillis();
            logger.debug(
                    "total: {} ms; executed scalar [{}]",
                    new Object[]{
                            end - start,
                            this.getName()});
        } catch (SQLException e) {
            this.connection.onException();
            throw new Sql2oException(
                    "Database error occurred while running executeScalar: "
                            + e.getMessage(), e);
        } finally {
            closeConnectionIfNecessary();
        }
    }

    public Object executeScalar(int sqlType) {
        long start = System.currentTimeMillis();
        try {
            logExecution();
            statement.registerOutParameter(1, sqlType);
            statement.execute();
            Object o = statement.getObject(1);
            long end = System.currentTimeMillis();
            logger.debug(
                    "total: {} ms; executed scalar [{}]",
                    new Object[]{
                            end - start,
                            this.getName()});
            return o;
        } catch (SQLException e) {
            this.connection.onException();
            throw new Sql2oException(
                    "Database error occurred while running executeScalar: "
                            + e.getMessage(), e);
        } finally {
            closeConnectionIfNecessary();
        }
    }

    private Quirks getQuirks() {
        return this.connection.getSql2o().getQuirks();
    }

    public <V> V executeScalar(Class<V> returnType) {
        try {
            Converter<V> converter;
            // noinspection unchecked
            converter = throwIfNull(returnType,
                    getQuirks().converterOf(returnType));
            // noinspection unchecked
            int sqlType = SqlTypeUtils.getSqlType(returnType);
            return executeScalar(converter, sqlType);
        } catch (ConverterException e) {
            throw new Sql2oException(
                    "Error occured while converting value from database to type "
                            + returnType, e);
        }
    }

    public <V> V executeScalar(Converter<V> converter, int sqlType) {
        try {
            // noinspection unchecked
            return converter.convert(executeScalar(sqlType));
        } catch (ConverterException e) {
            throw new Sql2oException(
                    "Error occured while converting value from database", e);
        }
    }

    /**************
     * private stuff
     ***************/

    private void closeConnectionIfNecessary() {
        try {
            if (connection.autoClose) {
                connection.close();
            }
        } catch (Exception ex) {
            throw new Sql2oException(
                    "Error while attempting to close connection", ex);
        }
    }

    private void logExecution() {
        logger.debug("Executing query: {}{}{}", new Object[]{this.getName(), System.lineSeparator(), this.parsedQuery});
    }
}
