package com.zoe.framework.data.jpa.repository.support;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.FieldInfo;
import com.zoe.framework.data.jpa.domain.ValidableEntity;
import com.zoe.framework.data.multitenancy.TenantContext;
import com.zoe.framework.sql2o.Connection;
import com.zoe.framework.sql2o.Query;
import com.zoe.framework.sql2o.Sql2oException;
import com.zoe.framework.sql2o.data.InsertData;
import com.zoe.framework.sql2o.data.PocoColumn;
import com.zoe.framework.sql2o.data.PojoData;
import com.zoe.framework.sql2o.data.TableInfo;
import com.zoe.framework.sql2o.query.QueryMap;
import com.zoe.framework.sql2o.query.QueryOp;
import com.zoe.framework.sql2o.reflection.PojoIntrospector;
import com.zoe.framework.sql2o.reflection.PojoProperty;
import com.zoe.framework.sql2o.util.CastUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Sql2oCrudService
 * Created by caizhicong on 2017/8/3.
 */
public final class Sql2oCrudService {

    /**
     * 特殊数字，表示NULL，用来将数据库的整数型字段设置为NULL
     */
    public static final Integer NullInteger = Integer.MAX_VALUE - 19890720;
    /**
     * 特殊数字，表示NULL，用来将数据库的整数型字段设置为NULL
     */
    public static final Float NullFloat = Float.NaN;
    /**
     * 特殊数字，表示NULL，用来将数据库的整数型字段设置为NULL
     */
    public static final Double NullDouble = Double.NaN;
    /**
     * 特殊数字，表示NULL，用来将数据库的整数型字段设置为NULL
     */
    public static final BigDecimal NullBigDecimal = BigDecimal.valueOf(0.07202012);
    /**
     * 表示NULL的空字符串
     */
    public static final String NullString = "";
    /**
     * 表示NULL的日期
     */
    public static final Date NullDate = new Date(0);
    /**
     * 表示NULL的Byte数组
     */
    public static final byte[] NullBytes = new byte[0];
    private static final Pattern WHERE = Pattern.compile(".*where\\s+.*", CASE_INSENSITIVE);
    private final Connection connection;
    private String validFlagProp = "validFlag";
    private boolean autoFilterValidFlag = true;

    private Sql2oCrudService(Connection connection) {
        this.connection = connection;
    }

    public static void main(String[] args) {
        System.out.println(NullInteger);//2127592927
        System.out.println(NullBigDecimal);//0.07202012
        System.out.println(Objects.equals(NullBigDecimal, BigDecimal.valueOf(0.07202012)));//true
        System.out.println(NullDate.getTime());//0

        /*System.out.println(NullInteger.equals(NullInteger));//true
        System.out.println(NullDouble.equals(Double.NaN));//true
        System.out.println(NullFloat.equals(Float.NaN));//true
        System.out.println(NullBigDecimal.equals(NullBigDecimal));//true
        System.out.println(NullString.equals(NullString));//true
        System.out.println(NullDate.equals(NullDate));//true
        System.out.println(NullBytes.equals(NullBytes));//true*/

        System.out.println(NullBytes.getClass());//class [B
        System.out.println(byte[].class);//class [B
        System.out.println(byte[].class == NullBytes.getClass());//true

    }

    public static Sql2oCrudService of(Connection connection) {
        return new Sql2oCrudService(connection);
    }

    public static Sql2oCrudService instance() {
        return new Sql2oCrudService(null);
    }

