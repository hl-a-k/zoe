package com.zoe.framework.sql2o.reflect;

import com.zoe.framework.sql2o.reflection.FieldMemberFactory;
import com.zoe.framework.sql2o.reflection.IMember;
import junit.framework.TestCase;

import java.lang.reflect.Field;

/**
 * User: dimzon
 * Date: 4/9/14
 * Time: 9:46 PM
 */
public abstract class AbstractFieldMemberFactoryTest extends TestCase {
    public static class POJO1{
        boolean _boolean;
        byte _byte;
        short _short;
        int _int;
        long _long;
        float _float;
        double _double;
        char _char;
        Object _obj;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            POJO1 pojo1 = (POJO1) o;

            if (_boolean != pojo1._boolean) return false;
            if (_byte != pojo1._byte) return false;
            if (_char != pojo1._char) return false;
            if (Double.compare(pojo1._double, _double) != 0) return false;
            if (Float.compare(pojo1._float, _float) != 0) return false;
            if (_int != pojo1._int) return false;
            if (_long != pojo1._long) return false;
            if (_short != pojo1._short) return false;
            if (_obj != null ? !_obj.equals(pojo1._obj) : pojo1._obj != null) return false;

            return true;
        }
    }


    public void testAllTypes1() throws IllegalAccessException {
        POJO1 pojo1 = new POJO1();
        pojo1._boolean = true;
        pojo1._byte = 17;
        pojo1._short=87;
        pojo1._int= Integer.MIN_VALUE;
        pojo1._long= 1337;
        pojo1._char='a';
        pojo1._double=Math.PI;
        pojo1._float= (float) Math.log(93);
        pojo1._obj = pojo1;

        POJO1 pojo2 = new POJO1();

        assertFalse(pojo1.equals(pojo2));

        Field[] fields = pojo1.getClass().getDeclaredFields();
        for (Field field : fields) {
            IMember setter = fmf.newMember(field);
            assertSame(field.getType(),setter.getType());
            Object val1 = field.get(pojo1);
            Object val2 = field.get(pojo2);
            assertFalse(val1.equals(val2));
            setter.setProperty(pojo2,val1);
            Object val3 = field.get(pojo2);
            assertEquals(val1,val3);
        }

        assertEquals(pojo1,pojo2);

        // let's reset all values to NULL
        // primitive fields will not be affected
        for (Field field : fields) {
            IMember setter = fmf.newMember(field);
            Object val1 = field.get(pojo1);
            assertNotNull(val1);

            setter.setProperty(pojo1,null);

            Object val2 = field.get(pojo1);
            if(!setter.getType().isPrimitive()){
                assertNull(val2);
                continue;
            }
            assertNotNull(val2);
            // not affected
            assertEquals(val1,val2);
        }
        pojo2._obj = null;
        assertEquals(pojo2,pojo1);
    }
 
    public void testAllTypes() throws IllegalAccessException {
        POJO1 pojo = new POJO1();
        pojo._boolean = true;
        pojo._byte = 17;
        pojo._short=87;
        pojo._int= Integer.MIN_VALUE;
        pojo._long= 1337;
        pojo._char='a';
        pojo._double=Math.PI;
        pojo._float= (float) Math.log(93);
        pojo._obj = pojo;

        Field[] fields = pojo.getClass().getDeclaredFields();

        for (Field field : fields) {
            IMember getter = fmf.newMember(field);
            assertSame(field.getType(),getter.getType());

            Object val1 = field.get(pojo);
            assertEquals(val1, getter.getProperty(pojo));
        }
    }

    public final FieldMemberFactory fmf;

    protected AbstractFieldMemberFactoryTest(FieldMemberFactory fmf) {
        this.fmf = fmf;
    }
}
