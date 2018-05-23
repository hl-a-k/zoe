package com.zoe.framework.db.schema;

import com.zoe.framework.db.schema.Column;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a primary key entity.
 * Created by caizhicong on 2016/3/21.
 */
public class PrimaryKey implements Serializable {

    private PrimaryKeyType type;
    private List<Column> columns;

    public PrimaryKey() {
        setColumns(new ArrayList<>());
    }

    public PrimaryKeyType getType() {
        return type;
    }

    public void setType(PrimaryKeyType value) {
        type = value;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> value) {
        columns = value;
    }

    public String getName() {
        List<String> names = new ArrayList<>();
        for (Column column : getColumns()) {
            names.add(column.getName());
        }
        return StringUtils.join(",", names);
    }
}