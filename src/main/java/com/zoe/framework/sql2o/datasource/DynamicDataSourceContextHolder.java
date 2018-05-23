package com.zoe.framework.sql2o.datasource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * DynamicDataSourceContextHolder
 * Created by caizhicong on 2017/7/12.
 */
public final class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    private static List<String> dataSources = new ArrayList<>();

    public static void set(String dataSourceType) {
        contextHolder.set(dataSourceType);
    }

    public static String get() {
        return contextHolder.get();
    }

    public static void remove() {
        contextHolder.remove();
    }

    public static boolean exists(String dataSourceId){
        return dataSources.contains(dataSourceId);
    }

    public static boolean add(String dataSource) {
        return dataSources.add(dataSource);
    }

    public static boolean add(Collection<String> dataSources) {
        return DynamicDataSourceContextHolder.dataSources.addAll(dataSources);
    }
}
