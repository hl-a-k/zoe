package com.zoe.framework.shiro.redis;

import com.zoe.framework.io.serializer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SerializeUtils {

    private static Logger logger = LoggerFactory.getLogger(SerializeUtils.class);
    private static Serializer serializer;

    static {
        Properties props = RedisConfig.getConfig();
        String ser = props.getProperty("cache.serializer");
        if (ser == null || "".equals(ser.trim()))
            serializer = new KryoSerializer();
        else {
            if (ser.equals("java")) {
                serializer = new JavaSerializer();
            } else if (ser.equals("fst")) {
                serializer = new FSTSerializer();
            } else if (ser.equals("kryo")) {
                serializer = new KryoSerializer();
            } else if (ser.equals("kryo_pool_ser")) {
                serializer = new KryoPoolSerializer();
            } else {
                try {
                    serializer = (Serializer) Class.forName(ser).newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Cannot initialize Serializer named [" + ser + ']', e);
                }
            }
        }
        logger.info("Using Serializer -> [" + serializer.name() + ":" + serializer.getClass().getName() + ']');
    }

    /**
     * 反序列化
     *
     * @param bytes
     * @return
     */
    public static Object deserialize(byte[] bytes) {
        try {
            return serializer.deserialize(bytes);
        } catch (Exception e) {
            logger.error("Failed to deserialize", e);
        }
        return null;
    }

    /**
     * 序列化
     *
     * @param object
     * @return
     */
    public static byte[] serialize(Object object) {
        try {
            return serializer.serialize(object);
        } catch (Exception e) {
            logger.error("Failed to serialize", e);
        }
        return null;
    }
}
