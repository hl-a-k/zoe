package com.zoe.framework.db.schema;

/**
 * 数据类型
 * Created by caizhicong on 2016/5/14.
 */

import java.util.HashMap;

/**
 * <P>The class that defines the constants that are used to identify generic
 * SQL types, called JDBC types.
 * <p>
 * This class is never instantiated.
 */
public enum DbType {


    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BIT</code>.
     */
    BIT(-7),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TINYINT</code>.
     */
    TINYINT(-6),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>SMALLINT</code>.
     */
    SMALLINT(5),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>INTEGER</code>.
     */
    INTEGER(4),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BIGINT</code>.
     */
    BIGINT(-5),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>FLOAT</code>.
     */
    FLOAT(6),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>REAL</code>.
     */
    REAL(7),


    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DOUBLE</code>.
     */
    DOUBLE(8),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>NUMERIC</code>.
     */
    NUMERIC(2),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DECIMAL</code>.
     */
    DECIMAL(3),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>CHAR</code>.
     */
    CHAR(1),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>VARCHAR</code>.
     */
    VARCHAR(12),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>LONGVARCHAR</code>.
     */
    LONGVARCHAR(-1),


    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>DATE</code>.
     */
    DATE(91),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIME</code>.
     */
    TIME(92),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>TIMESTAMP</code>.
     */
    TIMESTAMP(93),


    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>BINARY</code>.
     */
    BINARY(-2),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>VARBINARY</code>.
     */
    VARBINARY(-3),

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * <code>LONGVARBINARY</code>.
     */
    LONGVARBINARY(-4),

    /**
     * <P>The constant in the Java programming language
     * that identifies the generic SQL value
     * <code>NULL</code>.
     */
    NULL(0),

    /**
     * The constant in the Java programming language that indicates
     * that the SQL type is database-specific and
     * gets mapped to a Java object that can be accessed via
     * the methods <code>getObject</code> and <code>setObject</code>.
     */
    OTHER(1111),


    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>JAVA_OBJECT</code>.
     *
     * @since 1.2
     */
    JAVA_OBJECT(2000),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>DISTINCT</code>.
     *
     * @since 1.2
     */
    DISTINCT(2001),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>STRUCT</code>.
     *
     * @since 1.2
     */
    STRUCT(2002),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>ARRAY</code>.
     *
     * @since 1.2
     */
    ARRAY(2003),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>BLOB</code>.
     *
     * @since 1.2
     */
    BLOB(2004),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>CLOB</code>.
     *
     * @since 1.2
     */
    CLOB(2005),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * <code>REF</code>.
     *
     * @since 1.2
     */
    REF(2006),

    /**
     * The constant in the Java programming language, somtimes referred to
     * as a type code, that identifies the generic SQL type <code>DATALINK</code>.
     *
     * @since 1.4
     */
    DATALINK(70),

    /**
     * The constant in the Java programming language, somtimes referred to
     * as a type code, that identifies the generic SQL type <code>BOOLEAN</code>.
     *
     * @since 1.4
     */
    BOOLEAN(16),

    //------------------------- JDBC 4.0 -----------------------------------

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>ROWID</code>
     *
     * @since 1.6
     */
    ROWID(-8),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>NCHAR</code>
     *
     * @since 1.6
     */
    NCHAR(-15),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>NVARCHAR</code>.
     *
     * @since 1.6
     */
    NVARCHAR(-9),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>LONGNVARCHAR</code>.
     *
     * @since 1.6
     */
    LONGNVARCHAR(-16),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>NCLOB</code>.
     *
     * @since 1.6
     */
    NCLOB(2011),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type <code>XML</code>.
     *
     * @since 1.6
     */
    SQLXML(2009),

    //--------------------------JDBC 4.2 -----------------------------

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type {@code REF CURSOR}.
     *
     * @since 1.8
     */
    REF_CURSOR(2012),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * {@code TIME WITH TIMEZONE}.
     *
     * @since 1.8
     */
    TIME_WITH_TIMEZONE(2013),

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * {@code TIMESTAMP WITH TIMEZONE}.
     *
     * @since 1.8
     */
    TIMESTAMP_WITH_TIMEZONE(2014),

