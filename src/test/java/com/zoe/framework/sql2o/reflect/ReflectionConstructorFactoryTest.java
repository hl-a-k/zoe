package com.zoe.framework.sql2o.reflect;

import com.zoe.framework.sql2o.reflection.ObjectConstructorFactory;
import com.zoe.framework.sql2o.reflection.ReflectionObjectConstructorFactory;

/**
 * User: dimzon
 * Date: 4/9/14
 * Time: 10:15 PM
 */
public class ReflectionConstructorFactoryTest extends AbstractObjectConstructorFactoryTest {
    public ReflectionConstructorFactoryTest() {
        super(new ReflectionObjectConstructorFactory());
    }
}