    /**
     * 对需要置空的值设置特殊值以表示Null
     *
     * @param model
     * @param map
     * @param data
     * @param <T>
     */
    public static <T> void applySpatialNull(Class<T> model, Map<String, Object> map, T data) {
        ParserConfig config = ParserConfig.getGlobalInstance();
        JavaBeanDeserializer javaBeanDeserializer = null;
        ObjectDeserializer deserializer = config.getDeserializer(model);
        if (deserializer instanceof JavaBeanDeserializer) {
            javaBeanDeserializer = (JavaBeanDeserializer) deserializer;
        }
        if (javaBeanDeserializer != null) {
            FieldInfo[] fieldInfos = javaBeanDeserializer.beanInfo.sortedFields;
            for (FieldInfo fieldInfo : fieldInfos) {
                String fieldName = fieldInfo.name;
                Object value = map.get(fieldName);
                if (value != null && value.equals("")) {//为空字符串才表示要设置为空
                    FieldDeserializer fieldDeserializer = javaBeanDeserializer.smartMatch(fieldName);
                    if (fieldInfo.fieldClass == Integer.class) {
                        fieldDeserializer.setValue(data, NullInteger);//数字型字段默认值为0
                    } else if (fieldInfo.fieldClass == Double.class) {
                        fieldDeserializer.setValue(data, NullDouble);//浮点型字段默认值为0.0
                    } else if (fieldInfo.fieldClass == Float.class) {
                        fieldDeserializer.setValue(data, NullFloat);//浮点型字段默认值为0.0
                    } else if (fieldInfo.fieldClass == BigDecimal.class) {
                        fieldDeserializer.setValue(data, NullBigDecimal);//浮点型字段默认值为0.0
                    } else if (fieldInfo.fieldClass == String.class) {
                        fieldDeserializer.setValue(data, NullString);//字符串型字段默认值为空字符串
                    } else if (Date.class.isAssignableFrom(fieldInfo.fieldClass)) {
                        fieldDeserializer.setValue(data, NullDate);//日期型字段默认值为0
                    } else if (byte[].class == fieldInfo.fieldClass) {
                        fieldDeserializer.setValue(data, NullBytes);//字节数组字段默认值为0
                    }
                }
            }
        }
    }

    /**
     * 不自动过滤 validFlag=1 的数据
     *
     * @return
     */
    public Sql2oCrudService withoutValidFlag() {
        this.autoFilterValidFlag = false;
        return this;
    }

    public Sql2oCrudService validFlagProp(String propertyName) {
        Assert.hasText(propertyName, "propertyName 不能为空");
        this.validFlagProp = propertyName;
        return this;
    }

    /**
     * 根据参数列表生成sql语句
     *
     * @param pojoClass
     * @param sql
     * @param params
     * @param <T>
     * @return
     */
    public <T> String buildFilterSql(Class<T> pojoClass, String sql, Map<String, Object> params) {
        TableInfo ti = PojoData.forClass(pojoClass).getTableInfo();
        return buildFilterSql(sql, params, ti);
    }

    /**
     * 根据参数列表生成sql语句
     *
     * @param sql
     * @param params
     * @param <T>
     * @return
     */
    public <T> String buildFilterSql(String sql, Map<String, Object> params) {
        return buildFilterSql(sql, params, null);
    }

    //region 构建查询语句

    private String buildFilterSql(String sql, Map<String, Object> params, TableInfo ti) {
        Assert.notNull(params, "params 不能为 null ！");
        if (ti != null && (sql == null || "".equals(sql))) {
            String fields = StringUtils.trimToNull((String) params.get("fields"));
            String allFields = "*";
            if (fields != null) {
                List<String> fieldList = Arrays.asList(fields.split(","));
                List<String> newFieldList = new ArrayList<>();
                for (String field : fieldList) {
                    PocoColumn column = ti.getColumn(field);
                    if (column == null) continue;
                    newFieldList.add(column.ColumnName);
                }
                if (newFieldList.size() > 0) {
                    allFields = StringUtils.join(newFieldList.toArray(), ",");
                }
            }
            sql = "SELECT " + allFields + " FROM " + ti.getTableName();
        }
        StringBuilder builder = new StringBuilder(sql);

        if (!WHERE.matcher(sql).matches()) {
            builder.append(" WHERE 1 = 1 ");
        }
        WherePair wherePair = buildWherePair(params, ti);
        builder.append(wherePair.toSql(true));
        return builder.toString();
    }

