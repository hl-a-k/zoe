package com.zoe.framework.db.schema;

import com.zoe.framework.db.schema.Column;

import java.io.Serializable;
import java.util.List;

/**
 * Created by caizhicong on 2016/3/21.
 */
public interface IPrimaryKey extends Serializable {

    PrimaryKeyType getKeyType();

    List<Column> getColumns();

    void setColumns(List<Column> value);
}
