package com.zoe.framework.data.jpa.repository.support;

import com.alibaba.fastjson.util.TypeUtils;
import com.zoe.framework.sql2o.query.QueryMap;
import com.zoe.framework.sql2o.query.QueryOp;
import com.zoe.framework.sql2o.tools.CamelCaseUtils;
import com.zoe.framework.util.CastUtils;
import com.zoe.framework.util.SqlFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 存储前端发送过来的查询信息
 *
 * @author caizhicong
 */
public class QueryInfo implements Pageable, Serializable {

    private static final long serialVersionUID = 1L;

    private static final ThreadLocal<Integer> maxRowsHolder = new ThreadLocal<>();

    private QueryMap queryMap = new QueryMap();

    private int page;
    private int size;
    private Sort sort;
    private String sql;
    private boolean caseSensitive = true;
    private boolean autoDeriveColumnNames = true;
    private int pageOp = 3;//1=page,2=count,3=1+2=page+count
    private int allowedMaxRows = 10000; //允许的最大查询条数

    public QueryInfo() {
        this(1, 10);
    }

    public QueryInfo(int page, int size) {
        this(page, size, null);
    }

    public QueryInfo(int page, int size, Sort sort) {
        this.setPageInfo(page, size);
        this.sort = sort;
    }

    public QueryInfo(int page, int size, Sort.Direction direction, String... properties) {
        this(page, size, new Sort(direction, properties));
    }

    public QueryInfo setPageInfo(int page, int size){
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero!");
        }

        if (size < 1) {
            throw new IllegalArgumentException("Page size must not be less than one!");
        }

        this.page = page;
        this.size = size;

