package com.zoe.framework.db.schema;

/**
 * 枚举表类型
 * <p>
 * Created by caizhicong on 2016/3/21.
 */
public enum TableType {
    /**
     * 表
     */
    Table,
    /**
     * 视图
     */
    View;

    public static TableType forValue(String value) {
        if ("table".equalsIgnoreCase(value)) {
            return Table;
        } else if ("view".equalsIgnoreCase(value)) {
            return View;
        }
        return Table;
    }
}
