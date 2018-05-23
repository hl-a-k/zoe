package com.zoe.framework.sql4o;

import junit.framework.TestCase;

public class AbstractSqlCreatorTest extends TestCase {

    public void testAllocateParameter() {
        SelectBuilder sc = new SelectBuilder();
        assertEquals("param0", sc.allocateParameter());
        assertEquals("param1", sc.allocateParameter());
        assertEquals("param2", sc.allocateParameter());
    }
}
