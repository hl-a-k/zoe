package com.zoe.framework.data.jpa.repository.support;

import org.springframework.util.ClassUtils;

/**
 * Created by caizhicong on 2017/7/5.
 */
public final class Sql2oUtils {

    public static final boolean SQL2O_PRESENT = ClassUtils.isPresent("com.zoe.framework.sql2o.SqlBag", Sql2oUtils.class.getClassLoader());

}
