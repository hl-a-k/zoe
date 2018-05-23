package com.zoe.framework.db.schema;

/**
 * Defines what primary keys are supported.
 *
 * Created by caizhicong on 2016/3/21.
 */
public enum PrimaryKeyType {
    /**
     * Primary key consisting of one column.
     */
    PrimaryKey,
    /**
     * Primary key consisting of two or more columns.
     */
    CompositeKey
}
