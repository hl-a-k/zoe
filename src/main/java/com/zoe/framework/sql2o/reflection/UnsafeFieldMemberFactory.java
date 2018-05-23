package com.zoe.framework.sql2o.reflection;

import com.zoe.framework.sql2o.Sql2oException;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by caizhicong on 2016/10/3.
 *
 * @author mdelapenya
 * @author caizhicong
 */
@SuppressWarnings("Unsafe")
public class UnsafeFieldMemberFactory implements FieldMemberFactory , ObjectConstructorFactory{

    private final static Unsafe theUnsafe;
    static {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field declaredField = unsafeClass.getDeclaredField("theUnsafe");
            declaredField.setAccessible(true);
            theUnsafe = (Unsafe) declaredField.get(null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public IMember newMember(final Field field) {
        final Class type = field.getType();
        final boolean isStatic = Modifier.isStatic(field.getModifiers());

        final long offset =  isStatic
                ? theUnsafe.staticFieldOffset(field)
                : theUnsafe.objectFieldOffset(field);

        if (!Modifier.isVolatile(field.getModifiers())) {
            if (type == Boolean.TYPE) {
                return new IMember() {
                    public Object getProperty(Object obj) {
                        return theUnsafe.getBoolean(obj, offset);
                    }
                    public void setProperty(Object obj, Object value) {
                        if (value == null) return;
                        theUnsafe.putBoolean(obj, offset, (Boolean) value);
                    }
                    public Class getType() {
                        return Boolean.TYPE;
                    }
                };
            }

            if (type == Character.TYPE) {
                return new IMember() {
                    public Object getProperty(Object obj) {
                        return theUnsafe.getChar(obj, offset);
                    }
                    public void setProperty(Object obj, Object value) {
                        if (value == null) return;
                        theUnsafe.putChar(obj, offset, (Character) value);
                    }
                    public Class getType() {
                        return Character.TYPE;
                    }
                };
            }

            if (type == Byte.TYPE) {
                return new IMember() {
                    public Object getProperty(Object obj) {
                        return theUnsafe.getByte(obj, offset);
                    }
                    public void setProperty(Object obj, Object value) {
                        if (value == null) return;
                        theUnsafe.putByte(obj, offset, ((Number) value).byteValue());
                    }
                    public Class getType() {
                        return Byte.TYPE;
                    }
                };
            }

            if (type == Short.TYPE) {
                return new IMember() {
                    public Object getProperty(Object obj) {
                        return theUnsafe.getShort(obj, offset);
                    }
                    public void setProperty(Object obj, Object value) {
                        if (value == null) return;
                        theUnsafe.putShort(obj, offset, ((Number) value).shortValue());
                    }
                    public Class getType() {
                        return Short.TYPE;
                    }
                };
            }

            if (type == Integer.TYPE) {
                return new IMember() {
                    public Object getProperty(Object obj) {
                        return theUnsafe.getInt(obj, offset);
                    }
                    public void setProperty(Object obj, Object value) {
                        if (value == null) return;
                        theUnsafe.putInt(obj, offset, ((Number) value).intValue());
                    }
                    public Class getType() {
                        return Integer.TYPE;
                    }
                };
            }

            if (type == Long.TYPE) {
                return new IMember() {
                    public Object getProperty(Object obj) {
                        return theUnsafe.getLong(obj, offset);
                    }
                    public void setProperty(Object obj, Object value) {
                        if (value == null) return;
                        theUnsafe.putLong(obj, offset, ((Number) value).longValue());
                    }
                    public Class getType() {
                        return Long.TYPE;
                    }
                };
            }

            if (type == Float.TYPE) {
                return new IMember() {
                    public Object getProperty(Object obj) {
                        return theUnsafe.getFloat(obj, offset);
                    }
                    public void setProperty(Object obj, Object value) {
                        if (value == null) return;
                        theUnsafe.putFloat(obj, offset, ((Number) value).floatValue());
                    }
                    public Class getType() {
                        return Float.TYPE;
                    }
                };
            }
            if (type == Double.TYPE) {
                return new IMember() {
                    public Object getProperty(Object obj) {
                        return theUnsafe.getDouble(obj, offset);
                    }
                    public void setProperty(Object obj, Object value) {
                        if (value == null) return;
                        theUnsafe.putDouble(obj, offset, ((Number) value).doubleValue());
                    }
                    public Class getType() {
                        return Double.TYPE;
                    }
                };
            }
            return new IMember() {
                public Object getProperty(Object obj) {
                    return theUnsafe.getObject(obj, offset);
                }
                public void setProperty(Object obj, Object value) {
                    theUnsafe.putObject(obj, offset, value);
                }
                public Class getType() {
                    return type;
                }
            };
        }

        if (type == Boolean.TYPE) {
            return new IMember() {
                public Object getProperty(Object obj) {
                    return theUnsafe.getBooleanVolatile(obj, offset);
                }
                public void setProperty(Object obj, Object value) {
                    if (value == null) return;
                    theUnsafe.putBooleanVolatile(obj, offset, (Boolean) value);
                }
                public Class getType() {
                    return Boolean.TYPE;
                }
            };
        }
        if (type == Character.TYPE) {
            return new IMember() {
                public Object getProperty(Object obj) {
                    return theUnsafe.getCharVolatile(obj, offset);
                }
                public void setProperty(Object obj, Object value) {
                    if (value == null) return;
                    theUnsafe.putCharVolatile(obj, offset, (Character) value);
                }
                public Class getType() {
                    return Character.TYPE;
                }
            };
        }
        if (type == Byte.TYPE) {
            return new IMember() {
                public Object getProperty(Object obj) {
                    return theUnsafe.getByteVolatile(obj, offset);
                }
                public void setProperty(Object obj, Object value) {
                    if (value == null) return;
                    theUnsafe.putByteVolatile(obj, offset, ((Number) value).byteValue());
                }
                public Class getType() {
                    return Byte.TYPE;
                }
            };
        }
        if (type == Short.TYPE) {
            return new IMember() {
                public Object getProperty(Object obj) {
                    return theUnsafe.getShortVolatile(obj, offset);
                }
                public void setProperty(Object obj, Object value) {
                    if (value == null) return;
                    theUnsafe.putShortVolatile(obj, offset, ((Number) value).shortValue());
                }
                public Class getType() {
                    return Short.TYPE;
                }
            };
        }
        if (type == Integer.TYPE) {
            return new IMember() {
                public Object getProperty(Object obj) {
                    return theUnsafe.getIntVolatile(obj, offset);
                }
                public void setProperty(Object obj, Object value) {
                    if (value == null) return;
                    theUnsafe.putIntVolatile(obj, offset, ((Number) value).intValue());
                }
                public Class getType() {
                    return Integer.TYPE;
                }
            };
        }
        if (type == Long.TYPE) {
            return new IMember() {
                public Object getProperty(Object obj) {
                    return theUnsafe.getLongVolatile(obj, offset);
                }
                public void setProperty(Object obj, Object value) {
                    if (value == null) return;
                    theUnsafe.putLongVolatile(obj, offset, ((Number) value).longValue());
                }
                public Class getType() {
                    return Long.TYPE;
                }
            };
        }
        if (type == Float.TYPE) {
            return new IMember() {
                public Object getProperty(Object obj) {
                    return theUnsafe.getFloatVolatile(obj, offset);
                }
                public void setProperty(Object obj, Object value) {
                    if (value == null) return;
                    theUnsafe.putFloatVolatile(obj, offset, ((Number) value).floatValue());
                }
                public Class getType() {
                    return Float.TYPE;
                }
            };
        }
        if (type == Double.TYPE) {
            return new IMember() {
                public Object getProperty(Object obj) {
                    return theUnsafe.getDoubleVolatile(obj, offset);
                }
                public void setProperty(Object obj, Object value) {
                    if (value == null) return;
                    theUnsafe.putDoubleVolatile(obj, offset, ((Number) value).doubleValue());
                }
                public Class getType() {
                    return Double.TYPE;
                }
            };
        }
        return new IMember() {
            public Object getProperty(Object obj) {
                return theUnsafe.getObjectVolatile(obj, offset);
            }
            public void setProperty(Object obj, Object value) {
                theUnsafe.putObjectVolatile(obj, offset, value);
            }
            public Class getType() {
                return type;
            }
        };
    }

    public ObjectConstructor newConstructor(final Class<?> clazz) {
        return getConstructor(clazz);
    }
    public static ObjectConstructor getConstructor(final Class<?> clazz) {
        return new ObjectConstructor() {
            public Object newInstance() {
                try {
                    return theUnsafe.allocateInstance(clazz);
                } catch (InstantiationException e) {
                    throw new Sql2oException("Could not create a new instance of class " + clazz, e);
                }
            }
        };
    }
}
