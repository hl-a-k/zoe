package com.zoe.framework.db.schema;

/**
 * 接口：数据库对象
 * <p/>
 * Created by caizhicong on 2016/3/21.
 */
public interface IDBObject {
    /**
     * 获取：数据库对象名称
     *
     * @return
     */
    String getName();

    /**
     * 设置：数据库对象名称
     *
     * @param value
     */
    void setName(String value);

    /**
     * 获取：模式名称
     *
     * @return
     */
    String getSchemaName();

    /**
     * 设置：模式名称
     *
     * @param value
     */
    void setSchemaName(String value);

    /**
     * 获取：注释
     *
     * @return
     */
    String getComment();

    /**
     * 设置：注释
     *
     * @param value
     */
    void setComment(String value);
}
