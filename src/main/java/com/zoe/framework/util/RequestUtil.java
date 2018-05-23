package com.zoe.framework.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;
import com.zoe.framework.data.jpa.repository.support.QueryInfo;
import com.zoe.framework.data.jpa.repository.support.Sql2oCrudService;
import com.zoe.framework.json.JsonHelper;
import com.zoe.framework.sql2o.query.QueryOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * RequestUtil
 * Created by caizhicong on 2017/7/13.
 */
public final class RequestUtil {

    private final Logger logger = LoggerFactory.getLogger(RequestUtil.class);

    private HttpServletRequest request;
    private HttpServletResponse response;
    private List<String> fromAttribute = Arrays.asList("query");

    private RequestUtil(HttpServletRequest request) {
        this.request = request;
    }

    private RequestUtil(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    public static RequestUtil of(HttpServletRequest request) {
        return new RequestUtil(request);
    }

    public static RequestUtil of(HttpServletRequest request, HttpServletResponse response) {
        return new RequestUtil(request, response);
    }

    public RequestUtil fromAttributes(List<String> attributes) {
        fromAttribute.addAll(attributes);
        return this;
    }

    public String get(String name) {
        return org.apache.commons.lang3.StringUtils.trim(request.getParameter(name));
    }

    /**
     * 尝试从parameter或attribute里获取参数值
     *
     * @param name
     * @return
     */
    public String getParamOrAttr(String name) {
        String value = request.getParameter(name);
        if (value == null) value = (String) request.getAttribute(name);
        return org.apache.commons.lang3.StringUtils.trim(value);
    }

    /**
     * 尝试从attribute里获取参数值
     *
     * @param name
     * @return
     */
    public String getAttr(String name) {
        String value = (String) request.getAttribute(name);
        return org.apache.commons.lang3.StringUtils.trim(value);
    }

    /**
     * 获取请求参数（默认进行SQL注入过滤）
     *
     * @param name 参数名称（区分大小写！）
     * @return
     */
    public String getParameter(String name) {
        return getParameter(name, true);
    }

    /**
     * 获取请求参数
     *
     * @param name      参数名称（区分大小写！）
     * @param sqlFilter 是否进行SQL注入过滤
     * @return
     */
    public String getParameter(String name, boolean sqlFilter) {
        String value = org.apache.commons.lang3.StringUtils.trim(request.getParameter(name));
        if (sqlFilter) {
            return SqlFilter.filter(value);
        } else {
            return value;
        }
    }

    public Integer getAsInt(String name, Integer defaultValue) {
        String value = get(name);
        if (StringUtils.isEmpty(value))
            return defaultValue;

        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            logger.warn("将对象[{}]转化为数值时发生异常！", value, e);

            return defaultValue;
        }
    }

    public int getAsInt(String name) {
        return getAsInt(name, 0);
    }

    private Map<String, String> map;

