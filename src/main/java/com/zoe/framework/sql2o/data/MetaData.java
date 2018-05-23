package com.zoe.framework.sql2o.data;

import com.zoe.framework.sql2o.tools.AbstractCache;

/**
 * Created by caizhicong on 2016/2/23.
 */
public class MetaData {

    private static final AbstractCache<String, Table, Void> pCache = new AbstractCache<String, Table, Void>() {
        @Override
        protected Table evaluate(String tableName, Void param) {
            return readMetaData(tableName);
        }
    };

    public static Table read(String tableName) {
        return pCache.get(tableName, null);
    }

    private static Table readMetaData(String tableName) {
        
        return null;
    }
}
