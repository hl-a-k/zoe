package com.zoe.framework.data.jpa.repository.support;

import com.zoe.framework.sql2o.CallableParameters;
import com.zoe.framework.sql2o.data.Table;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * SqlBag Repository
 * Created by X on 2017/7/4.
 */
@NoRepositoryBean
public interface Sql2oRepository<T, ID extends Serializable> extends Repository<T,ID> {

    void touchForCreate(Object entity);

    void touchForUpdate(Object entity);

    /**
     * 保存实体
     *
     * @param entity 实体
     * @return 保存后的实体
     */
    <S extends T> S save(S entity);

    /**
     * 保存实体列表，默认开启事务
     *
     * @param entities 实体列表
     * @return 批量执行结果
     */
    <S extends T> int[] save(List<S> entities);

    /**
     * 保存实体列表，指定是否开启事务
     *
     * @param entities 实体列表
     * @param useTransaction 是否使用事务,需要手动开启
     * @return 批量执行结果
     */
    <S extends T> int[] save(List<S> entities, boolean useTransaction);

    /**
     * 逻辑删除实体
     *
     * @param entity 实体
     */
    <S extends T> int delete(S entity);

    /**
     * 物理删除实体
     *
     * @param entity 实体
     */
    <S extends T> int remove(S entity);

    /**
     * 逻辑删除实体集合,默认启用事务控制
     *
     * @param entities 实体集合
     * @return 删除结果
     */
    <S extends T> int[] delete(List<S> entities);

    /**
     * 物理删除实体集合，默认启用事务控制
     *
     * @param entities 实体集合
     * @return 删除结果
     */
    <S extends T> int[] remove(List<S> entities);

    /**
     * 逻辑删除实体集合，手动指定是否开启事务控制
     *
     * @param entities 实体集合
     * @param useTransaction 指定是否开启事务
     * @return 删除结果
     */
    <S extends T> int[] delete(List<S> entities, boolean useTransaction);

    /**
     * 物理删除实体集合，手动指定是否开启事务控制
     *
     * @param entities 实体集合
     * @param useTransaction 指定是否开启事务
     * @return 删除结果
     */
    <S extends T> int[] remove(List<S> entities, boolean useTransaction);

    /**
     * 逻辑删除记录
     *
     * @param id 主键
     * @return 删除结果
     */
    int delete(ID id);

    /**
     * 物理删除记录
     *
     * @param id 主键
     * @return 删除结果
     */
    int remove(ID id);

    /**
     * 逻辑删除记录
     *
     * @param id 主键
     * @return 删除结果
     */
    <A extends Persistable> int delete(Class<A> domain, ID id);

    /**
     * 物理删除记录
     *
     * @param id 主键
     * @return 删除结果
     */
    <A> int remove(Class<A> domain, ID id);

    /**
     * 批量删除对象，默认开启事务，逻辑删除
     *
     * @param ids 主键列表
     */
    int delete(Iterable<? extends ID> ids);

    /**
     * 批量删除对象，默认开启事务，物理删除
     *
     * @param ids 主键列表
     */
    int remove(Iterable<? extends ID> ids);

    /**
     * 批量删除对象，指定是否开启事务，逻辑删除
     *
     * @param ids            主键列表
     * @param useTransaction 指定是否开启事务
     */
    int delete(Iterable<? extends ID> ids, boolean useTransaction);

    /**
     * 批量删除对象，指定是否开启事务，物理删除
     *
     * @param ids            主键列表
     * @param useTransaction 指定是否开启事务
     */
    int remove(Iterable<? extends ID> ids, boolean useTransaction);

    /**
     * 根据某些字段执行删除，逻辑删除
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    int deleteByProperty(String propertyName, Object value);

    /**
     * 根据某些字段执行删除，物理删除
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    int removeByProperty(String propertyName, Object value);

    /**
     * 根据某些字段执行删除
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    <A> int deleteByProperty(Class<A> domain, String propertyName, Object value);

    /**
     * 根据某些字段执行删除，物理删除
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    <A> int removeByProperty(Class<A> domain, String propertyName, Object value);

    /**
     * 根据某些字段执行删除
     *
     * @param params 属性字典
     * @return
     */
    int deleteByProperties(Map<String, Object> params);

    /**
     * 根据某些字段执行删除，物理删除
     *
     * @param params 属性字典
     * @return
     */
    int removeByProperties(Map<String, Object> params);

    /**
     * 根据某些字段执行删除
     *
     * @param params 属性字典
     * @return
     */
    <A> int deleteByProperties(Class<A> domain, Map<String, Object> params);

    /**
     * 根据某些字段执行删除，物理删除
     *
     * @param domain
     * @param params 属性字典
     * @return
     */
    <A> int removeByProperties(Class<A> domain, Map<String, Object> params);

    /**
     * 删除集合
     * @param propertyName 属性名
     * @param propertyValues 属性值列表
     * @return 受影响行数
     */
    int deleteIn(String propertyName, Iterable<? extends Serializable> propertyValues);

