package com.zoe.framework.sql2o;

import com.zoe.framework.sql2o.converters.Converter;
import com.zoe.framework.sql2o.converters.ConverterException;
import com.zoe.framework.sql2o.data.*;
import com.zoe.framework.sql2o.logging.LocalLoggerFactory;
import com.zoe.framework.sql2o.logging.Logger;
import com.zoe.framework.sql2o.quirks.Quirks;
import com.zoe.framework.sql2o.quirks.ServerType;
import com.zoe.framework.sql2o.reflection.PojoIntrospector;
import com.zoe.framework.sql2o.reflection.PojoProperty;
import com.zoe.framework.sql2o.tools.FeatureDetector;
import com.zoe.framework.sql2o.util.CastUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.io.Closeable;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;

import static com.zoe.framework.sql2o.converters.Convert.throwIfNull;

/**
 * Represents a connection to the database with a transaction.
 */
public class Connection implements AutoCloseable, Closeable {

    private final static Logger logger = LocalLoggerFactory
            .getLogger(Connection.class);
    // this is thread-safe since static initializer is thread-safe
    private static final boolean springJdbcAvailable = FeatureDetector.isSpringJdbcAvailable();
    final boolean autoClose;
    private final Set<Statement> statements = new HashSet<>();
    private java.sql.Connection jdbcConnection;
    private Sql2o sql2o;
    private Integer result = null;
    private int[] batchResult = null;
    private List<Object> keys;
    private boolean canGetKeys;
    /**
     * 标记是否处于sql2o开启的事务中
     */
    private boolean isInSql2oTransaction = false;
    private boolean rollbackOnException = true;

    Connection(Sql2o sql2o, boolean autoClose) {

        this.autoClose = autoClose;
        this.sql2o = sql2o;
        createConnection();
    }

    public boolean isInSql2oTransaction() {
        return isInSql2oTransaction;
    }

    public void setIsInSql2oTransaction(boolean isInSql2oTransaction) {
        this.isInSql2oTransaction = isInSql2oTransaction;
    }

    public boolean isRollbackOnException() {
        return rollbackOnException;
    }

    public Connection setRollbackOnException(boolean rollbackOnException) {
        this.rollbackOnException = rollbackOnException;
        return this;
    }

    private boolean rollbackOnClose = true;

    public boolean isRollbackOnClose() {
        return rollbackOnClose;
    }

    public Connection setRollbackOnClose(boolean rollbackOnClose) {
        this.rollbackOnClose = rollbackOnClose;
        return this;
    }

    void onException() {
        if (isRollbackOnException()) {
            rollback(this.autoClose);
        }
    }

    public java.sql.Connection getJdbcConnection() {
        return jdbcConnection;
    }

    public Sql2o getSql2o() {
        return sql2o;
    }

    public Query createQuery(String queryText) {
        boolean returnGeneratedKeys = this.sql2o.getQuirks()
                .returnGeneratedKeysByDefault();
        return createQuery(queryText, returnGeneratedKeys);
    }

    public Query createQuery(String queryText, boolean returnGeneratedKeys) {

        try {
            if (jdbcConnection.isClosed()) {
                createConnection();
            }
        } catch (SQLException e) {
            throw new Sql2oException("Error creating connection", e);
        }

        return new Query(this, queryText, returnGeneratedKeys);
    }

    public Query createQuery(String queryText, String... columnNames) {
        try {
            if (jdbcConnection.isClosed()) {
                createConnection();
            }
        } catch (SQLException e) {
            throw new Sql2oException("Error creating connection", e);
        }

        return new Query(this, queryText, columnNames);
    }

    public Query createQueryWithParams(String queryText, Object... paramValues){
        // due to #146, creating a query will not create a statement anymore;
        // the PreparedStatement will only be created once the query needs to be executed
        // => there is no need to handle the query closing here anymore since there is nothing to close
        return createQuery(queryText)
                .withParams(paramValues);
    }

    public Query createQueryWithoutParseQuery(String queryText){
        boolean returnGeneratedKeys = this.sql2o.getQuirks()
                .returnGeneratedKeysByDefault();
        try {
            if (jdbcConnection.isClosed()) {
                createConnection();
            }
        } catch (SQLException e) {
            throw new Sql2oException("Error creating connection", e);
        }

        return new Query(this, queryText, returnGeneratedKeys, false);
    }

