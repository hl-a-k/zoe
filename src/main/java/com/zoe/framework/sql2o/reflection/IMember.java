package com.zoe.framework.sql2o.reflection;

/**
 * 整合 Getter 和 Setter
 * The IMember interface is used by sql2o to get or set property values when doing automatic column to property mapping
 * Created by caizhicong on 2016/10/3.
 *
 * @author mdelapenya
 * @author caizhicong
 */
public interface IMember {
    Object getProperty(Object obj);

    void setProperty(Object obj, Object value);

    Class getType();
}
