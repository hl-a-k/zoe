package com.zoe.framework.sql2o.query;

import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * QueryMap
 * Created by caizhicong on 2017/7/14.
 */
public class QueryMap extends LinkedCaseInsensitiveMap<Object> {

    private LinkedCaseInsensitiveMap<QueryOp> opMap = new LinkedCaseInsensitiveMap<>();
    private LinkedCaseInsensitiveMap<Boolean> orMap = new LinkedCaseInsensitiveMap<>();

    public static QueryMap create() {
        return new QueryMap();
    }

    public QueryMap add(String key, Object value) {
        return this.add(key, value, true);
    }

    public QueryMap add(String key, Object value, QueryOp op) {
        return add(key, value, op, true);
    }

    public QueryMap add(String key, Object value, boolean ignoreNullValue) {
        return add(key, value, QueryOp.eq, ignoreNullValue);
    }

    public QueryMap add(String key, Object value, QueryOp op, boolean ignoreNullValue) {
        if (!op.aboutNull() && ignoreNullValue && StringUtils.isEmpty(value)) {
            return this;
        }
        put(key, value);
        opMap.put(key, op);
        return this;
    }

    @Override
    public Object remove(Object key) {
        opMap.remove(key);
        return super.remove(key);
    }

    public QueryOp getOp(String key) {
        return opMap.get(key);
    }

    public QueryOp setOp(String key, QueryOp op) {
        if(op.aboutNull() && !containsKey(key)){
            put(key, null);
        }
        return opMap.put(key, op);
    }

    public Boolean isOr(String key) {
        Boolean isOr = orMap.get(key);
        if (isOr != null) return isOr;
        return false;
    }

    public Boolean isOr(String key, boolean isOr) {
        if (isOr) orMap.put(key, true);
        return false;
    }

    public QueryOp setOp(String key, QueryOp op, boolean isOr) {
        if (isOr) orMap.put(key, true);
        return setOp(key, op);
    }

    public Map<String, String> toStringMap() {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : this.entrySet()) {
            Object value = entry.getValue();
            map.put(entry.getKey(), value == null ? "" : value.toString());
        }
        return map;
    }
}
