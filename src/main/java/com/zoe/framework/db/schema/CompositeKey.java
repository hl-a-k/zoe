package com.zoe.framework.db.schema;

/**
 * Defines a composite key entity.
 * Created by caizhicong on 2016/3/21.
 */
public class CompositeKey extends AbstractPrimaryKey {
    @Override
    public PrimaryKeyType getKeyType() {
        return PrimaryKeyType.CompositeKey;
    }
}