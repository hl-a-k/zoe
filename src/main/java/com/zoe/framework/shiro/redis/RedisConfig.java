package com.zoe.framework.shiro.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by caizhicong on 2016/7/22.
 */
public class RedisConfig {

    private final static Logger log = LoggerFactory.getLogger(RedisConfig.class);

    private final static String CONFIG_FILE = "/shiro.redis.properties";
    private final static Properties config;

    static {
        try {
            config = loadConfig();
        } catch (IOException e) {
            throw new RuntimeException("Unabled to load shiro redis configuration " + CONFIG_FILE, e);
        }
    }

    public static Properties getConfig() {
        return config;
    }

    /**
     * 加载配置
     *
     * @return Properties
     * @throws IOException
     */
    private static Properties loadConfig() throws IOException {
        log.info("Load Shiro Redis Config File : [{}].", CONFIG_FILE);
        InputStream configStream = RedisConfig.class.getClassLoader().getParent().getResourceAsStream(CONFIG_FILE);
        if (configStream == null)
            configStream = RedisConfig.class.getResourceAsStream(CONFIG_FILE);
        if (configStream == null)
            throw new RuntimeException("Cannot find " + CONFIG_FILE + " !!!");

        Properties props = new Properties();

        try {
            props.load(configStream);
        } finally {
            configStream.close();
        }

        return props;
    }
}
