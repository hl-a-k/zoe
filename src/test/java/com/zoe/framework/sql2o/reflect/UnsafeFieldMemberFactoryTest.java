package com.zoe.framework.sql2o.reflect;

import com.zoe.framework.sql2o.reflection.UnsafeFieldMemberFactory;

/**
 * @author mdelapenya
 */
public class UnsafeFieldMemberFactoryTest extends AbstractFieldMemberFactoryTest {
    public UnsafeFieldMemberFactoryTest() {
        super(new UnsafeFieldMemberFactory());
    }
}