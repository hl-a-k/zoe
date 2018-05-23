package com.zoe.framework.io.serializer;

import java.io.IOException;

/**
 * 对象序列化接口
 *
 * @author winterlau
 */
public interface Serializer {

    String name();

    byte[] serialize(Object obj) throws IOException;

    Object deserialize(byte[] bytes) throws IOException;
}
