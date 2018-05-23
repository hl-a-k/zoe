package com.zoe.framework.sql2o;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurablePropertyResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by caizhicong on 2016/7/13.
 */
public class Sql2oPropertyConfigurer extends PropertySourcesPlaceholderConfigurer {

    private static Properties properties = new Properties();

    @Override
    protected Properties mergeProperties() throws IOException {
        properties = super.mergeProperties();
        return properties;
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static Map<String,String> getByPrefix(String prefix){
        if(!prefix.endsWith(".")) prefix = prefix + ".";
        Map<String,String> stringMap = new HashMap<>();
        for (Map.Entry<Object,Object> entry : properties.entrySet()){
            String key = entry.getKey().toString();
            if(key.startsWith(prefix)){
                Object oval = properties.get(key);
                String sval = (oval instanceof String) ? (String)oval : null;
                stringMap.put(key,sval);
            }
        }
        return stringMap;
    }
}