    private WherePair buildWherePair(Map<String, Object> params, TableInfo ti) {
        WherePair wherePair = new WherePair();

        if (autoFilterValidFlag && ti != null) {
            if (!params.containsKey(validFlagProp) && !params.containsKey("valid_flag") && ti.getProperties().get(validFlagProp) != null) {
                params.put(validFlagProp, Arrays.asList(0, 1));//默认查询未删除数据
                wherePair.addParam(validFlagProp, Arrays.asList(0, 1));//默认查询未删除数据
            }
        }

        if ("null".equals(params.get(validFlagProp)) || (params.containsKey(validFlagProp) && params.get(validFlagProp) == null)) {
            params.remove(validFlagProp);
        }

        if (ti != null && ti.getProperties().get("tenantId") != null) {
            String tenantId = TenantContext.getTenant();
            if (tenantId != null) {
                params.put("tenantId", tenantId);//过滤当前租户
                wherePair.addParam("tenantId", tenantId);//过滤当前租户
            }
        }

        QueryMap queryMap;
        if (params instanceof QueryMap) {
            queryMap = (QueryMap) params;
        } else {
            queryMap = QueryMap.create();
            queryMap.putAll(params);
        }

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            PocoColumn pocoColumn;
            Class<?> type = String.class;
            String columnName = entry.getKey();
            if(ti != null) {
                pocoColumn = ti.getColumn(entry.getKey());
                if (pocoColumn == null) continue;
                type = pocoColumn.PropertyInfo.type;
                columnName = pocoColumn.ColumnName;
            }
            String paramName = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String && ((String) value).length() == 0) {
                value = null;
                entry.setValue(null);
            }