        return this;
    }


    public static QueryInfo New() {
        return new QueryInfo();
    }

    public static QueryInfo New(int page, int size) {
        return new QueryInfo(page, size);
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public QueryInfo setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    public boolean isAutoDeriveColumnNames() {
        return autoDeriveColumnNames;
    }

    public QueryInfo setAutoDeriveColumnNames(boolean autoDeriveColumnNames) {
        this.autoDeriveColumnNames = autoDeriveColumnNames;
        return this;
    }

    public int getPageNumber() {
        return page;
    }

    public void setPageNumber(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return size;
    }

    public void setPageSize(int size) {
        this.size = size;
    }

    public Sort getSort() {
        return getSort(false);
    }

    public Sort getSort(boolean useNullOrder) {
        if(sort == null && useNullOrder){
            sort = new Sort("NULL");
        }
        return sort;
    }

    public Sort getSort(List<Sort.Order> orders) {
        if(sort == null){
            sort = new Sort(orders);
        }
        return sort;
    }

    public Sort getSort(String... properties) {
        if(sort == null){
            sort = new Sort(properties);
        }
        return sort;
    }

    public Sort getSort(Sort.Direction direction, String... properties) {
        if(sort == null){
            sort = new Sort(direction, properties);
        }
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    /**
     * 设置可用的排序范围
     * @param availableOrders 需要限制的字段排序范围
     */
    public void setAvailableOrders(String... availableOrders) {
        if (this.sort != null && availableOrders != null && availableOrders.length > 0) {
            Iterator<Sort.Order> iterator = sort.iterator();
            List<Sort.Order> orderList = new ArrayList<>();
            for (String availableOrder : availableOrders) {
                Sort.Order order = sort.getOrderFor(availableOrder);
                if (order != null) {
                    orderList.add(order);
                }
            }
            if (orderList.size() > 0) {
                sort = new Sort(orderList);
            } else {
                sort = null;
            }
        }
    }

    public int firstResult() { return (page - 1) * size; }

    public int maxResults() { return page * size; }

    public int getOffset() {
        return firstResult();
    }

    public boolean hasPrevious() {
        return page > 0;
    }

    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    public Pageable next() {
        return new QueryInfo(getPageNumber() + 1, getPageSize(), getSort());
    }

    public QueryInfo previous() {
        return getPageNumber() == 0 ? this : new QueryInfo(getPageNumber() - 1, getPageSize(), getSort());
    }

    public QueryInfo first() {
        return new QueryInfo(0, getPageSize(), getSort());
    }

    public int getPageOp(){
        return pageOp;
    }

    public QueryInfo setPageOp(int pageOp){
        this.pageOp = pageOp;
        return this;
    }

    public static int getAllowedMaxRows() {
        Integer maxRows = maxRowsHolder.get();
        if(maxRows == null) return 10000;
        return maxRows;
    }

    public static void setAllowedMaxRows(int allowedMaxRows) {
        maxRowsHolder.set(allowedMaxRows);
    }

    public static void clearAllowedMaxRows() {
        maxRowsHolder.remove();
    }

    public String getSql() {
        return sql;
    }

    public QueryInfo setSql(String sql) {
        this.sql = sql;
        return this;
    }

    /**
     * 返回ORDER BY 子句，不含ORDER BY 关键字
     *
     * @return
     */
    public String getOrderBy() {
        return getOrderBy("t", true);
    }

    /**
     * 返回ORDER BY 子句，不含ORDER BY 关键字
     *
     * @param tableAliasName
     * @return
     */
    public String getOrderBy(String tableAliasName) {
        return getOrderBy(tableAliasName, true);
    }

    /**
     * 返回ORDER BY 子句，不含ORDER BY 关键字
     *
     * @param tableAliasName
     * @param containsOrderBy 标记是否包含ORDER BY 关键字
     * @return
     */
    public String getOrderBy(String tableAliasName, boolean containsOrderBy) {
        if (getSort() == null || getSort().iterator().hasNext()) {
            return "";
        }
        if (!StringUtils.isEmpty(tableAliasName)) {
            tableAliasName = tableAliasName + ".";
        }
        //将驼峰转为下划线
        StringBuilder builder = new StringBuilder(containsOrderBy ? " ORDER BY " : " ");
        for (Sort.Order order : getSort()) {
            builder.append(String.format("%s%s %s", tableAliasName, order.getProperty(), order.getDirection().toString())).append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        return builder.toString();
    }

    public Map<String, Object> getQueryItems() {
        return queryMap;
    }

    public QueryInfo setQueryItems(Map<String, Object> queryItems) {
        if(queryItems == null || queryItems.size() == 0) return this;
        for (Map.Entry<String, Object> entry : queryItems.entrySet()) {
            this.queryMap.add(processKey(entry.getKey()), processSpatialChar(entry.getValue(), QueryOp.eq), QueryOp.eq);
        }
        return this;
    }

    private KeyStrategy keyStrategy = KeyStrategy.NoOp;
    public QueryInfo setKeyStrategy(KeyStrategy keyStrategy){
        this.keyStrategy = keyStrategy;
        return this;
    }

    private String processKey(String key){
        if(keyStrategy == KeyStrategy.NoOp) return key;
        if(keyStrategy == KeyStrategy.CamelCase) return CamelCaseUtils.underscoreToCamelCase(key);
        if(keyStrategy == KeyStrategy.LowerCaseWithUnderscores) return CamelCaseUtils.camelCaseToUnderscore(key);
        return key;
    }

    /**
     * 特殊字符转义
     * @param value
     * @return
     */
    private Object processSpatialChar(Object value, QueryOp op) {
        if (value instanceof String && op.aboutLike()) {
            String val = value.toString();
            if (val.length() > 0) {
                //mysql中需要转义 %、_
                val = val.replace("%", "\\%").replace("_", "\\_");
                return val;
            }
        }
        return value;
    }

    public QueryInfo addQueryItem(String key, Object value) {
        return addQueryItem(key, value, QueryOp.eq);
    }

    public QueryInfo addQueryItem(String key, Object value, boolean ignoreNullValue) {
        this.queryMap.add(processKey(key), processSpatialChar(value, QueryOp.eq), QueryOp.eq, ignoreNullValue);
        return this;
    }

    public QueryInfo addQueryItem(String key, Object value, QueryOp op) {
        this.queryMap.add(processKey(key), processSpatialChar(value, op), op);
        return this;
    }

    public QueryInfo addQueryItem(String key, Object value, QueryOp op, boolean ignoreNullValue) {
        this.queryMap.add(processKey(key), processSpatialChar(value, op), op, ignoreNullValue);
        return this;
    }

    public QueryInfo addQueryItem(String key, Object value, QueryOp op, boolean ignoreNullValue, boolean isOr) {
        this.queryMap.add(processKey(key), processSpatialChar(value, op), op, ignoreNullValue);
        this.queryMap.isOr(key, isOr);
        return this;
    }

    public QueryInfo addQueryItems(Map<String,Object> items) {
        if(items == null || items.size() == 0) return this;
        for (Map.Entry<String, Object> entry : items.entrySet()) {
            String key = entry.getKey();
            this.queryMap.add(processKey(key), processSpatialChar(entry.getValue(), QueryOp.eq), QueryOp.eq);
        }
        return this;
    }

    /**
     * 获取是否包含查询项
     *
     * @return
     */
    public boolean hasQuery() {
        return this.queryMap.size() > 0;
    }

    /**
     * 获取是否包含排序条件
     *
     * @return
     */
    public boolean hasOrderBy() {
        return this.getSort().iterator().hasNext();
    }

    /**
     * 获取指定的查询项
     *
     * @param field 字段名
     * @return
     */
    public MutablePair<Object,QueryOp> getQueryItem(String field) {
        if (this.hasQuery()) {
            Object value = this.queryMap.get(field);
            QueryOp op = this.queryMap.getOp(field);
            if (value != null) return MutablePair.of(value, op);
        }
        return null;
    }

    public Object removeQueryItem(String field) {
        return queryMap.remove(field);
    }

    public QueryInfo setQueryOp(String field,QueryOp op) {
        queryMap.setOp(field, op);
        return this;
    }

    public QueryInfo setQueryOp(String field,QueryOp op, boolean isOr) {
        queryMap.setOp(field, op, isOr);
        return this;
    }

    /**
     * 检索是否存在查询项
     *
     * @param field 字段名
     * @return
     */
    public boolean hasItem(String field) {
        return this.getItem(field) != null;
    }

    /**
     * 获取指定的查询项
     *
     * @param field 字段名
     * @return
     */
    public Object getItem(String field) {
        return queryMap.get(field);
    }

    /**
     * 获取查询项的值。找不到对应的查询项时，会返回该类型的默认值。（对于数值型，是否应该返回-1？）
     *
     * @param clazz 类型
     * @param field 字段名
     * @return
     */
    public <T> T getItemValue(Class<T> clazz, String field) {
        return getItemValue(clazz, field, null);
    }

    /**
     * 获取查询项的值。找不到对应的查询项时，会返回该类型的默认值。（对于数值型，是否应该返回-1？）
     *
     * @param clazz 类型
     * @param field 字段名
     * @return
     */
    public <T> T getItemValue(Class<T> clazz, String field, T defaultValue) {
        Object item = getItem(field);
        if (item != null) {
            return CastUtils.cast(SqlFilter.filter(item.toString()), clazz);
        }
        if (defaultValue != null) {
            return defaultValue;
        }
        return null;
    }

    public String getString(String field) {
        return this.getItemValue(String.class, field);
    }

    public Integer getInteger(String field) {
        return this.getItemValue(Integer.class, field);
    }

    /**
     * 获取日期字符串，yyyy-MM-dd
     *
     * @param field
     * @return
     */
    public String getDateString(String field) {
        Date date = this.getItemValue(Date.class, field);
        if (date != null) {
            return date.toString();
        }
        return null;
    }

    /**
     * 获取以逗号分隔的2个日期的第1个（开始日期）
     *
     * @param field
     * @return
     */
    public String getDateStart(String field) {
        String date = this.getString(field);
        if (date == null) {
            return null;
        }
        String[] dates = StringUtils.split(date, ",");
        if (dates.length > 0) {
            return dates[0];
        }
        return null;
    }

    /**
     * 获取以逗号分隔的2个日期的第2个（结束日期）
     *
     * @param field
     * @return
     */
    public String getDateEnd(String field) {
        String date = this.getString(field);
        if (date == null) {
            return null;
        }
        String[] dates = StringUtils.split(date, ",");
        if (dates.length > 1) {
            return dates[1];
        }
        return null;
    }

    /**
     * 获取一对日期（开始日期，结束日期），不存在时为null
     *
     * @param field
     * @return
     */
    public MutablePair<String, String> getDate(String field) {
        String date = this.getString(field);
        if (date == null) {
            return MutablePair.of(null,null);
        }
        String[] dates = StringUtils.split(date, ",");
        if (dates.length > 1) {
            return MutablePair.of(dates[0], dates[1]);
        }
        if (dates.length > 0) {
            int index = StringUtils.indexOf(date, ",");
            if (index > 1) {
                return MutablePair.of(dates[0], null);
            }
            return MutablePair.of(null, dates[0]);
        }
        return MutablePair.of(null, null);
    }

    /**
     * 获取查询参数的列表，逗号分隔(如ID列表）
     *
     * @param field 参数名
     * @param clazz         实体类型
     * @return 指定类型的对象数组
     */
    public <T> T[] getArray(String field, Class<T[]> clazz) {
        String[] array = new String[0];
        String value = getString(field);
        if (value != null) {
            array = StringUtils.split(value.trim(), ",");
        }
        return TypeUtils.castToJavaBean(array, clazz);
    }

    private Boolean pagination;

    public Boolean getPagination() {
        if (pagination == null) {
            pagination = getPageNumber() > 0 && getPageSize() > 0;
        }
        return pagination;
    }

    public void setPagination(Boolean pagination) {
        this.pagination = pagination;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;

        result = prime * result + page;
        result = prime * result + size;
        result = prime * result + (null == sort ? 0 : sort.hashCode());

        return result;
    }

    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        if (!(obj instanceof QueryInfo)) {
            return false;
        }

        QueryInfo that = (QueryInfo) obj;

        boolean sortEqual = this.sort == null ? that.sort == null : this.sort.equals(that.sort);

        return  this.page == that.page && this.size == that.size && sortEqual;
    }

    public String toString() {
        return String.format("Page request [number: %d, size %d, sort: %s]", getPageNumber(), getPageSize(),
                sort == null ? null : sort.toString());
    }

    public enum KeyStrategy {
        /**
         * 不做处理
         */
        NoOp(0),

        /**
         * eg: userId
         */
        CamelCase(1),

        /**
         * eg: user_id
         */
        LowerCaseWithUnderscores(2);

        private int strategy;

        KeyStrategy(int strategy){
            this.strategy = strategy;
        }

        public int getStrategy() {
            return this.strategy;
        }
    }
}
