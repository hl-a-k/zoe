package com.zoe.framework.db.schema.query;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by caizhicong on 2016/5/16.
 */
public final class QueryParams extends HashMap<String, Object> {

    public static QueryParams create() {
        return new QueryParams();
    }

    public QueryParams add(String key, Object value) {
        return this.add(key, value, true);
    }

    public QueryParams add(String key, Object value, boolean ignoreNullValue) {
        if (ignoreNullValue && (value == null || value.toString().trim().isEmpty())) {
            return this;
        }
        put(key, value);
        return this;
    }
}
