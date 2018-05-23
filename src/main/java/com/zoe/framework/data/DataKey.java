package com.zoe.framework.data;

import java.util.ArrayList;
import java.util.List;


public class DataKey {
    /**
     * 主键或外键名称
     */
    private String keyName;
    List<DataColumn> keyColumns = new ArrayList<DataColumn>();

    public DataKey() {
        keyColumns = new ArrayList<DataColumn>();
    }

    public DataKey(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public List<DataColumn> getKeyColumns() {
        return keyColumns;
    }

    public void setKeyColumns(List<DataColumn> keyColumns) {
        this.keyColumns = keyColumns;
    }

    public String getKeyString(DataRow row) {
        if (row == null) { 
            throw new DataException("Datakey-getKeyString datarow can't be null"); }
        String key = "";
        for (DataColumn col : keyColumns) {
            key += row.getValue(col.getColumnIndex()) + ",";
        }
        if (key.endsWith(",")) key = key.substring(0, key.length() - 1);
        return key;
    }
}
