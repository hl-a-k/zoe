package com.zoe.framework.db.schema;

import com.zoe.framework.db.schema.IDBObject;

/**
 * Created by caizhicong on 2016/3/21.
 */
public interface IStoredProcedure extends IDBObject
{
    Object getOutput();
    void setOutput(Object value);
}