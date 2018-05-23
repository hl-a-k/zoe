package com.zoe.framework.util;


import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.UUID;

/**
 * 类型辅助工具类，类型间转换工具。
 */
public class TypeUtils {

    private TypeUtils() {
    }

    //byte 与 int 的相互转换
    public static byte intToByte(int data) {
        return (byte) data;
    }

    public static int byteToInt(byte data) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return data & 0xFF;
    }

    public static byte[] intToBytes(int data) {
        return new byte[]{
                (byte) ((data >> 24) & 0xFF),
                (byte) ((data >> 16) & 0xFF),
                (byte) ((data >> 8) & 0xFF),
                (byte) (data & 0xFF)
        };
    }

    public static int bytesToInt(byte[] bytes) {
        return bytes[3] & 0xFF |
                (bytes[2] & 0xFF) << 8 |
                (bytes[1] & 0xFF) << 16 |
                (bytes[0] & 0xFF) << 24;
    }

    private static ByteBuffer longBuffer = ByteBuffer.allocate(8);

    public static byte[] longToBytes(long x) {
        longBuffer.putLong(0, x);
        return longBuffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        longBuffer.put(bytes, 0, bytes.length);
        longBuffer.flip();//need flip
        return longBuffer.getLong();
    }

    public static byte[] toBytes(char[] data, String charset) throws Exception {
        CharBuffer cb = CharBuffer.allocate(data.length);
        cb.put(data);
        cb.flip();
        return Charset.forName(charset).encode(cb).array();
    }

    public static byte[] toBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        String value = uuid.toString();
        value = value.substring(0, 8) + value.substring(9, 13) + value.substring(14, 18) + value.substring(19, 23) + value.substring(24);

        for(int i = 0; i < 16; ++i) {
            int bit = Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
            bytes[i] = (byte)(bit & 255);
        }

        return bytes;
    }

    public static byte[] toBytes(Object data) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(data);
        byte[] bytes = baos.toByteArray();
        oos.flush();
        oos.close();
        baos.flush();
        baos.close();
        return bytes;
    }

    public static String toHex(byte[] bytes) {
        int len = bytes.length;
        StringBuilder hex = new StringBuilder(len);
        String str;

        for(int i = 0; i < len; ++i) {
            Byte b = bytes[i];
            boolean negative = b < 0;
            int abs = Math.abs(b);
            if(negative) {
                abs |= 128;
            }

            str = Integer.toHexString(abs & 255);
            if(str.length() == 1) {
                hex.append("0");
            }
            hex.append(str);
        }
        return hex.toString();
    }

    public static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];

        for(int i = 0; i < hex.length(); i += 2) {
            String subStr = hex.substring(i, i + 2);
            boolean negative = false;
            int inte = Integer.parseInt(subStr, 16);
            if(inte > 127) {
                negative = true;
            }

            if(inte == 128) {
                inte = -128;
            } else if(negative) {
                inte = 0 - (inte & 127);
            }

            byte b = (byte)inte;
            bytes[i / 2] = b;
        }

        return bytes;
    }

    public static String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] fromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}
