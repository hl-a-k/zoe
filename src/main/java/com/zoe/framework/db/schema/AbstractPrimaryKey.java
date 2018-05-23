package com.zoe.framework.db.schema;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caizhicong on 2016/3/21.
 */
public abstract class AbstractPrimaryKey implements IPrimaryKey {

    private List<Column> Columns;

    protected AbstractPrimaryKey() {
        setColumns(new ArrayList<>());
    }

    public abstract PrimaryKeyType getKeyType();

    public List<Column> getColumns() {
        return Columns;
    }

    public void setColumns(List<Column> value) {
        Columns = value;
    }

    public String getName() {
        List<String> names = new ArrayList<>();
        for (Column column : getColumns()) {
            names.add(column.getName());
        }
        return StringUtils.join(",", names);
    }
}