    /**
     * @apiNote 由于Java的 java.sql.Types 并没有覆盖 mysql 、oracle 、sqlserver 等数据库的所有数据类型，这里进行拓展
     * mysql 值以 198901开头
     * oracle 值以 198902开头
     * sqlserver 值以 198903开头
     */
    MEDIUMINT(19890101),
    INT(19890102),
    YEAR(19890103),
    DATETIME(19890104),
    TINYBLOB(19890105),
    MEDIUMBLOB(19890106),
    LONGBLOB(19890107),
    TINYTEXT(19890108),
    MEDIUMTEXT(19890109),
    TEXT(19890110),
    LONGTEXT(19890111);

    private int intValue;
    private static HashMap<Integer, DbType> mappings;

    private static HashMap<Integer, DbType> getMappings() {
        if (mappings == null) {
            synchronized (DbType.class) {
                if (mappings == null) {
                    mappings = new HashMap<Integer, DbType>();
                }
            }
        }
        return mappings;
    }

    DbType(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    public int getValue() {
        return intValue;
    }

    public static DbType forValue(int value) {
        return getMappings().get(value);
    }

    /**
     * Gets a value indicating whether this instance is numeric.
     * <p>
     * <value>
     * <c>true</c> if this instance is numeric; otherwise, <c>false</c>.
     * </value>
     */
    public final boolean isNumeric() {
        return getValue() == DbType.INT.getValue()
                || getValue() == DbType.INTEGER.getValue()
                || getValue() == DbType.TINYINT.getValue()
                || getValue() == DbType.SMALLINT.getValue()
                || getValue() == DbType.MEDIUMINT.getValue()
                || getValue() == DbType.BIGINT.getValue()
                || getValue() == DbType.FLOAT.getValue()
                || getValue() == DbType.DOUBLE.getValue()
                || getValue() == DbType.DECIMAL.getValue()
                || getValue() == DbType.NUMERIC.getValue();
    }

    /**
     * Gets a value indicating whether this instance is date time.
     * <p>
     * <value>
     * <c>true</c> if this instance is date time; otherwise, <c>false</c>.
     * </value>
     */
    public final boolean isDate() {
        return getValue() == DbType.DATE.getValue()
                || getValue() == DbType.TIME.getValue()
                || getValue() == DbType.YEAR.getValue()
                || getValue() == DbType.DATETIME.getValue()
                || getValue() == DbType.TIMESTAMP.getValue();
    }

    /**
     * Gets a value indicating whether this instance is string.
     * <p>
     * <value><c>true</c> if this instance is string; otherwise, <c>false</c>.</value>
     */
    public final boolean isString() {
        return  getValue() == DbType.CHAR.getValue()
                ||getValue() == DbType.VARCHAR.getValue()
                || getValue() == DbType.NVARCHAR.getValue()
                || getValue() == DbType.LONGNVARCHAR.getValue()
                || getValue() == DbType.TEXT.getValue()
                || getValue() == DbType.TINYTEXT.getValue()
                || getValue() == DbType.MEDIUMTEXT.getValue()
                || getValue() == DbType.LONGTEXT.getValue();
    }

    /**
     * 判断是否为不需要设置长度的类型
     * @return
     */
    public final boolean isNoNeedLength(){
        return  getValue() == DbType.TEXT.getValue()
                ||getValue() == DbType.TINYTEXT.getValue()
                || getValue() == DbType.MEDIUMTEXT.getValue()
                || getValue() == DbType.LONGTEXT.getValue()
                || getValue() == DbType.BLOB.getValue()
                || getValue() == DbType.TINYBLOB.getValue()
                || getValue() == DbType.MEDIUMBLOB.getValue()
                || getValue() == DbType.LONGBLOB.getValue();
    }
}