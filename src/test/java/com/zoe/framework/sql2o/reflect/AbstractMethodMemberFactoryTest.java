package com.zoe.framework.sql2o.reflect;

import junit.framework.TestCase;

import com.zoe.framework.sql2o.reflection.IMember;
import com.zoe.framework.sql2o.reflection.MethodMemberFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author mdelapenya
 */
public abstract class AbstractMethodMemberFactoryTest extends TestCase {
    public static class POJO1{
        public boolean get_boolean() {
            return this._boolean;
        }

        public byte set_byte() {
            return this._byte;
        }

        public short set_short() {
            return this._short;
        }

        public int set_int() {
            return this._int;
        }

        public long set_long() {
            return this._long;
        }

        public float set_float() {
            return this._float;
        }

        public double set_double() {
            return this._double;
        }

        public char set_char() {
            return this._char;
        }

        public Object set_obj() {
            return this._obj;
        }

        public void set_boolean(boolean _boolean) {
            this._boolean = _boolean;
        }

        public void set_byte(byte _byte) {
            this._byte = _byte;
        }

        public void set_short(short _short) {
            this._short = _short;
        }

        public void set_int(int _int) {
            this._int = _int;
        }

        public void set_long(long _long) {
            this._long = _long;
        }

        public void set_float(float _float) {
            this._float = _float;
        }

        public void set_double(double _double) {
            this._double = _double;
        }

        public void set_char(char _char) {
            this._char = _char;
        }

        public void set_obj(Object _obj) {
            this._obj = _obj;
        }

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


    public void testAllTypes() throws IllegalAccessException, NoSuchFieldException {
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

        Method[] methods = pojo.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if(!method.getName().startsWith("get_")) continue;

            Field field = pojo.getClass()
                    .getDeclaredField(method.getName().substring(3));

            IMember getter = mmf.newMember(method,method);
            assertSame(field.getType(),getter.getType());

            Object val1 = field.get(pojo);
            assertEquals(val1, getter.getProperty(pojo));
        }
    }

//    public void testAllTypes1() throws IllegalAccessException, NoSuchFieldException {
//        POJO1 pojo1 = new POJO1();
//        pojo1._boolean = true;
//        pojo1._byte = 17;
//        pojo1._short=87;
//        pojo1._int= Integer.MIN_VALUE;
//        pojo1._long= 1337;
//        pojo1._char='a';
//        pojo1._double=Math.PI;
//        pojo1._float= (float) Math.log(93);
//        pojo1._obj = pojo1;
//
//        POJO1 pojo2 = new POJO1();
//
//        assertFalse(pojo1.equals(pojo2));
//
//        Method[] methods = pojo1.getClass().getDeclaredMethods();
//        for (Method method : methods) {
//            if(!method.getName().startsWith("set_")) continue;
//            String name = method.getName().substring(3);
//            Field field = pojo1.getClass()
//                    .getDeclaredField(name);
//            Method getMethod=null;
//            try {
//                getMethod = pojo1.getClass().getDeclaredMethod("get" + name);
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }
//            IMember setter = mmf.newMember(getMethod,method);
//            assertSame(field.getType(),setter.getType());
//            Object val1 = field.get(pojo1);
//            Object val2 = field.get(pojo2);
//            assertFalse(val1.equals(val2));
//            setter.setProperty(pojo2,val1);
//            Object val3 = field.get(pojo2);
//            assertEquals(val1,val3);
//        }
//
//        assertEquals(pojo1,pojo2);
//
//        // let's reset all values to NULL
//        // primitive fields will not be affected
//        for (Method method : methods) {
//            if(!method.getName().startsWith("set_")) continue;
//            Field field = pojo1.getClass()
//                    .getDeclaredField(method.getName().substring(3));
//            IMember setter = mmf.newMember(method,method);
//            Object val1 = field.get(pojo1);
//            assertNotNull(val1);
//
//            setter.setProperty(pojo1,null);
//
//            Object val2 = field.get(pojo1);
//            if(!setter.getType().isPrimitive()){
//                assertNull(val2);
//                continue;
//            }
//            assertNotNull(val2);
//            // not affected
//            assertEquals(val1,val2);
//        }
//        pojo2._obj = null;
//        assertEquals(pojo2,pojo1);
//    }

    public  final MethodMemberFactory mmf;

    public AbstractMethodMemberFactoryTest(MethodMemberFactory mmf) {
        this.mmf = mmf;
    }
}