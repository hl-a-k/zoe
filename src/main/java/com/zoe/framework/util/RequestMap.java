package com.zoe.framework.util;

import java.util.HashMap;

/**
 * Created by caizhicong on 2017/8/24.
 */
public class RequestMap extends HashMap<String, Object> {

    public static RequestMap create() {
        return new RequestMap();
    }

    public RequestMap add(String key, Object value) {
        return this.add(key, value, true);
    }

    public RequestMap add(String key, Object value, boolean ignoreNullValue) {
        if (ignoreNullValue && (value == null || value.toString().trim().isEmpty())) {
            return this;
        }
        this.put(key, value);
        return this;
    }
}