    public Map<String, String> getMap() {
        if (map != null) return map;
        map = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String[] value = entry.getValue();
            map.put(entry.getKey(), value == null ? null : value[0].trim());
        }
        return map;
    }

    private Map<String, Object> map2;

    public Map<String, Object> getMap2() {
        if (map2 != null) return map2;
        map2 = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String[] value = entry.getValue();
            map2.put(entry.getKey(), value == null ? null : value[0].trim());
        }
        return map2;
    }

    public JSONObject getJSONObject() {
        return new JSONObject(getMap2());
    }

    public <T> T setToModel(Class<T> model) {
        return JsonHelper.deserialize(getJSONObject(), model);
    }

    public <T> T setToModel(Class<T> model, boolean applySpatialNull) {
        JSONObject map = getJSONObject();
        T data = JsonHelper.deserialize(map, model);
        if (applySpatialNull) {
            Sql2oCrudService.applySpatialNull(model, map, data);
        }
        return data;
    }

    /**
     * 对需要置空的值设置特殊值以表示Null
     *
     * @param model
     * @param data
     * @param <T>
     */
    public <T> void applySpatialNull(Class<T> model, T data) {
        Sql2oCrudService.applySpatialNull(model, getMap2(), data);
    }

    public String[] getAsArray(String name) {
        return getArray(name, String[].class);
    }

    //region for repository

    /**
     * 获取请求参数（默认进行SQL注入过滤）
     *
     * @param name 参数名称（区分大小写！）
     * @return
     */
    public <T> T getParameter(String name, Class<T> domain) {
        String value = getParameter(name, true);
        return CastUtils.cast(value, domain);
    }

    /**
     * 获取请求参数（默认进行SQL注入过滤）
     *
     * @param name 参数名称（区分大小写！）
     * @return
     */
    public <T> T getParameter(String name, Class<T> domain, T defaultValue) {
        String value = getParameter(name, true);
        return CastUtils.cast(value, domain, defaultValue);
    }

    /**
     * 获取查询参数的列表，逗号分隔(如ID列表）
     *
     * @param parameterName 参数名
     * @param clazz         实体类型
     * @return 指定类型的对象数组
     */
    public <T> T[] getArray(String parameterName, Class<T[]> clazz) {
        String[] array = request.getParameterValues(parameterName);
        if (array == null) array = new String[]{""};
        if (array.length == 1 && array[0] != null) {
            array = org.apache.commons.lang3.StringUtils.split(array[0].trim(), ",");
        }
        return TypeUtils.castToJavaBean(Arrays.asList(array), clazz);
    }

    /**
     * 从提交参数（data=JSON）获取提交的实体
     *
     * @return 指定类型的实体对象
     */
    public <T> T getModel(Class<T> domain) {
        return getModel("data", domain);
    }

    /**
     * 从提交参数（data=JSON）中反序列化出指定实体类
     *
     * @param parameterName 参数名
     * @param domain        实体类型
     * @return 指定类型的实体对象
     */
    public <T> T getModel(String parameterName, Class<T> domain) {
        return getModel(parameterName, domain, null);
    }

    /**
     * 从提交参数（data=JSON）中反序列化出指定实体类
     *
     * @param parameterName 参数名
     * @param domain        实体类型
     * @return 指定类型的实体对象
     */
    public <T> T getModel(String parameterName, Class<T> domain, T data) {
        String jsonString = request.getParameter(parameterName);
        if (data != null) {
            JsonHelper.deserialize(jsonString, domain, data);
        } else {
            try {
                data = JsonHelper.deserialize(jsonString, domain);
                /*JSONObject jsonObject = JSON.parseObject(jsonString);
                data = jsonObject.toJavaObject(domain);
                applySpatialNull(domain, jsonObject, data);*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    /**
     * 从提交参数（data=JSON）获取提交的实体
     *
     * @return
     */
    public JSONObject getModelJSON() {
        return getModelJSON("data");
    }

    /**
     * 从提交参数（data=JSON）获取提交的实体
     *
     * @return
     */
    public JSONObject getModelJSON(String parameterName) {
        String jsonString = request.getParameter(parameterName);
        return JSONObject.parseObject(jsonString);
    }

    public QueryInfo getQuery() {
        return getQueryInfo(QueryInfo.KeyStrategy.NoOp);
    }

    public QueryInfo getQuery(QueryInfo.KeyStrategy keyStrategy) {
        return getQueryInfo(keyStrategy);
    }

    /**
     * 获取查询信息
     *
     * @return
     */
    private QueryInfo getQueryInfo(QueryInfo.KeyStrategy keyStrategy) {
        QueryInfo queryInfo = new QueryInfo();
        Map<String, Object> queryItems = getQueryItems();
        queryInfo.setQueryItems(queryItems);
        int page = queryInfo.getItemValue(Integer.class, "page", 1);
        int rows = queryInfo.getItemValue(Integer.class, "rows", 10);
        queryInfo.setPageInfo(page, rows);
        int maxRows = QueryInfo.getAllowedMaxRows();
        if (rows > maxRows) {
            logger.warn("分页条数[{}]过大，当前限制查询最大条数为[{}]!!!请注意!!!如果确实需要查询超过[{}]条记录，请按如下方法手动设置：\n QueryInfo.setAllowedMaxRows(rows) ", rows, maxRows, maxRows);
        }

        String[] properties = queryInfo.getArray("sort", String[].class);
        if (properties != null && properties.length > 0) {
            String orderStr = queryInfo.getString("order");
            List<Sort.Order> orderList = new ArrayList<>();
            for (String property : properties) {
                String name = property;
                String[] splits = property.split(" ");
                if (splits.length > 1) {
                    name = splits[0];
                    orderStr = splits[1].toLowerCase();
                }
                Sort.Direction direction = "desc".equalsIgnoreCase(orderStr) ? Sort.Direction.DESC : Sort.Direction.ASC;
                orderList.add(new Sort.Order(direction, name));
            }
            Sort sort = new Sort(orderList);
            queryInfo.setSort(sort);
        }

        if (queryInfo.hasItem("pageOp")) {
            queryInfo.setPageOp(queryInfo.getItemValue(Integer.class, "pageOp", 3));
        }

        String queryOp = queryInfo.getString("queryOp");
        if (!org.apache.commons.lang3.StringUtils.isBlank(queryOp)) {
            try {
                //noinspection unchecked
                Map<String, Object> items = JsonHelper.deserialize(queryOp, Map.class);
                if (items != null) {
                    for (Map.Entry<String, Object> entry : items.entrySet()) {
                        queryInfo.setQueryOp(entry.getKey(), QueryOp.valueOf((String) entry.getValue()));
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return queryInfo;
    }

    public Map<String, Object> getQueryItems() {
        Map<String, Object> queryItems = new HashMap<>();
        String query = getParamOrAttr("query");
        if (!org.apache.commons.lang3.StringUtils.isBlank(query)) {
            try {
                //noinspection unchecked
                Map<String, Object> items = JsonHelper.deserialize(query, Map.class);
                if (items != null) queryItems.putAll(items);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            Enumeration pNames = request.getParameterNames();
            while (pNames.hasMoreElements()) {
                String name = (String) pNames.nextElement();
                String value = get(name);
                if (!queryItems.containsKey(name)) {
                    queryItems.put(name, value);
                }
            }
            if (fromAttribute.size() > 0) {
                for (String attr : fromAttribute) {
                    String value = getAttr(attr);
                    if (value != null && !queryItems.containsKey(attr)) {
                        queryItems.put(attr, value);
                    }
                }
            }
        } else {
            Enumeration pNames = request.getParameterNames();
            while (pNames.hasMoreElements()) {
                String name = (String) pNames.nextElement();
                String value = get(name);
                queryItems.put(name, value);
            }
            if (fromAttribute.size() > 0) {
                for (String attr : fromAttribute) {
                    String value = getAttr(attr);
                    if (value != null) {
                        queryItems.put(attr, value);
                    }
                }
            }
        }
        return queryItems;
    }

    //endregion

    //region json response

    /**
     * 判断浏览器是否为IE6
     *
     * @return
     */
    public boolean isIE6() {
        String User_Agent = request.getHeader("User-Agent");
        return org.apache.commons.lang3.StringUtils.indexOfIgnoreCase(User_Agent, "MSIE 6") > -1;
    }

    /**
     * 将对象转换成JSON字符串，并响应回前台
     *
     * @param object
     * @throws IOException
     */
    public void writeJson(Object object) {
        writeJson(object, null, null, null, new SerializerFeature[0]);
    }

    /**
     * 将对象转换成JSON字符串，并响应回前台
     *
     * @param object
     * @param features Fastjson的SerializerFeature序列化属性
     *                 QuoteFieldNames———-输出key时是否使用双引号,默认为true
     *                 WriteMapNullValue——–是否输出值为null的字段,默认为false
     *                 WriteNullNumberAsZero—-数值字段如果为null,输出为0,而非null
     *                 WriteNullListAsEmpty—–List字段如果为null,输出为[],而非null
     *                 WriteNullStringAsEmpty—字符类型字段如果为null,输出为”“,而非null
     *                 WriteNullBooleanAsFalse–Boolean字段如果为null,输出为false,而非null
     * @throws IOException
     */
    public void writeJson(Object object, SerializerFeature... features) {
        writeJson(object, null, null, null, features);
    }

    /**
     * 将对象转换成JSON字符串，并响应回前台
     *
     * @param object
     * @throws IOException
     */
    public void writeJsonP(Object object) {
        writeJson(object, null, null, "jsonPCallback");
    }

    /**
     * 将对象转换成JSON字符串，并响应回前台
     *
     * @param object
     * @param jsonPCallback jsonp 回调参数名
     * @throws IOException
     */
    public void writeJsonP(Object object, String jsonPCallback) {
        writeJson(object, null, null, jsonPCallback);
    }

    /**
     * 输出表格分页JSON
     *
     * @param paged
     */
    public void writePaged(Page paged) {
        Grid grid = new Grid();
        if (paged != null) {
            grid.setTotal(paged.getTotalElements());
            grid.setRows(paged.getContent());
        }
        writeJson(grid, null, null, null, new SerializerFeature[0]);
    }

    public class Grid implements java.io.Serializable {
        private static final long serialVersionUID = 1;
        private Long total = 0L;
        private List rows = new ArrayList();

        public Long getTotal() {
            return total;
        }

        public void setTotal(Long integer) {
            this.total = integer;
        }

        public List getRows() {
            return rows;
        }

        public void setRows(List rows) {
            this.rows = rows;
        }
    }

    /**
     * 将对象转换成JSON字符串，并响应回前台
     *
     * @param object
     * @param includesProperties 需要转换的属性
     */
    public void writeJsonWithIncludes(Object object, String[] includesProperties) {
        writeJson(object, includesProperties, null, null);
    }

    /**
     * 将对象转换成JSON字符串，并响应回前台
     *
     * @param object
     * @param excludesProperties 不需要转换的属性
     */
    public void writeJsonWithExcludes(Object object, String[] excludesProperties) {
        writeJson(object, null, excludesProperties, null);
    }

    /**
     * 将对象转换成JSON字符串，并响应回前台
     *
     * @param object
     * @param includesProperties 需要转换的属性
     * @param excludesProperties 不需要转换的属性
     */
    public void writeJson(Object object, String[] includesProperties,
                          String[] excludesProperties, String jsonPCallback,
                          SerializerFeature... features) {
        String json;
        if (object != null && object instanceof String) {
            json = object.toString();
        } else {
            if (this.isIE6()) {
                json = JsonHelper.serializeIE6(object, includesProperties,
                        excludesProperties, features);
            } else {
                json = JsonHelper.serialize(object, includesProperties,
                        excludesProperties, features);
            }
        }

        try {
            String contentType = "application/json; charset=UTF-8";// text/html;charset=utf-8
            if (!org.apache.commons.lang3.StringUtils.isBlank(jsonPCallback)) {
                String callback = get(jsonPCallback);
                if (!org.apache.commons.lang3.StringUtils.isBlank(callback)) {
                    json = callback + "&&" + callback + "(" + json + ")";
                    contentType = "text/javascript; charset=UTF-8";
                }
            }
            response.setContentType(contentType);
            response.getWriter().write(json);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //endregion


    /**
     * 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址;
     */
    public String getIpAddress() {
        // 获取请求主机IP地址,如果通过代理进来，则透过防火墙获取真实IP地址
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Forwarded-For");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
                    // 根据网卡取本机配置的IP
                    try {
                        ip = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (ip.length() > 7) { //"1.1.1.1".length() = 7
            //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            String[] ips = ip.split(",");
            for (int index = 0; index < ips.length; index++) {
                String strIp = ips[index];
                if (!("unknown".equalsIgnoreCase(strIp))) {
                    ip = strIp;
                    break;
                }
            }
        }
        return ip;
    }

    public static String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }
}
