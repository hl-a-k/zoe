package com.zoe.framework.data.jpa.repository;

import org.springframework.data.annotation.QueryAnnotation;

import java.lang.annotation.*;

/**
 * Created by caizhicong on 2017/7/10.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@QueryAnnotation
@Documented
public @interface Sql2oQuery {
    String value() default "";

    String countQuery() default "";

    String countProjection() default "";

    boolean nativeQuery() default false;

    String name() default "";

    String countName() default "";

    boolean updateQuery() default false;

    boolean procedure() default false;

    /**
     * 是否过滤VALID_FLAG,如果有的话
     * @return
     */
    boolean checkValid() default true;
}
