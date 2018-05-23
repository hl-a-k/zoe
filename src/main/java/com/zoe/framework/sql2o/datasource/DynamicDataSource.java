package com.zoe.framework.sql2o.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * dynamic DataSource
 * Created by caizhicong on 2017/7/12.
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.get();
    }

    @Override
    public int hashCode() {
        int code = super.hashCode();
        Object lookupKey = determineCurrentLookupKey();
        if(lookupKey != null) {
            code += lookupKey.hashCode();
        }
        return code;
    }
}