    public CallableQuery createCallQuery(String queryText) {
        try {
            if (jdbcConnection.isClosed()) {
                createConnection();
            }
        } catch (SQLException e) {
            throw new Sql2oException("Error creating connection", e);
        }

        return new CallableQuery(this, queryText);
    }

    public Sql2o rollback() {
        return this.rollback(true).sql2o;
    }

    public Connection rollback(boolean closeConnection) {
        try {
            if (isInSql2oTransaction()) {//仅回滚sql2o开启的事务！！！
                jdbcConnection.rollback();
            }
        } catch (SQLException e) {
            logger.warn("Could not roll back transaction. message: {}", e);
        } finally {
            if (closeConnection)
                this.closeJdbcConnection();
        }
        return this;
    }

    public Sql2o commit() {
        return this.commit(true).sql2o;
    }

    public Connection commit(boolean closeConnection) {
        try {
            jdbcConnection.commit();
        } catch (SQLException e) {
            throw new Sql2oException(e);
        } finally {
            if (closeConnection)
                this.closeJdbcConnection();
        }
        return this;
    }

    public int getResult() {
        if (this.result == null) {
            throw new Sql2oException(
                    "It is required to call executeUpdate() method before calling getResult().");
        }
        return this.result;
    }

    void setResult(int result) {
        this.result = result;
    }

    public int[] getBatchResult() {
        if (this.batchResult == null) {
            throw new Sql2oException(
                    "It is required to call executeBatch() method before calling getBatchResult().");
        }
        return this.batchResult;
    }

    void setBatchResult(int[] value) {
        this.batchResult = value;
    }