    /**
     * 删除集合(物理删除)
     * @param propertyName 属性名
     * @param propertyValues 属性值列表
     * @return 受影响行数
     */
    int removeIn(String propertyName, Iterable<? extends Serializable> propertyValues);

    /**
     * 更新实体
     *
     * @param entity 实体
     */
    <S extends Persistable> int update(S entity);

    /**
     * 更新实体
     *
     * @param entity 实体
     * @param updateProperties 更新的属性列表
     */
    <S extends Persistable> int update(S entity, List<String> updateProperties);

    /**
     * 根据某些字段执行修改
     *
     * @param propertiesMap 属性字典（必须包含主键信息）
     * @return
     */
    int updateByProperties(Map<String, Object> propertiesMap);

    /**
     * 根据某些字段执行修改
     *
     * @param propertiesMap 属性字典（必须包含主键信息）
     * @return
     */
    <A> int updateByProperties(Class<A> domain, Map<String, Object> propertiesMap);

    /**
     * 根据某些字段执行修改
     *
     * @param propertiesMap 属性字典（必须包含主键信息）
     * @param fetchRecord 是否重新读取记录作对比，以仅仅更新变更字段
     * @return
     */
    int updateByProperties(Map<String, Object> propertiesMap, boolean fetchRecord);

    /**
     * 根据某些字段执行修改
     *
     * @param propertiesMap 属性字典（必须包含主键信息）
     * @param fetchRecord 是否重新读取记录作对比，以仅仅更新变更字段
     * @return
     */
    <A> int updateByProperties(Class<A> domain, Map<String, Object> propertiesMap, boolean fetchRecord);

    /**
     * 根据某些字段执行修改
     *
     * @param sets 更新的属性集合
     * @param wheres 过滤的属性集合
     * @return
     */
    <A> int updateByProperties(Map<String, Object> sets, Map<String, Object> wheres);

    /**
     * 根据某些字段执行修改
     *
     * @param sets 更新的属性集合
     * @param wheres 过滤的属性集合
     * @return
     */
    <A> int updateByProperties(Class<A> domain, Map<String, Object> sets, Map<String, Object> wheres);
    
    /**
     * 保存或更新一个对象
     *
     * @param o 对象
     */
    int saveOrUpdate(T o);

    /**
     * 通过主键查找实体
     *
     * @param id 主键
     * @return 实体
     */
    T findById(ID id);

    /**
     * 通过主键查找实体
     *
     * @param domain  实体类型
     * @param id 主键
     * @return 类型
     */
    <A> A findById(Class<A> domain, ID id);

    /**
     * 通过SQL语句获取一个对象
     *
     * @param sql SQL语句
     * @return 对象
     */
    T findBySql(String sql);

    /**
     * 查询某个类型的单条语句
     *
     * @param domain   类名.class
     * @param sql sql语句
     * @return 对象
     */
    <A> A findBySql(Class<A> domain, String sql);

    /**
     * 通过SQL语句获取一个对象
     *
     * @param sql    SQL语句
     * @param params 参数
     * @return 对象
     */
    T findBySql(String sql, Map<String, Object> params);

    /**
     * 查询某个类型的单条语句
     *
     * @param sql sql语句
     * @return 对象
     */
    Map findMapBySql(String sql);

    /**
     * 查询某个类型的单条语句
     *
     * @param sql sql语句
     * @return 对象
     */
    Map findMapBySql(String sql, Map<String, Object> params);

    /**
     * 查询某个类型的单条语句
     *
     * @param domain 类名.class
     * @param sql    sql语句
     * @param params 参数
     * @return 对象
     */
    <A> A findBySql(Class<A> domain, String sql, Map<String, Object> params);

    /**
     * 根据某个字段查询
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    T findFirst(String propertyName, Object value);

    /**
     * 根据某个字段查询
     *
     * @param params 属性列表
     * @return
     */
    T findFirst(Map<String, Object> params);

    /**
     * 根据某个字段查询
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    List<T> findList(String propertyName, Object value);

    /**
     * 根据某个字段查询
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    <A> List<A> findList(Class<A> domain, String propertyName, Object value);

    /**
     * 根据某个字段查询
     *
     * @param params 属性列表
     * @return
     */
    List<T> findList(Map<String, Object> params);

    /**
     * 根据某个字段查询
     *
     * @param params 属性列表
     * @return
     */
    <A> List<A> findList(Class<A> domain, Map<String, Object> params);

    /**
     * 获得结果集
     *
     * @param sql SQL语句
     * @return 结果集
     */
    List<T> findListBySql(String sql);

    /**
     * 获得结果集
     *
     * @param sql    SQL语句
     * @param params 参数
     * @return 结果集
     */
    List<T> findListBySql(String sql, Map<String, Object> params);

    /**
     * 获得结果集
     *
     * @return 结果集
     */
    <A> List<A> findListBySql(Class<A> domain, QueryInfo queryInfo);

