package com.zoe.framework.sql2o.datasource;

import java.lang.annotation.*;

/**
 * 指定数据源的注解
 * Created by caizhicong on 2017/7/12.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {
    /**
     * 数据源名称，可以是一个动态集
     * @return
     */
    String value();

    /**
     * sql2o bean name
     * @return
     */
    String sql2oBeanName() default "";
}