    public Object getKey() {
        if (!this.canGetKeys) {
            throw new Sql2oException(
                    "Keys where not fetched from database. Please call executeUpdate(true) to fetch keys");
        }
        if (this.keys != null && this.keys.size() > 0) {
            return keys.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    // need to change Convert
    public <V> V getKey(Class returnType) {
        final Quirks quirks = this.sql2o.getQuirks();
        Object key = getKey();
        try {
            Converter<V> converter = throwIfNull(returnType,
                    quirks.converterOf(returnType));
            return converter.convert(key);
        } catch (ConverterException e) {
            throw new Sql2oException(
                    "Exception occurred while converting value from database to type "
                            + returnType.toString(), e);
        }
    }

    public Object[] getKeys() {
        if (!this.canGetKeys) {
            throw new Sql2oException(
                    "Keys where not fetched from database. Please set the returnGeneratedKeys parameter in the createQuery() method to enable fetching of generated keys.");
        }
        if (this.keys != null) {
            return this.keys.toArray();
        }
        return null;
    }

    void setKeys(ResultSet rs) throws SQLException {
        if (rs == null) {
            this.keys = null;
            return;
        }
        this.keys = new ArrayList<Object>();
        while (rs.next()) {
            this.keys.add(rs.getObject(1));
        }
    }

    @SuppressWarnings("unchecked")
    // need to change Convert
    public <V> List<V> getKeys(Class<V> returnType) {
        final Quirks quirks = sql2o.getQuirks();
        if (!this.canGetKeys) {
            throw new Sql2oException(
                    "Keys where not fetched from database. Please set the returnGeneratedKeys parameter in the createQuery() method to enable fetching of generated keys.");
        }

        if (this.keys != null) {
            try {
                Converter<V> converter = throwIfNull(returnType,
                        quirks.converterOf(returnType));

                List<V> convertedKeys = new ArrayList<V>(this.keys.size());

                for (Object key : this.keys) {
                    convertedKeys.add(converter.convert(key));
                }

                return convertedKeys;
            } catch (ConverterException e) {
                throw new Sql2oException(
                        "Exception occurred while converting value from database to type "
                                + returnType.toString(), e);
            }
        }

        return null;
    }

    void setCanGetKeys(boolean canGetKeys) {
        this.canGetKeys = canGetKeys;
    }

    void registerStatement(Statement statement) {
        statements.add(statement);
    }

    void removeStatement(Statement statement) {
        statements.remove(statement);
    }

    public void close() {
        boolean connectionIsClosed;
        try {
            connectionIsClosed = jdbcConnection.isClosed();
        } catch (SQLException e) {
            throw new Sql2oException(
                    "Sql2o encountered a problem while trying to determine whether the connection is closed.",
                    e);
        }

        if (!connectionIsClosed) {

            for (Statement statement : statements) {
                try {
                    getSql2o().getQuirks().closeStatement(statement);
                } catch (Throwable e) {
                    logger.warn("Could not close statement.", e);
                }
            }
            statements.clear();

            boolean rollback = rollbackOnClose;
            try {
                rollback = !jdbcConnection.getAutoCommit();
            } catch (SQLException e) {
                logger.warn("Could not determine connection auto commit mode.",
                        e);
            }

            // if in transaction, rollback, otherwise just close
            if (rollback) {
                this.rollback(true);
            }
            else {
                this.closeJdbcConnection();
            }
        }
    }

    private void createConnection() {
        try {
            if (springJdbcAvailable) {
                this.jdbcConnection = DataSourceUtils.getConnection(this.sql2o.getDataSource());
            } else {
                this.jdbcConnection = this.sql2o.getDataSource().getConnection();
            }
        } catch (Exception ex) {
            throw new Sql2oException(
                    "Could not acquire a connection from DataSource - "
                            + ex.getMessage(), ex);
        }
    }

    private void closeJdbcConnection() {
        try {
            if (springJdbcAvailable) {
                DataSourceUtils.releaseConnection(jdbcConnection, this.sql2o.getDataSource());
            } else {
                jdbcConnection.close();
            }
        } catch (SQLException e) {
            logger.warn("Could not close connection. message: {}", e);
        }
    }

    /**
     * 读取数据库表结构信息
     *
     * @param schemaName  模式名称
     * @param readColumns
     * @return
     */
    public List<Table> readMetaData(String schemaName, boolean readColumns) {
        List<Table> tables = new ArrayList<>();
        try {
            DatabaseMetaData meta = jdbcConnection.getMetaData();
            String[] types = {"TABLE"};
            ResultSet rs = meta.getTables(null, schemaName, "%", types);
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                String tableSchema = rs.getString("TABLE_SCHEM");
                String tableType = rs.getString("TABLE_TYPE");
                String remarks = rs.getString("REMARKS");
                Table table;
                if (readColumns) {
                    table = readMetaData(schemaName, tableName);
                } else {
                    table = new Table(false, sql2o.getQuirks());
                    table.setSchema(schemaName);
                    table.setName(tableName);
                }
                tables.add(table);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    /**
     * 读取数据库表结构信息
     *
     * @param schemaName 模式名称
     * @param tableName
     * @return
     */
    public Table readMetaData(String schemaName, String tableName) {
        Table table = new Table(false, sql2o.getQuirks());
        table.setSchema(schemaName);
        table.setName(tableName);
        List<Column> columns = table.columns();
        List<Column> pkColumns = new ArrayList<>();
        table.setPrimaryKey(pkColumns);
        Map<String, Integer> columnNameToIdxMap = new HashMap<>();
        try {
            DatabaseMetaData meta = jdbcConnection.getMetaData();
            ResultSet rs = meta.getColumns(null, schemaName, tableName, "%");
            int colIdx = 0;
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String columnType = rs.getString("TYPE_NAME");
                //int datasize = rs.getInt("COLUMN_SIZE");
                //int digits = rs.getInt("DECIMAL_DIGITS");
                //int nullable = rs.getInt("NULLABLE");
                int dataType = rs.getInt("DATA_TYPE");//类型
                Column column = new Column(columnName, colIdx, columnType);
                column.setSqlType(dataType);
                columns.add(column);
                columnNameToIdxMap.put(columnName, colIdx);
                colIdx++;
            }
            rs.close();

            ResultSet rsPk = meta.getPrimaryKeys(null, schemaName, tableName);
            while (rsPk.next()) {
                String columnName = rsPk.getString("COLUMN_NAME");//列名
                short keySeq = rsPk.getShort("KEY_SEQ");//序列号(主键内值1表示第一列的主键，值2代表主键内的第二列)
                String pkName = rsPk.getString("PK_NAME"); //主键名称

                Integer index = columnNameToIdxMap.get(columnName);
                if (index != null) {
                    Column column = columns.get(index);
                    column.setIsPrimaryKey(true);
                    pkColumns.add(column);
                }
            }
            rsPk.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return table;
    }
}
