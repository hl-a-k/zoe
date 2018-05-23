package com.zoe.framework.json;

import com.alibaba.fastjson.*;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zoe.framework.util.FastjsonFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class JsonHelper {

    protected static Logger logger = LoggerFactory.getLogger(JsonHelper.class);

    /**
     * 将对象转换成JSON字符串，并响应回前台
     *
     * @param object
     * @throws IOException
     */
    public static String serialize(Object object) {
        return serialize(object, null, null);
    }

    /**
     * 将对象转换成JSON字符串，并响应回前台
     *
     * @param object
     * @param includesProperties 需要转换的属性
     */
    public static String serializeWithIncludes(Object object,
                                               String[] includesProperties) {
        return serialize(object, includesProperties, null);
    }

    /**
     * 将对象转换成JSON字符串，并响应回前台
     *
     * @param object
     * @param excludesProperties 不需要转换的属性
     */
    public static String serializeWithExcludes(Object object,
                                               String[] excludesProperties) {
        return serialize(object, null, excludesProperties);
    }

    /**
     * 将对象转换成JSON字符串，并响应回前台
     *
     * @param object
     * @param includesProperties 需要转换的属性
     * @param excludesProperties 不需要转换的属性
     */
    public static String serialize(Object object, String[] includesProperties,
                                   String[] excludesProperties, SerializerFeature... features) {
        // 使用SerializerFeature.WriteDateUseDateFormat特性来序列化日期格式的类型为yyyy-MM-dd
        // hh24:mi:ss
        // 使用SerializerFeature.DisableCircularReferenceDetect特性关闭引用检测和生成
        List<SerializerFeature> featureList = new ArrayList<SerializerFeature>();
        featureList.add(SerializerFeature.WriteDateUseDateFormat);
        featureList.add(SerializerFeature.DisableCircularReferenceDetect);
        for (int i = 0; i < features.length; i++) {
            featureList.add(features[i]);
        }
        return JsonHelper.serializeInternal(object, includesProperties,
                excludesProperties, featureList.toArray(new SerializerFeature[0]));
    }

    public static String serializeIE6(Object object) {
        return JsonHelper.serializeIE6(object, null, null);
    }

    public static String serializeIE6(Object object,
                                      String[] includesProperties, String[] excludesProperties, SerializerFeature... features) {
        // 使用SerializerFeature.BrowserCompatible特性会把所有的中文都会序列化为\\uXXXX这种格式，字节数会多一些，但是能兼容IE6
        List<SerializerFeature> featureList = new ArrayList<SerializerFeature>();
        featureList.add(SerializerFeature.WriteDateUseDateFormat);
        featureList.add(SerializerFeature.DisableCircularReferenceDetect);
        featureList.add(SerializerFeature.BrowserCompatible);
        for (int i = 0; i < features.length; i++) {
            featureList.add(features[i]);
        }
        return JsonHelper.serializeInternal(object, includesProperties,
                excludesProperties, featureList.toArray(new SerializerFeature[0]));
    }

    private static String serializeInternal(Object object, String[] includesProperties,
                                            String[] excludesProperties, SerializerFeature... features) {

        FastjsonFilter filter = new FastjsonFilter();// excludes优先于includes
        if ((excludesProperties != null) && (excludesProperties.length > 0)) {
            filter.getExcludes().addAll(
                    Arrays.<String>asList(excludesProperties));
        }
        if ((includesProperties != null) && (includesProperties.length > 0)) {
            filter.getIncludes().addAll(
                    Arrays.<String>asList(includesProperties));
        }
        /*logger.info("对象转JSON：要排除的属性[" + excludesProperties + "]要包含的属性["
                + includesProperties + "]");*/
        String json = JSON.toJSONString(object, filter, features);
        /*logger.info("转换后的JSON字符串：" + json);*/
        return json;
    }

    /**
     * JSON 反序列化为对象
     *
     * @param jsonString
     * @param classT
     * @return
     */
    public static <T> T deserialize(String jsonString, Class<T> classT) {
        T t;
        try {
            t = JSON.parseObject(jsonString, classT);
        } catch (JSONException e) {
            throw new RuntimeException("反序列化失败[" + classT.getName() + "].");
        }
        return t;
    }

    /**
     * JSON 反序列化为对象
     *
     * @param jsonString
     * @param classT
     * @return
     */
    public static <T> void deserialize(String jsonString, Class<T> classT,
                                       T data) {
        T t = null;
        try {
            DefaultJSONParser parser = new DefaultJSONParser(jsonString,
                    ParserConfig.getGlobalInstance());
            JSONReader reader = new JSONReader(parser);
            reader.readObject(data);
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
    }

    /**
     * JSON 反序列化为数组
     *
     * @param jsonString
     * @param classT
     * @return
     */
    public static <T> List<T> deserializeArray(String jsonString,
                                               Class<T> classT) {
        List<T> t = new ArrayList<>();
        if (!StringUtils.isBlank(jsonString)) {
            try {
                if (classT == String.class) {
                    //noinspection unchecked
                    t = (List<T>) JSON.parseObject(jsonString,
                            new TypeReference<List<String>>() {
                            });
                } else if (classT == Map.class || classT == HashMap.class) {
                    //noinspection unchecked
                    t = (List<T>) JSON.parseObject(jsonString,
                            new TypeReference<List<Map<String, Object>>>() {
                            });
                } else {
                    t = JSON.parseArray(jsonString, classT);
                }
            } catch (Exception e) {
                // TODO: handle exception
                t = new ArrayList<>();
            }
        }
        return t;
    }

    /**
     * JSON 反序列化为对象
     *
     * @param jsonObject
     * @param classT
     * @return
     */
    public static <T> T deserialize(JSONObject jsonObject, Class<T> classT) {
        T t;
        try {
            t = JSONObject.toJavaObject(jsonObject, classT);
        } catch (JSONException e) {
            throw new RuntimeException("反序列化失败[" + classT.getName() + "].");
        }
        return t;
    }
}
