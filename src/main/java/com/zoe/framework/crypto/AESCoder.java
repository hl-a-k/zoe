package com.zoe.framework.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;


/**
 * AES安全编码组件
 * <p/>
 * <pre>
 * 支持 DES、DESede(TripleDES,就是3DES)、AES、Blowfish、RC2、RC4(ARCFOUR)
 * DES                  key size must be equal to 56
 * DESede(TripleDES)     key size must be equal to 112 or 168
 * AES                  key size must be equal to 128, 192 or 256,but 192 and 256 bits may not be available
 * Blowfish          key size must be multiple of 8, and can only range from 32 to 448 (inclusive)
 * RC2                  key size must be between 40 and 1024 bits
 * RC4(ARCFOUR)      key size must be between 40 and 1024 bits
 * 具体内容 需要关注 JDK Document http://.../docs/technotes/guides/security/SunProviders.html
 * </pre>
 *
 * @author 梁栋
 * @version 1.0
 * @since 1.0
 */
public abstract class AESCoder extends Coder {
    /**
     * ALGORITHM 算法 <br>
     * 可替换为以下任意一种算法，同时key值的size相应改变。
     * <p/>
     * <pre>
     * DES                  key size must be equal to 56
     * DESede(TripleDES)     key size must be equal to 112 or 168
     * AES                  key size must be equal to 128, 192 or 256,but 192 and 256 bits may not be available
     * Blowfish          key size must be multiple of 8, and can only range from 32 to 448 (inclusive)
     * RC2                  key size must be between 40 and 1024 bits
     * RC4(ARCFOUR)      key size must be between 40 and 1024 bits
     * </pre>
     * <p/>
     * 在Key toKey(byte[] key)方法中使用下述代码
     * <code>SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);</code> 替换
     * <code>
     * DESKeySpec dks = new DESKeySpec(key);
     * SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
     * SecretKey secretKey = keyFactory.generateSecret(dks);
     * </code>
     *
     * 说明：
     * 默认 Java 中仅支持 128 位密钥，当使用 256 位密钥的时候，会报告密钥长度错误
     * 即报异常：java.security.InvalidKeyException:illegal Key Size的解决方案
     * <ol>
     * 	<li>在官方网站下载JCE无限制权限策略文件（JDK7的下载地址：
     *      http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html</li>
     * 	<li>下载后解压，可以看到local_policy.jar和US_export_policy.jar以及readme.txt</li>
     * 	<li>如果安装了JRE，将两个jar文件放到%JRE_HOME%\lib\security目录下覆盖原来的文件</li>
     * 	<li>如果安装了JDK，将两个jar文件放到%JDK_HOME%\jre\lib\security目录下覆盖原来文件</li>
     * </ol>
     * 在PKCS# Padding中说:
     *
     * 因为恢复的明文的最后一个字节 告诉你 存在多少个填充字节, 用PKCS#5 填充 的加密方法, 即使在输入的明文长度 恰好是 块大小(Block Size)整数倍 , 也会增加一个完整的填充块. 否则,恢复出来的明文的最后一个字节可能是实际的消息字节.
     * 因为第1个因素限制了 使用PKCS#填充的 对称加密算法的 输入块大小(Block Size, 注意不是输入的明文的总长度 total input length), 最大只能是256个字节.   因为大多数对称块加密算法 通常使用8字节或者16字节的块, 所以,这不是一个问题
     * 使用ECB模式填充可能会有安全问题.
     * 使用PKCS#5填充 可以很方便地检测明文中的错误.
     *
     * AES算法默认使用ECB模式，不够安全。
     */
    public static final String ALGORITHM = "AES";
    // AES/CBC/PKCS7Padding
    // AES/CBC/PKCS5Padding
    // 使用CBC模式更安全一些，CBC模式需要16个字节的IV
    public static final String ALGORITHM_CBC = "AES/CBC/NoPadding";
    public static final String ALGORITHM_CBC_PKCS5Padding = "AES/CBC/PKCS5Padding";
    public static final String ALGORITHM_ECB_PKCS5Padding = "AES/ECB/PKCS5Padding";

    /**
     * 转换密钥<br>
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static Key toKey(byte[] key) throws Exception {
        //DESKeySpec dks = new DESKeySpec(key);
        //SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        //SecretKey secretKey = keyFactory.generateSecret(dks);

        // 当使用其他对称加密算法时，如AES、Blowfish等算法时，用下述代码替换上述三行代码
        SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);

        return secretKey;
    }

    /**
     * 解密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] data, String key) throws Exception {
        byte[] aesKey = decryptBASE64(key);
        return decrypt(data, aesKey);
    }

    public static byte[] decrypt(byte[] data, byte[] aesKey) throws Exception {
        return decrypt(data, aesKey, ALGORITHM_CBC);
    }

    public static byte[] decrypt(byte[] data, byte[] aesKey, String ALGORITHM) throws Exception {
        Key k = toKey(aesKey);
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        if (ALGORITHM.contains("CBC")) {
            IvParameterSpec iv = new IvParameterSpec(aesKey, 0, 16);
            cipher.init(Cipher.DECRYPT_MODE, k, iv);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, k);
        }

        return cipher.doFinal(data);
    }

    /**
     * 加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] data, String key) throws Exception {
        byte[] aesKey = decryptBASE64(key);
        return encrypt(data, aesKey);
    }

    public static byte[] encrypt(byte[] data, byte[] aesKey) throws Exception {
        return encrypt(data, aesKey, ALGORITHM_CBC);
    }

    public static byte[] encrypt(byte[] data, byte[] aesKey, String ALGORITHM) throws Exception {
        Key k = toKey(aesKey);
        Cipher cipher = Cipher.getInstance(ALGORITHM);

        if (ALGORITHM.contains("CBC")) {
            IvParameterSpec iv = new IvParameterSpec(aesKey, 0, 16);
            cipher.init(Cipher.ENCRYPT_MODE, k, iv);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, k);
        }

        return cipher.doFinal(data);
    }

    /**
     * 生成密钥，默认秘钥长度为128位(16字节)
     * 默认 Java 中仅支持 128 位密钥，当使用 256 位密钥的时候，会报告密钥长度错误
     *
     * @return
     * @throws Exception
     */
    public static String initKey() throws Exception {
        return initKey(null, 128);
    }

    /**
     * 生成密钥
     * 默认 Java 中仅支持 128 位密钥，当使用 256 位密钥的时候，会报告密钥长度错误
     *
     * @param keyLength 秘钥长度，可选128,192,256
     * @return
     * @throws Exception
     */
    public static String initKey(int keyLength) throws Exception {
        return initKey(null, keyLength);
    }

    /**
     * 生成密钥
     *
     * @param seed
     * @return
     * @throws Exception
     */
    public static String initKey(String seed, int keyLength) throws Exception {
        SecureRandom secureRandom = null;

        if (seed != null) {
            secureRandom = new SecureRandom(decryptBASE64(seed));
        } else {
            secureRandom = new SecureRandom();
        }

        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);
        kg.init(keyLength, secureRandom);

        SecretKey secretKey = kg.generateKey();
        return encryptBASE64(secretKey.getEncoded());
    }

    public static byte[] initKey(String password) throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);
        kg.init(128, new SecureRandom(password.getBytes()));
        SecretKey secretKey = kg.generateKey();
        return secretKey.getEncoded();
    }
}