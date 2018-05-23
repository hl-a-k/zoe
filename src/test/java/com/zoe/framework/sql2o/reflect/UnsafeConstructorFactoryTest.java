package com.zoe.framework.sql2o.reflect;

import com.zoe.framework.sql2o.reflection.UnsafeFieldMemberFactory;

/**
 * User: dimzon
 * Date: 4/9/14
 * Time: 10:16 PM
 */
public class UnsafeConstructorFactoryTest extends AbstractObjectConstructorFactoryTest {
    public UnsafeConstructorFactoryTest() {
        super(new UnsafeFieldMemberFactory());
    }
}