    /**
     * 获得结果集
     *
     * @param sql    SQL语句
     * @param domain 返回的实体类型
     * @return 结果集
     */
    <A> List<A> findListBySql(Class<A> domain, String sql);

    /**
     * 获得结果集
     *
     * @param domain 返回的实体类型
     * @param sql    SQL语句
     * @param params 参数
     * @return 结果集
     */
    <A> List<A> findListBySql(Class<A> domain, String sql, Map<String, Object> params);

    /**
     * 获取所有记录
     *
     * @return List
     */
    List<T> findAll();

    /**
     * 查询集合
     * @param propertyName 属性名
     * @param propertyValues 属性值列表
     * @return
     */
    List<T> findIn(String propertyName, Iterable<? extends Serializable> propertyValues);

    /**
     * 列表查询
     *
     * @param query 查询条件集合
     * @return 列表查询结果集
     */
    List<T> findList(QueryInfo query);

    /**
     * 列表查询
     *
     * @param query 查询条件集合
     * @return 列表查询结果集
     */
    List<Map> findMapList(QueryInfo query);

    /**
     * 获得结果集
     *
     * @param sql SQL语句
     * @return 结果集
     */
    List<Map> findMapListBySql(String sql);

    /**
     * 获得结果集
     *
     * @param sql SQL语句
     * @return 结果集
     */
    List<Map> findMapListBySql(String sql, Map<String, Object> params);

    /**
     * 获得结果集
     *
     * @param sql SQL语句
     * @return 结果集
     */
    Table findTableBySql(String sql);

    /**
     * 获得结果集
     *
     * @param sql SQL语句
     * @return 结果集
     */
    Table findTableBySql(String sql, Map<String, Object> params);

    /**
     * 获得结果集
     *
     * @param queryInfo 查询信息
     * @return 结果集
     */
    Table findTable(QueryInfo queryInfo);

    /**
     * 分页查询
     *
     * @param query 查询条件集合
     * @return 分页结果集
     */
    Page<T> findPage(QueryInfo query);

    /**
     * 分页查询
     *
     * @param query 查询条件集合
     * @return 分页结果集
     */
    <A> Page<A> findPage(Class<A> domain, QueryInfo query);

    /**
     * 分页查询
     *
     * @param query 查询条件集合
     * @return 分页结果集
     */
    Page<Map> findPageMap(QueryInfo query);

    /**
     * 获得结果集包含统计总数
     *
     * @param sqlPage  SQL分页语句
     * @param sqlCount SQL统计语句
     * @return 结果集
     */
    Page<T> findPage(String sqlPage, String sqlCount, QueryInfo queryInfo);

    /**
     * 获得结果集包含统计总数
     *
     * @param sqlPage  SQL分页语句
     * @param sqlCount SQL统计语句
     * @return 结果集
     */
    <A> Page<A> findPage(Class<A> domain, String sqlPage, String sqlCount, QueryInfo query);

    /**
     * 获取一个返回值
     *
     * @param sql        SQL语句
     * @param params     参数
     * @param returnType 返回类型
     * @return
     */
    <A> A executeScalar(String sql, Map<String, Object> params, Class<A> returnType);

    /**
     * 获取一个返回值
     *
     * @param sql        sql 语句
     * @param returnType 返回类型
     * @return
     */
    <A> A executeScalar(String sql, Class<A> returnType);

    /**
     * 获取一个返回列表值
     *
     * @param sql        SQL语句
     * @param params     参数
     * @param returnType 返回类型
     * @return
     */
    <A> List<A> executeScalarList(String sql, Map<String, Object> params, Class<A> returnType);

    /**
     * 获取一个返回列表值
     *
     * @param sql        sql 语句
     * @param returnType 返回类型
     * @return
     */
    <A> List<A> executeScalarList(String sql, Class<A> returnType);

    /**
     * 执行存储过程，获取一个返回值
     * 执行存储过程 首字段用于处理返回值 所以存储过程写法必须是 {?=call PRODUCENAME(?,?...,?)}
     *
     * @param sql        SQL语句
     * @param returnType 返回类型
     * @param params     参数
     * @return
     */
    <A> A executeCall(String sql, Class<A> returnType, Object... params);

    /**
     * 执行存储过程 存储过程写法必须是 {call PRODUCENAME(?,?...,?)}
     *
     * @param sql    SQL语句
     * @param params 参数
     * @return
     */
    void executeCall(String sql, CallableParameters params);

    /**
     * 执行SQL语句
     *
     * @param sql SQL语句
     * @return 响应行数
     */
    int executeSql(String sql);

    /**
     * 执行一条SQL语句
     *
     * @param sql    SQL语句
     * @param params 参数
     * @return 响应结果数目
     */
    int executeSql(String sql, Map<String, Object> params);

    /**
     * 批量执行SQL语句
     * @param sql
     * @param params
     * @return
     */
    Object executeBatch(List<String> sql, Map<String, Object> params);

    /**
     * 获取数据库当前时间
     *
     * @return 当前时间
     */
    Date now();
}
