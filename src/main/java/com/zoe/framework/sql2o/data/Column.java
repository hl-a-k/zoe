package com.zoe.framework.sql2o.data;

import com.zoe.framework.sql2o.tools.SqlTypeUtils;

import java.io.Serializable;
import java.sql.Types;

/**
 * Represents a result set column
 */
public class Column implements Serializable {

    private String name;
    private Integer index;
    private String type;
    private Boolean isPrimaryKey = false;
    private Integer sqlType;
    private Class<?> javaType;

    public Column(String name, Integer index, String type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Integer getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public Class<?> getJavaType() {
        if (sqlType == null) {

        }
        if (sqlType != null && javaType == null) {
            javaType = SqlTypeUtils.getJavaType(sqlType);
            return javaType;
        }
        return String.class;
    }

    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(Boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    @Override
    public String toString() {
        return getName() + " (" + getType() + ")";
    }

    public void setSqlType(Integer sqlType) {
        this.sqlType = sqlType;
    }

    public Integer getSqlType() {
        return sqlType;
    }
}
