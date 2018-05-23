package com.zoe.framework.db.schema;

import com.zoe.framework.db.schema.Table;

import java.sql.Types;

/**
 * 数据库字段
 * <p/>
 * Created by caizhicong on 2016/3/21.
 */
public class Column implements IDBObject {

    private transient Table table;
    private DbType dbType = DbType.NULL;
    private int dataType = Types.NULL;
    /**
     * 数据库类型名称
     */
    private String typeName;
    /**
     * 映射的代码里的基础数据类型
     */
    private String mappedDataType;
    private int length;
    private boolean _isNullable = true;
    private boolean _isReadOnly;
    private boolean _isComputed;
    private boolean autoIncrement;
    private int scale;
    private int precision;
    private boolean _isPrimaryKey;
    private boolean _isUnique;
    private boolean _isIdentity;
    private boolean _isForeignKey;
    private Object defaultSetting;
    private String comment;
    private String name;
    private String propertyName;

    public Column() {

    }

    public Column(Table table) {
        this("", table);
    }

    public Column(String columnName, Table table) {
        setTable(table);
        setName(columnName);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table value) {
        table = value;
    }

    /**
     * @return
     */
    public DbType getDbType() {
        if (dbType == DbType.NULL) {
            dbType = DbType.forValue(getDataType());
        }
        return dbType;
    }

    public void setDbType(DbType value) {
        dbType = value;
    }

    /**
     * SQL type from java.sql.Types
     *
     * @return
     * @see java.sql.Types
     */
    public int getDataType() {
        if(dataType == Types.NULL){
            dataType = getDbType().getValue();
        }
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String value) {
        typeName = value;
    }

    public String getMappedDataType() {
        return mappedDataType;
    }

    public void setMappedDataType(String value) {
        mappedDataType = value;
    }

    /**
     * 数据长度
     *
     * @return
     */
    public int getLength() {
        return length;
    }

    public void setLength(int value) {
        length = value;
    }

    public boolean isNullable() {
        return _isNullable;
    }

    public void setIsNullable(boolean value) {
        _isNullable = value;
    }

    public boolean isReadOnly() {
        return _isReadOnly;
    }

    public void setIsReadOnly(boolean value) {
        _isReadOnly = value;
    }

    public boolean isComputed() {
        return _isComputed;
    }

    public void setIsComputed(boolean value) {
        _isComputed = value;
    }

    /**
     * 是否为自动增长
     *
     * @return
     */
    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean value) {
        autoIncrement = value;
    }

    /**
     * 小数位数
     *
     * @return
     */
    public int getScale() {
        return scale;
    }

    /**
     * 小数位数
     *
     * @param value
     */
    public void setScale(int value) {
        scale = value;
    }

    /**
     * 精度
     *
     * @return
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * 精度
     *
     * @param value
     */
    public void setPrecision(int value) {
        precision = value;
    }

    public boolean isPrimaryKey() {
        return _isPrimaryKey;
    }

    public void setIsPrimaryKey(boolean value) {
        _isPrimaryKey = value;
    }

    public boolean isUnique() {
        return _isUnique;
    }

    public void setIsUnique(boolean value) {
        _isUnique = value;
    }

    public boolean isIdentity() {
        return _isIdentity;
    }

    public void setIsIdentity(boolean value) {
        _isIdentity = value;
    }

    public boolean isForeignKey() {
        return _isForeignKey;
    }

    public void setIsForeignKey(boolean value) {
        _isForeignKey = value;
    }

    public Object getDefaultSetting() {
        return defaultSetting;
    }

    public void setDefaultSetting(Object value) {
        defaultSetting = value;
    }

    /**
     * 请优先使用Table对象上的SchemaName属性
     *
     * @return
     */
    @Deprecated
    public String getSchemaName() {
        if (table != null) {
            return table.getSchemaName();
        }
        return "";
    }

    /**
     * 请优先使用Table对象上的SchemaName属性
     *
     * @param value
     */
    @Deprecated
    public void setSchemaName(String value) {
        if (table != null) {
            table.setSchemaName(value);
        }
    }

    /**
     * 获取：注释
     *
     * @return
     */
    public String getComment() {
        if (comment != null) {
            return comment;
        } else {
            return getName();
        }
    }

    /**
     * 设置：注释
     *
     * @param value
     */
    public void setComment(String value) {
        comment = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String value) {
        propertyName = value;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == Column.class) {
            Column compareTo = (Column) obj;

            if(!getName().equals(compareTo.getName()) ||(getComment()!=null && !getComment().equals(compareTo.getComment()))) {
                return false;
            }

            if ((isPrimaryKey() != compareTo.isPrimaryKey())
                    || (isAutoIncrement() != compareTo.isAutoIncrement())
                    || (isIdentity() != compareTo.isIdentity())
                    || (isNullable() != compareTo.isNullable())
                    || (isUnique() != compareTo.isUnique())) {
                return false;
            }

            if (getDbType().isString()) {
                return compareTo.getDbType().getValue() == getDbType().getValue() && getLength() == compareTo.getLength();
            }

            if (getDbType().isNumeric()) {
                return compareTo.getDbType() == getDbType() && getPrecision() == compareTo.getPrecision() && getScale() == compareTo.getScale();
            }
            return compareTo.getDbType() == getDbType();
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
