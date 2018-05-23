package com.zoe.framework.dataset;

import java.io.Serializable;

/**
 *
 * Created by Administrator on 2016/10/2.
 */
public class CDataColumn implements Serializable {
    private int number;
    private String name;

    private Class<?> type;
    private boolean allowNull;
    private CDataCacheContainer table;

    public CDataColumn(CDataCacheContainer table, int columnNumber, String columnName, Class<?> type, boolean allowNull)
    {
        this.number = columnNumber;
        this.name = columnName;
        this.type = type;
        this.allowNull = allowNull;
        this.table = table;
    }

    /**
     * The zero-based column number.
     */
    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public boolean getAllowNull()
    {
        return this.allowNull;
    }

    public CDataCacheContainer getTable()
    {
        return this.table;
    }
}