            QueryOp queryOp = queryMap.getOp(entry.getKey());
            if (queryOp == null) queryOp = QueryOp.eq;
            if (value != null && List.class.isAssignableFrom(value.getClass())) {
                queryOp = QueryOp.in;
                //noinspection ConstantConditions
                List list = (List) value;
                if (list.size() == 0) continue;
            }
            if (value == null && !queryOp.aboutNull())
                continue;
            Boolean isOr = queryMap.isOr(entry.getKey());
            wherePair.addClause(isOr, queryOp.format(columnName, paramName, entry.getValue(), type));
                /*if(queryOp == QueryOp.in && paramName.equals(validFlagProp)){
                    params.remove(validFlagProp);
                    whereParams.remove(validFlagProp);
                    continue;
                }*/
            if (queryOp != QueryOp.in) {
                if (queryOp.aboutLike()) {
                    wherePair.addParam(entry.getKey(), queryOp.formatValue(entry.getValue()));
                } else {
                    wherePair.addParam(entry.getKey(), CastUtils.cast(queryOp.formatValue(entry.getValue()), type));
                }
                entry.setValue(wherePair.getParamValue(entry.getKey()));
            }
        }
        return wherePair;
    }

    /**
     * 根据查询条件列表构建查询语句
     *
     * @param params 查询条件列表
     * @return 查询语句
     */
    public String buildSql(Map<String, Object> params) {

        return null;
    }

    /**
     * 根据主键查询记录
     *
     * @param pojoClass 实体Class
     * @param idValue   单个主键值或整个实体对象或者包含主键信息的Map
     * @return
     */
    public <T> T getById(Class<T> pojoClass, final Object idValue) {
        TableInfo ti = PojoData.forClass(pojoClass).getTableInfo();
        Map<String, Object> params = getIdParams(pojoClass, ti, idValue, true);
        return this.findFirstByProperties(pojoClass, params);
    }

    //endregion

    /**
     * 根据某些字段查找列表
     *
     * @param pojoClass 实体Class
     * @param params    筛选字段
     * @return
     */
    public <T> List<T> findByProperties(Class<T> pojoClass,
                                        Map<String, Object> params) {
        return this.findByProperties(pojoClass, params, false);
    }

    /**
     * 根据属性列表查找匹配的第一条记录
     *
     * @param pojoClass 实体属性名称
     * @param params    筛选的属性（字段）
     * @return
     */
    public <T> T findFirstByProperties(Class<T> pojoClass,
                                       Map<String, Object> params) {
        List<T> list = this.findByProperties(pojoClass, params, true);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 根据属性列表查找匹配的第一条记录
     *
     * @param pojoClass 实体属性名称
     * @param params    筛选的属性（字段）
     * @return 实体列表
     */
    private <T> List<T> findByProperties(Class<T> pojoClass,
                                         Map<String, Object> params, boolean fetchFirst) {
        TableInfo ti = PojoData.forClass(pojoClass).getTableInfo();
        if (params == null) params = new QueryMap();
        String queryText = buildFilterSql(null, params, ti);
        boolean hasParams = params.size() > 0;
        if(!hasParams) return new ArrayList<>();//没有过滤条件不返回数据
        Query query = connection.createQuery(queryText);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value == null || List.class.isAssignableFrom(value.getClass())) {
                continue;
            }
            query.addParameter(entry.getKey(), value);
        }
        if (fetchFirst) {
            ArrayList<T> list = new ArrayList<>(1);
            list.add(query.executeAndFetchFirst(pojoClass));
            return list;
        }
        return query.executeAndFetch(pojoClass);
    }

    /**
     * 查询全表
     *
     * @param pojoClass 实体Class
     * @return
     */
    public <T> List<T> findAll(Class<T> pojoClass) {
        return findByProperties(pojoClass, null, false);
    }

    public InsertData getInsertData(final Object pojo, boolean ignoreNull) {
        TableInfo ti = PojoData.forClass(pojo.getClass()).getTableInfo();

        List<String> namesList = new ArrayList<>();
        List<String> valuesList = new ArrayList<>();

        if (ti.getSequenceColumn() != null
                && ti.getColumns().containsKey(ti.getSequenceColumn())) {
            // Oracle
            namesList.add(ti.getSequenceColumn());
            valuesList.add(ti.getSequenceName() + ".nextval");
        }

        Map<String, Object> parameterMap = new LinkedHashMap<>();
        for (PocoColumn column : ti.getColumns().values()) {
            if (!column.insertable()) {
                continue;
            }
            Object value = column.PropertyInfo.get(pojo);
            if (value == null && ignoreNull) {
                continue;
            }
            String propertyName = column.PropertyInfo.name;
            parameterMap.put(propertyName, value);

            namesList.add(column.ColumnName);
            valuesList.add(":" + propertyName);
        }

        // INSERT INTO %s (col) VALUES (:col)
        String queryText = "INSERT INTO " +
                ti.getTableName() +
                " ( " +
                StringUtils.join(namesList, ",") +
                " ) " +
                "VALUES (" +
                StringUtils.join(valuesList, ",") +
                " ) ";
        InsertData data = new InsertData();
        data.setSql(queryText);
        data.setParameter(parameterMap);
        return data;
    }

    /**
     * 将实体插入数据库
     *
     * @param pojo 实体对象
     * @return
     */
    public int insert(final Object pojo) {
        InsertData insertData = this.getInsertData(pojo, true);
        Query query = connection.createQuery(insertData.getSql());
        Map<String, Object> parameterMap = insertData.getParameters();
        query.addParameters(parameterMap);
        return query.executeUpdate().getResult();
    }

    /**
     * 批量插入
     *
     * @param pojoList 实体列表
     * @return
     */
    public <T> Connection insertList(final List<T> pojoList) {
        Object pojo = pojoList.get(0);
        InsertData insertData = this.getInsertData(pojo, false);
        Query query = connection.createQuery(insertData.getSql());
        Map<String, Object> parameterMap = insertData.getParameters();

        Class clazz = pojo.getClass();
        Map<String, PojoProperty> propertyMap = PojoIntrospector.collectProperties(clazz);
        for (T o : pojoList) {
            for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
                PojoProperty property = propertyMap.get(entry.getKey());
                if (property != null) {
                    Object value = property.get(o);
                    query.addParameter(entry.getKey(), value);
                }
            }
            query.addToBatch();
        }
        return query.executeBatch(); // executes entire batch
        // this.commit();//由外部提交
    }

    /**
     * 更新实体对应记录
     *
     * @param pojo 实体对象
     * @return
     */
    public int update(final Object pojo) {
        return this.update(pojo, null);
    }

    /**
     * 更新实体记录，可以指定要更新的列
     *
     * @param pojo       实体对象
     * @param properties 指定要更新的属性列表，可以放空(注意这边是映射的实体属性列表，不是数据库字段列表)
     * @return
     */
    public int update(final Object pojo, List<String> properties) {
        Class<?> clazz = pojo.getClass();
        TableInfo ti = PojoData.forClass(clazz).getTableInfo();

        List<String> setsList = new ArrayList<>();
        List<String> whereList = new ArrayList<>();

        Map<String, Object> parameterMap = new LinkedHashMap<>();

        for (String key : ti.getPrimaryKeys()) {
            PocoColumn keyColumn = ti.getColumns().get(key);
            Object value = keyColumn.PropertyInfo.get(pojo);
            if (value == null) continue;
            whereList.add(key + " = :" + key);
            parameterMap.put(key, value);
        }

        boolean updateSpatialColumns = properties != null && properties.size() > 0;
        if (updateSpatialColumns) {
            for (String property : properties) {
                PocoColumn pocoColumn = ti.getColumn(property);
                if (pocoColumn == null || !pocoColumn.updatable()) {
                    continue;
                }
                String columnName = pocoColumn.ColumnName;
                setsList.add(columnName + " = :" + columnName);

                Object value = pocoColumn.PropertyInfo.get(pojo);
                parameterMap.put(columnName, value);
            }
        } else {
            Object originalPojo = Sql2oCrudService.of(connection).withoutValidFlag().getById(clazz, pojo);
            if (originalPojo == null) {
                return -1;
            }
            Map<String, PocoColumn> changes = this.getChangedProperties(pojo, originalPojo);
            if (changes.size() == 0) {
                return 1;//表示操作成功
            }
            for (Map.Entry<String, PocoColumn> entry : changes.entrySet()) {
                PocoColumn pocoColumn = entry.getValue();
                if (!pocoColumn.updatable()) {
                    continue;//排除不可更新的字段(包括主键)
                }
                String columnName = entry.getKey();
                setsList.add(columnName + " = :" + columnName);

                Object value = pocoColumn.PropertyInfo.get(pojo);
                parameterMap.put(columnName, value);
            }
        }
        if (whereList.size() == 0) return -1;//表示找不到满足条件的记录，避免全表更新
        if (setsList.size() == 0) return 1;//表示操作成功

        String queryText = "UPDATE " +
                ti.getTableName() +
                " SET " +
                StringUtils.join(setsList, ",") +
                " WHERE " +
                StringUtils.join(whereList, " and ");
        Query query = connection.createQuery(queryText);
        query.addParameters(parameterMap);
        return query.executeUpdate().getResult();
    }

    /**
     * 逻辑删除实体对应的数据库记录
     *
     * @param pojo 实体对象
     * @return
     */
    public int delete(final Object pojo) {
        Class<?> pojoClass = pojo.getClass();
        return this.deleteById(pojoClass, pojo);
    }

    /**
     * 逻辑删除实体对应的数据库记录(如果不存在逻辑删除字段，则物理删除)
     *
     * @param pojoClass 实体类Class
     * @param idValue   单个主键值或整个实体对象或者包含主键信息的Map
     * @return
     */
    public int deleteById(Class<?> pojoClass, final Object idValue) {
        TableInfo ti = PojoData.forClass(pojoClass).getTableInfo();
        if (ValidableEntity.class.isAssignableFrom(pojoClass)) {
            Map<String, Object> wheres = getIdParams(pojoClass, ti, idValue, true);
            if (wheres.size() == 0) {
                throw new Sql2oException("无效的过滤条件！");
            }
            Map<String, Object> sets = new HashMap<>();
            sets.put(ValidableEntity.FieldName, -1);
            return this.updateByProperties(pojoClass, sets, wheres);
        }
        return this.removeById(pojoClass, idValue);
    }

    /**
     * 物理删除实体对应的数据库记录
     *
     * @param pojo 实体对象
     * @return
     */
    public int remove(final Object pojo) {
        Class<?> pojoClass = pojo.getClass();
        return this.removeById(pojoClass, pojo);
    }

    /**
     * 物理删除实体对应的数据库记录
     *
     * @param pojoClass 实体类Class
     * @param idValue   单个主键值或整个实体对象或者包含主键信息的Map
     * @return
     */
    public int removeById(Class<?> pojoClass, final Object idValue) {
        TableInfo ti = PojoData.forClass(pojoClass).getTableInfo();
        Map<String, Object> propertiesMap = getIdParams(pojoClass, ti, idValue, true);
        return this.removeByProperties(pojoClass, propertiesMap);
    }

    /**
     * 根据某个属性删除记录，逻辑删除
     *
     * @param pojoClass
     * @param propertyName
     * @param value
     * @param <T>
     * @return
     */
    public <T> int deleteByProperty(Class<T> pojoClass, String propertyName, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(propertyName, value);
        return this.deleteByProperties(pojoClass, params);
    }

    /**
     * 删除实体对应的数据库记录
     *
     * @param pojoClass 实体类Class
     * @param params    过滤的属性Map
     * @return
     */
    public <T> int deleteByProperties(Class<T> pojoClass, Map<String, Object> params) {
        if (ValidableEntity.class.isAssignableFrom(pojoClass)) {
            if (params.size() == 0) {
                throw new Sql2oException("无效的过滤条件！");
            }
            Map<String, Object> sets = new HashMap<>();
            sets.put(ValidableEntity.FieldName, -1);
            return this.updateByProperties(pojoClass, sets, params);
        }
        return removeByProperties(pojoClass, params);
    }

    /**
     * 根据某个属性删除记录,物理删除
     *
     * @param pojoClass
     * @param propertyName
     * @param value
     * @param <T>
     * @return
     */
    public <T> int removeByProperty(Class<T> pojoClass, String propertyName, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(propertyName, value);
        return this.removeByProperties(pojoClass, params);
    }

    /**
     * 删除实体对应的数据库记录
     *
     * @param pojoClass 实体类Class
     * @param params    过滤的属性Map
     * @return
     */
    public <T> int removeByProperties(Class<T> pojoClass, Map<String, Object> params) {
        TableInfo ti = PojoData.forClass(pojoClass).getTableInfo();
        WherePair wherePair = buildWherePair(params, ti);
        if (wherePair.size() == 0) {
            throw new Sql2oException("未包含过滤字段！");
        }

        String queryText = String.format("DELETE FROM %s WHERE %s",
                ti.getTableName(), wherePair.toSql(false));

        Query query = connection.createQuery(queryText);
        query.addParameters(wherePair.getParams());

        return query.executeUpdate().getResult();
    }

    /**
     * 获取主键参数
     */
    private Map<String, Object> getIdParams(Class pojoClass, TableInfo ti, final Object idValue) {
        return getIdParams(pojoClass, ti, idValue, false);
    }

    /**
     * 获取主键参数
     *
     * @param properties 是否返回key为属性名的列表
     */
    private Map<String, Object> getIdParams(Class pojoClass, TableInfo ti, final Object idValue, boolean properties) {
        Map<String, Object> params = new HashMap<>();
        if (idValue != null && idValue.getClass() == pojoClass) {
            for (String key : ti.getPrimaryKeys()) {
                PocoColumn keyColumn = ti.getColumns().get(key);
                Object value = keyColumn.PropertyInfo.get(idValue);
                if (value == null) continue;
                params.put(properties ? keyColumn.PropertyInfo.name : key, value);
            }
        } else if (idValue != null && idValue instanceof Map) {
            Map idValueMap = (Map) idValue;
            for (String key : ti.getPrimaryKeys()) {
                PocoColumn keyColumn = ti.getColumns().get(key);
                Object value = idValueMap.get(keyColumn.PropertyInfo.name);
                if (value == null) continue;
                params.put(properties ? keyColumn.PropertyInfo.name : key, value);
            }
        } else {
            PocoColumn keyColumn = ti.getColumns().get(ti.getPrimaryKey());
            if (keyColumn != null) {
                params.put(properties ? keyColumn.PropertyInfo.name : keyColumn.ColumnName, idValue);
            }
        }
        if (params.size() == 0) {
            throw new Sql2oException("无法获取主键参数！！！");
        }
        return params;
    }

    /**
     * 是否为特殊Null表示值
     *
     * @param value
     * @param valueClass
     * @return
     */
    private boolean isSpatialNullValue(Object value, Class<?> valueClass) {
        if (valueClass == Integer.class) {
            return NullInteger.equals(value);
        } else if (valueClass == Double.class) {
            return NullDouble.equals(value);
        } else if (valueClass == Float.class) {
            return NullFloat.equals(value);
        } else if (valueClass == BigDecimal.class) {
            return NullBigDecimal.equals(value);
        } else if (valueClass == String.class) {
            return NullString.equals(value);
        } else if (Date.class.isAssignableFrom(valueClass)) {
            return NullDate.equals(value);
        } else if (byte[].class == valueClass) {
            return NullDate.equals(value);
        }
        return false;
    }

    /**
     * 根据实体主键自动查询数据库并对比新旧实体的差异属性
     *
     * @param pojo
     * @param originalPojo
     * @return
     */
    private Map<String, PocoColumn> getChangedProperties(final Object pojo, Object originalPojo) {
        TableInfo ti = PojoData.forClass(pojo.getClass()).getTableInfo();
        Map<String, PocoColumn> changedPropertiesMap = new HashMap<>();
        Map<String, PocoColumn> properties = ti.getProperties();
        for (Map.Entry<String, PocoColumn> entry : properties.entrySet()) {
            PocoColumn column = ti.getColumn(entry.getKey());
            Object newValue = column.PropertyInfo.get(pojo);
            if (newValue == null) continue;
            Object oldValue = column.PropertyInfo.get(originalPojo);
            if (!Objects.equals(newValue, oldValue)) {
                Class<?> returnType = column.PropertyInfo.getGetMethod().getReturnType();
                if (returnType == String.class) {
                    //字符串类型要清空值要手动设置值为空字符串，null不起作用！
                    if ((oldValue == null && newValue.equals("")) || newValue.equals(oldValue)) {
                        continue;//字符串null表示不更新，要为空字符串才表示设置为null
                    }
                } else {
                    //1、null值表示不更新
                    //2、要更新为空的字段应该设置为特殊初始值
                    boolean isSpatialNull = isSpatialNullValue(newValue, returnType);
                    if (isSpatialNull) {
                        column.PropertyInfo.set(pojo, null);
                        changedPropertiesMap.put(column.ColumnName, column);
                        continue;
                    }
                    if (returnType == Integer.class
                            || returnType == Double.class
                            || returnType == Float.class
                            || returnType == BigDecimal.class
                            || Date.class.isAssignableFrom(returnType)) {
                        if (newValue instanceof String) {
                            newValue = CastUtils.cast(newValue, returnType);
                        }
                        if (newValue != null && newValue.equals(oldValue)) {
                            continue;
                        }
                    }
                }
                changedPropertiesMap.put(column.ColumnName, column);
            }
        }
        return changedPropertiesMap;
    }

    /**
     * 根据某些属性执行修改
     *
     * @param <T>
     * @param pojoClass
     * @param setProperties
     * @param whereProperties
     * @return
     */
    public <T> int updateByProperties(Class<T> pojoClass, Map<String, Object> setProperties, Map<String, Object> whereProperties) {
        TableInfo ti = PojoData.forClass(pojoClass).getTableInfo();

        List<String> setsList = new ArrayList<>();

        Map<String, Object> parameterMap = new LinkedHashMap<>();
        WherePair wherePair = buildWherePair(whereProperties, ti);
        if (wherePair.size() == 0) {
            throw new Sql2oException("未包含过滤字段！");
        }
        parameterMap.putAll(wherePair.getParams());

        for (Map.Entry<String, Object> entry : setProperties.entrySet()) {
            PocoColumn pocoColumn = ti.getColumn(entry.getKey());
            if (pocoColumn == null || !pocoColumn.updatable()) continue;//忽略不是表中的字段或者不能更新的字段

            String columnName = pocoColumn.ColumnName;
            String paramName = columnName;
            if (parameterMap.containsKey(paramName)) {
                paramName = paramName + System.currentTimeMillis();
            }
            setsList.add(columnName + " = :" + paramName);

            Object value = CastUtils.cast(entry.getValue(), pocoColumn.PropertyInfo.type);
            parameterMap.put(paramName, value);
        }

        if (setsList.size() == 0) {
            throw new Sql2oException("未包含更新字段！");
        }

        String queryText = "UPDATE " +
                ti.getTableName() +
                " SET " +
                StringUtils.join(setsList, ",") +
                " WHERE " +
                wherePair.toSql(false);
        Query query = connection.createQuery(queryText);
        query.addParameters(parameterMap);
        return query.executeUpdate().getResult();
    }

    /**
     * 根据某些属性执行修改
     *
     * @param <T>
     * @param pojoClass
     * @param propertiesMap 属性map，需要包含主键信息！！！
     * @return
     */
    public <T> int updateByProperties(Class<T> pojoClass, Map<String, Object> propertiesMap) {
        TableInfo ti = PojoData.forClass(pojoClass).getTableInfo();
        Map<String, Object> wheres = new LinkedHashMap<>();

        for (String columnName : ti.getPrimaryKeys()) {
            PocoColumn pocoColumn = ti.getColumns().get(columnName);
            Object value = CastUtils.cast(propertiesMap.get(pocoColumn.PropertyInfo.name),
                    pocoColumn.PropertyInfo.type);
            if (value == null) continue;
            wheres.put(columnName, value);
            propertiesMap.remove(pocoColumn.PropertyInfo.name);
        }

        return updateByProperties(pojoClass, propertiesMap, wheres);
    }

    /**
     * 根据某个属性统计记录数
     *
     * @param pojoClass    实体类
     * @param propertyName 筛选的属性（字段）
     * @param value        筛选的属性（字段）值
     * @param <T>
     * @return
     */
    public <T> int countByProperty(Class<T> pojoClass, String propertyName, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(propertyName, value);
        return this.countByProperties(pojoClass, params);
    }

    /**
     * 根据某些属性统计记录数
     *
     * @param pojoClass 实体类
     * @param params    筛选的属性（字段）
     * @return
     */
    public <T> int countByProperties(Class<T> pojoClass, Map<String, Object> params) {
        TableInfo ti = PojoData.forClass(pojoClass).getTableInfo();
        WherePair wherePair = buildWherePair(params, ti);
        if (wherePair.size() == 0) {
            throw new Sql2oException("未包含过滤字段！");
        }
        String queryText = String.format("SELECT COUNT(1) FROM %s WHERE %s",
                ti.getTableName(), wherePair.toSql(false));

        Query query = connection.createQuery(queryText);
        query.addParameters(wherePair.getParams());

        return query.executeScalar(Integer.class);
    }

    /**
     * 判断是否存在包含特定属性值的记录
     *
     * @param pojoClass
     * @param propertyName
     * @param value
     * @param <T>
     * @return
     */
    public <T> boolean isExistByProperty(Class<T> pojoClass, String propertyName, Object value) {
        return this.countByProperty(pojoClass, propertyName, value) > 0;
    }

    /**
     * 判断是否存在包含某些特定属性值的记录
     *
     * @param pojoClass 实体类
     * @param params    筛选的属性（字段）
     * @param <T>
     * @return
     */
    public <T> boolean isExistByProperties(Class<T> pojoClass, Map<String, Object> params) {
        return this.countByProperties(pojoClass, params) > 0;
    }

    private static class WherePair {

        private List<String> andSql = new ArrayList<>();
        private List<String> orSql = new ArrayList<>();
        private Map<String, Object> whereParams = new HashMap<>();

        public String toSql() {
            return toSql(true);
        }

        public String toSql(boolean startsWithAnd) {
            String sql = "";
            if (andSql.size() > 0) {
                sql += StringUtils.join(andSql, " AND ");
            }
            if (orSql.size() > 0) {
                String ors = StringUtils.join(orSql, " OR ");
                if (andSql.size() > 0) {
                    sql += " AND";
                }
                sql += " ( " + ors + " ) ";
            }
            if (size() > 0 && startsWithAnd) {
                return " AND " + sql;
            }
            return sql;
        }

        @Override
        public String toString() {
            return toSql(false);
        }

        public int size() {
            return andSql.size() + orSql.size();
        }

        public WherePair addParam(String key, Object value) {
            whereParams.put(key, value);
            return this;
        }

        public Object getParamValue(String key) {
            return whereParams.get(key);
        }

        public WherePair addClause(Boolean isOr, String clause) {
            if (isOr) orSql.add(clause);
            else andSql.add(clause);
            //orSql.add((isOr ? " OR " : " AND ") + clause);
            return this;
        }

        public WherePair andClause(String clause) {
            return addClause(false, clause);
        }

        public WherePair orClause(String clause) {
            return addClause(true, clause);
        }

        public Map<String, Object> getParams() {
            return whereParams;
        }
    }
}
