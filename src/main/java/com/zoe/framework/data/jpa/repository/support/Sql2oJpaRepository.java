package com.zoe.framework.data.jpa.repository.support;

import com.zoe.framework.data.auditing.AuditingHandler;
import com.zoe.framework.data.jpa.domain.AuditableEntity;
import com.zoe.framework.data.jpa.domain.ValidableEntity;
import com.zoe.framework.sql2o.*;
import com.zoe.framework.sql2o.data.PojoData;
import com.zoe.framework.sql2o.data.Table;
import com.zoe.framework.sql2o.data.TableInfo;
import com.zoe.framework.sql2o.paging.SQLParts;
import com.zoe.framework.sql2o.query.QueryMap;
import com.zoe.framework.sql2o.quirks.NoQuirks;
import com.zoe.framework.sql2o.quirks.OracleQuirks;
import com.zoe.framework.util.BeanUtils;
import com.zoe.framework.util.LRULinkedHashMap;
import org.beetl.sql.core.engine.SQLParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Persistable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * JpaRepository的sql2o版本实现
 * Created by caizhicong on 2017/7/4.
 */
@Transactional(readOnly = true)
public class Sql2oJpaRepository<T extends Persistable, ID extends Serializable> implements Sql2oRepository<T, ID> {

    private Logger logger = LoggerFactory.getLogger(Sql2oJpaRepository.class);
    private Class<T> domainClass;
    private final Sql2o sql2o;
    private final AuditingHandler auditingHandler;
    private static final Map<Integer, SQLParts> pagingCacheMap = new LRULinkedHashMap<>(100);

    @Autowired(required = false)
    public Sql2oJpaRepository(Class<T> domainClass, Sql2o sql2o, AuditingHandler auditingHandler) {
        Assert.notNull(auditingHandler, "AuditingHandler must not be null!");
        if (domainClass == null) {
            domainClass = BeanUtils.getParameterizedClass(this.getClass());
        }
        this.domainClass = domainClass;
        this.sql2o = sql2o;
        this.auditingHandler = auditingHandler;
    }

    /**
     * 获得当前Sql2o
     *
     * @return SqlBag
     */
    public Sql2o getSql2o() {
        return sql2o;
    }

    /**
     * 获取当前领域实体类型
     *
     * @return
     */
    public Class<T> getDomainClass() {
        return domainClass;
    }

    /**
     * Sets modification and creation date and auditor on the target object in case it implements {@link Persistable} on
     * persist events.
     *
     * @param entity
     */
    public void touchForCreate(Object entity) {
        if (auditingHandler != null) {
            auditingHandler.markCreated(entity);
        }
    }

    /**
     * Sets modification and creation date and auditor on the target object in case it implements {@link Persistable} on
     * update events.
     *
     * @param entity
     */
    public void touchForUpdate(Object entity) {
        if (auditingHandler != null) {
            auditingHandler.markModified(entity);
        }
    }

    //region previous version

    @Transactional
    public <S extends T> S save(S entity) {
        touchForCreate(entity);//mark created
        try (Connection con = sql2o.open()) {
            Sql2oCrudService.of(con).insert(entity);
        }
        return entity;
    }

    @Transactional
    public <S extends T> int[] save(List<S> entities) throws Sql2oException {
        return save(entities, false);
    }

    //@Transactional
    public <S extends T> int[] save(List<S> entities, boolean useTransaction) throws Sql2oException {
        if (entities != null && entities.size() > 0) {
            entities.forEach(this::touchForCreate);
            Connection conn = useTransaction ? sql2o.beginTransaction() : sql2o.open();
            try (Connection con = conn) {
                int[] batchResult = Sql2oCrudService.of(con).insertList(entities).getBatchResult();
                if (useTransaction) {
                    con.commit();
                }
                return batchResult;
            } catch (Exception ex) {
                if (conn != null && useTransaction) {
                    conn.rollback();
                }
                throw new Sql2oException(ex.getMessage(), ex);
            }
        }
        return null;
    }

    /**
     * 删除一个对象
     *
     * @param entity 对象
     */
    @Transactional
    @Override
    public <S extends T> int delete(S entity) throws Sql2oException {
        touchForUpdate(entity);
        try (Connection con = sql2o.open()) {
            int affectedRows = Sql2oCrudService.of(con).delete(entity);
            return affectedRows;
        }
    }

    /**
     * 删除一个对象
     *
     * @param entity 对象
     */
    @Transactional
    @Override
    public <S extends T> int remove(S entity) {
        try (Connection con = sql2o.open()) {
            int affectedRows = Sql2oCrudService.of(con).remove(entity);
            return affectedRows;
        }
    }

    /**
     * 删除一组记录,默认启用事务控制
     *
     * @param entities
     * @return
     */
    @Transactional
    @Override
    public <S extends T> int[] delete(List<S> entities) throws Sql2oException {
        return delete(entities, false);
    }

    /**
     * 删除一组记录,默认启用事务控制
     *
     * @param entities
     * @return
     */
    @Transactional
    @Override
    public <S extends T> int[] remove(List<S> entities) throws Sql2oException {
        return remove(entities, false);
    }

    /**
     * 逻辑删除一个对象
     *
     * @param entities       对象
     * @param useTransaction 指定是否开启事务
     */
    @Override
    public <S extends T> int[] delete(List<S> entities, boolean useTransaction) throws Sql2oException {
        if (entities != null && entities.size() > 0) {
            entities.forEach(this::touchForUpdate);
            Connection conn = useTransaction ? sql2o.beginTransaction() : sql2o.open();
            try (Connection con = conn) {
                int[] affectedRows = new int[entities.size()];
                for (int i = 0; i < affectedRows.length; i++) {
                    affectedRows[i] = Sql2oCrudService.of(con).delete(entities.get(i));
                }
                if (useTransaction) {
                    con.commit();
                }
                return affectedRows;
            } catch (Exception ex) {
                if (conn != null && useTransaction) {
                    conn.rollback();
                }
                throw new Sql2oException(ex.getMessage(), ex);
            }
        }
        return null;
    }

    /**
     * 逻辑删除一个对象
     *
     * @param entities       对象
     * @param useTransaction 指定是否开启事务
     */
    @Override
    public <S extends T> int[] remove(List<S> entities, boolean useTransaction) throws Sql2oException {
        if (entities != null && entities.size() > 0) {
            entities.forEach(this::touchForUpdate);
            Connection conn = useTransaction ? sql2o.beginTransaction() : sql2o.open();
            try (Connection con = conn) {
                int[] affectedRows = new int[entities.size()];
                for (int i = 0; i < affectedRows.length; i++) {
                    affectedRows[i] = Sql2oCrudService.of(con).remove(entities.get(i));
                }
                if (useTransaction) {
                    con.commit();
                }
                return affectedRows;
            } catch (Exception ex) {
                if (conn != null && useTransaction) {
                    conn.rollback();
                }
                throw new Sql2oException(ex.getMessage(), ex);
            }
        }
        return null;
    }

    /**
     * 逻辑删除一个对象
     *
     * @param id 主键
     */
    @Transactional
    public int delete(ID id) throws Sql2oException {
        Class<T> domain = this.getDomainClass();
        return delete(domain, id);
    }

    /**
     * 删除一个对象
     *
     * @param id 主键
     */
    @Transactional
    public int remove(ID id) throws Sql2oException {
        Class<T> domain = this.getDomainClass();
        return remove(domain, id);
    }

    /**
     * 逻辑删除一个对象
     *
     * @param domain
     * @param id     对象
     */
    @Transactional
    public <A extends Persistable> int delete(Class<A> domain, ID id) throws Sql2oException {
        if (id == null) {
            return -1;
        }
        A entity;
        try (Connection con = sql2o.open()) {
            entity = Sql2oCrudService.of(con).withoutValidFlag().getById(domain, id);
        }
        if (entity != null) {
            List<String> fields = new ArrayList<>();
            if (entity instanceof ValidableEntity) {
                ((ValidableEntity) entity).setValidFlag(ValidableEntity.DELETED);
                fields.add(ValidableEntity.FieldName);
            }
            if (entity instanceof AuditableEntity) {
                fields.add("modifyUser");
                fields.add("modifyTime");
            }
            return update(entity, fields);
        }
        return -1;
    }

    /**
     * 删除一个对象
     *
     * @param domain
     * @param id     对象
     */
    @Transactional
    public <A> int remove(Class<A> domain, ID id) throws Sql2oException {
        try (Connection con = sql2o.open()) {
            int affectedRows = Sql2oCrudService.of(con).withoutValidFlag().removeById(domain, id);
            return affectedRows;
        }
    }

    /**
     * 批量删除对象，默认开启事务
     *
     * @param ids 主键列表
     */
    @Transactional
    public int delete(Iterable<? extends ID> ids) throws Sql2oException {
        return delete(ids, false);
    }

    /**
     * 批量删除对象，默认开启事务
     *
     * @param ids 主键列表
     */
    @Transactional
    public int remove(Iterable<? extends ID> ids) throws Sql2oException {
        return remove(ids, false);
    }

    /**
     * 批量删除对象，指定是否开启事务
     *
     * @param ids            主键列表
     * @param useTransaction 指定是否开启事务
     */
    //@Transactional
    public int delete(Iterable<? extends ID> ids, boolean useTransaction) throws Sql2oException {
        Class<T> domain = this.getDomainClass();
        Connection conn = useTransaction ? sql2o.beginTransaction() : sql2o.open();
        try (Connection con = conn) {
            int affectedRows = 0;
            Sql2oCrudService service = Sql2oCrudService.of(con).withoutValidFlag();
            for (Serializable id : ids) {
                affectedRows += service.deleteById(domain, id);
            }
            if (useTransaction) {
                con.commit();
            }
            return affectedRows;
        } catch (Exception ex) {
            if (conn != null && useTransaction) {
                conn.rollback();
            }
            throw new Sql2oException(ex.getMessage(), ex);
        }
    }

    /**
     * 批量删除对象，指定是否开启事务
     *
     * @param ids            主键列表
     * @param useTransaction 指定是否开启事务
     */
    @Override
    public int remove(Iterable<? extends ID> ids, boolean useTransaction) throws Sql2oException {
        Class<T> domain = this.getDomainClass();
        Connection conn = useTransaction ? sql2o.beginTransaction() : sql2o.open();
        try (Connection con = conn) {
            int affectedRows = 0;
            Sql2oCrudService service = Sql2oCrudService.of(con).withoutValidFlag();
            for (Serializable id : ids) {
                affectedRows += service.removeById(domain, id);
            }
            if (useTransaction) {
                con.commit();
            }
            return affectedRows;
        } catch (Exception ex) {
            if (conn != null && useTransaction) {
                conn.rollback();
            }
            throw new Sql2oException(ex.getMessage(), ex);
        }
    }

    /**
     * 根据某些字段执行删除
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    @Transactional
    @Override
    public int deleteByProperty(String propertyName, Object value) throws Sql2oException {
        Class<T> domain = getDomainClass();
        Map<String, Object> sets = new HashMap<>();
        sets.put("validFlag", -1);
        touchForUpdate(sets);
        Map<String, Object> wheres = new HashMap<>();
        wheres.put(propertyName, value);
        wheres.put("validFlag", 1);
        try (Connection con = sql2o.open()) {
            //Sql2oCrudService.of(con).deleteByProperty(domain, propertyName, value);
            return Sql2oCrudService.of(con).withoutValidFlag().updateByProperties(domain, sets, wheres);
        }
    }

    /**
     * 根据某些字段执行删除
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    @Transactional
    @Override
    public <A> int deleteByProperty(Class<A> domain, String propertyName, Object value) throws Sql2oException {
        Map<String, Object> sets = new HashMap<>();
        sets.put("validFlag", -1);
        touchForUpdate(sets);
        Map<String, Object> wheres = new HashMap<>();
        wheres.put(propertyName, value);
        wheres.put("validFlag", 1);
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).withoutValidFlag().updateByProperties(domain, sets, wheres);
        }
    }

    /**
     * 根据某些字段执行删除
     *
     * @param params 属性字典
     * @return
     */
    @Transactional
    @Override
    public int deleteByProperties(Map<String, Object> params) throws Sql2oException {
        Class<T> domain = getDomainClass();
        Map<String, Object> sets = new HashMap<>();
        sets.put("validFlag", -1);
        touchForUpdate(sets);
        params.put("validFlag", 1);
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).withoutValidFlag().updateByProperties(domain, sets, params);
        }
    }

    /**
     * 根据某些字段执行删除
     *
     * @param domain
     * @param params 属性字典
     * @return
     */
    @Transactional
    @Override
    public <A> int deleteByProperties(Class<A> domain, Map<String, Object> params) throws Sql2oException {
        Map<String, Object> sets = new HashMap<>();
        sets.put("validFlag", -1);
        touchForUpdate(sets);
        params.put("validFlag", 1);
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).withoutValidFlag().updateByProperties(domain, sets, params);
        }
    }

    /**
     * 根据某些字段执行删除
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    @Transactional
    @Override
    public int removeByProperty(String propertyName, Object value) throws Sql2oException {
        Class<T> domain = getDomainClass();
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).removeByProperty(domain, propertyName, value);
        }
    }

    /**
     * 根据某些字段执行删除，物理删除
     *
     * @param domain
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    @Transactional
    @Override
    public <A> int removeByProperty(Class<A> domain, String propertyName, Object value) {
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).removeByProperty(domain, propertyName, value);
        }
    }

    /**
     * 根据某些字段执行删除
     *
     * @param params 属性字典
     * @return
     */
    @Transactional
    @Override
    public int removeByProperties(Map<String, Object> params) throws Sql2oException {
        Class<T> domain = getDomainClass();
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).removeByProperties(domain, params);
        }
    }

    /**
     * 根据某些字段执行删除
     *
     * @param domain
     * @param params 属性字典
     * @return
     */
    @Transactional
    @Override
    public <A> int removeByProperties(Class<A> domain, Map<String, Object> params) throws Sql2oException {
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).removeByProperties(domain, params);
        }
    }

    @Transactional
    @Override
    public int deleteIn(String propertyName, Iterable<? extends Serializable> propertyValues) {
        Iterable<String> aa = new ArrayList<>();
        return deleteOrRemove(false, propertyName, propertyValues);
    }

    @Transactional
    @Override
    public int removeIn(String propertyName, Iterable<? extends Serializable> propertyValues) {
        return deleteOrRemove(true, propertyName, propertyValues);
    }

    private int deleteOrRemove(boolean remove, String propertyName, Iterable<? extends Serializable> propertyValues) {
        Class<T> domain = getDomainClass();
        PojoData pojoData = PojoData.forClass(domain);
        TableInfo ti = pojoData.getTableInfo();
        Map<String, Object> params = new HashMap<>();
        int i = 0;
        for (Object value : propertyValues) {
            params.put(propertyName + (i++), value);
        }
        String queryText = String.format(remove ? "DELETE FROM %s WHERE %s IN (%s)" : "UPDATE %s SET VALID_FLAG=-1 WHERE %s IN (%s)",
                ti.getTableName(), propertyName, StringUtils.collectionToDelimitedString(params.keySet(), ",", ":", ""));

        try (Connection con = sql2o.open()) {
            Query query = createQuery(con, queryText, params);
            return query.executeUpdate().getResult();
        }
    }

    /**
     * 更新一个对象
     *
     * @param entity 对象
     */
    @Transactional
    @Override
    public <S extends Persistable> int update(S entity) throws Sql2oException {
        touchForUpdate(entity);
        try (Connection con = sql2o.open()) {
            int affectedRows = Sql2oCrudService.of(con).update(entity, null);
            return affectedRows;
        }
    }

    /**
     * 更新一个对象
     *
     * @param entity           对象
     * @param updateProperties 更新的属性列表
     */
    @Transactional
    @Override
    public <S extends Persistable> int update(S entity, List<String> updateProperties) throws Sql2oException {
        touchForUpdate(entity);
        try (Connection con = sql2o.open()) {
            int affectedRows = Sql2oCrudService.of(con).update(entity, updateProperties);
            return affectedRows;
        }
    }

    /**
     * 根据某些字段执行修改
     *
     * @param propertiesMap 属性字典（必须包含主键信息）
     * @return
     */
    @Transactional
    @Override
    public int updateByProperties(Map<String, Object> propertiesMap) throws Sql2oException {
        Class<T> domain = getDomainClass();
        touchForUpdate(propertiesMap);
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).updateByProperties(domain, propertiesMap);
        }
    }

    /**
     * 根据某些字段执行修改
     *
     * @param domain
     * @param propertiesMap 属性字典（必须包含主键信息）
     * @return
     */
    @Transactional
    @Override
    public <A> int updateByProperties(Class<A> domain, Map<String, Object> propertiesMap) throws Sql2oException {
        touchForUpdate(propertiesMap);
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).updateByProperties(domain, propertiesMap);
        }
    }

    /**
     * 根据某些字段执行修改
     *
     * @param propertiesMap 属性字典（必须包含主键信息）
     * @param fetchRecord   是否重新读取记录作对比，以仅仅更新变更字段
     * @return
     */
    @Transactional
    @Override
    public int updateByProperties(Map<String, Object> propertiesMap, boolean fetchRecord) throws Sql2oException {
        Class<T> domain = getDomainClass();
        touchForUpdate(propertiesMap);
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).updateByProperties(domain, propertiesMap);
        }
    }

    /**
     * 根据某些字段执行修改
     *
     * @param propertiesMap 属性字典（必须包含主键信息）
     * @param fetchRecord   是否重新读取记录作对比，以仅仅更新变更字段
     * @return
     */
    @Transactional
    @Override
    public <A> int updateByProperties(Class<A> domain, Map<String, Object> propertiesMap, boolean fetchRecord) throws Sql2oException {
        touchForUpdate(propertiesMap);
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).updateByProperties(domain, propertiesMap);
        }
    }

    /**
     * 根据某些字段执行修改
     *
     * @param sets   更新的属性集合
     * @param wheres 过滤的属性集合
     * @return
     */
    @Transactional
    @Override
    public <A> int updateByProperties(Map<String, Object> sets, Map<String, Object> wheres) throws Sql2oException {
        if (wheres == null || wheres.size() == 0) {
            throw new Sql2oException("未指定过滤条件！");
        }
        Class<T> domain = getDomainClass();
        touchForUpdate(sets);
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).updateByProperties(domain, sets, wheres);
        }
    }

    /**
     * 根据某些字段执行修改
     *
     * @param sets   更新的属性集合
     * @param wheres 过滤的属性集合
     * @return
     */
    @Transactional
    @Override
    public <A> int updateByProperties(Class<A> domain, Map<String, Object> sets, Map<String, Object> wheres) throws Sql2oException {
        touchForUpdate(sets);
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).updateByProperties(domain, sets, wheres);
        }
    }

    /**
     * 保存或更新一个对象
     *
     * @param o 对象
     */
    @Transactional
    @Override
    public int saveOrUpdate(T o) throws Sql2oException {
        Class<T> domain = this.getDomainClass();
        try (Connection con = sql2o.open()) {
            int affectedRows;
            if (StringUtils.isEmpty(o.getId())) {
                touchForCreate(o);
                affectedRows = Sql2oCrudService.of(con).insert(o);
            } else {
                touchForUpdate(o);
                affectedRows = Sql2oCrudService.of(con).update(o, null);
            }
            return affectedRows;
        }
    }

    /**
     * 通过主键获得对象
     *
     * @param id 主键
     * @return <T>对象
     */
    @Override
    public T findById(ID id) {
        Class<T> domain = this.getDomainClass();
        return this.findById(domain, id);
    }

    /**
     * 通过主键获得对象
     *
     * @param domain 类名.class
     * @param id     主键
     * @return 对象
     */
    @Override
    public <A> A findById(Class<A> domain, ID id) {
        if (id == null) {
            return null;
        }
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).getById(domain, id);
        }
    }

    /**
     * 通过SQL语句获取一个对象
     *
     * @param sql SQL语句
     * @return 对象
     */
    @Override
    public T findBySql(String sql) {
        return findBySql(getDomainClass(), sql, null);
    }

    @Override
    public Map findMapBySql(String sql) {
        return findBySql(Map.class, sql, null);
    }

    /**
     * 获得结果集
     *
     * @param domain 返回的实体类型
     * @param sql    SQL语句
     * @return 结果集
     */
    @Override
    public <A> A findBySql(Class<A> domain, String sql) {
        return findBySql(domain, sql, null);
    }

    /**
     * 通过SQL语句获取一个对象
     *
     * @param sql    SQL语句
     * @param params 参数
     * @return 对象
     */
    @Override
    public T findBySql(String sql, Map<String, Object> params) {
        return findBySql(getDomainClass(), sql, params);
    }

    /**
     * 通过SQL语句获取一个Map对象
     *
     * @param sql    SQL语句
     * @param params 参数
     * @return 对象
     */
    @Override
    public Map findMapBySql(String sql, Map<String, Object> params) {
        return findBySql(Map.class, sql, params);
    }

    /**
     * 查询某个类型的单条语句
     *
     * @param domain 类名.class
     * @param sql    sql语句
     * @param params 参数
     * @return 对象
     */
    @Override
    public <A> A findBySql(Class<A> domain, String sql, Map<String, Object> params) {
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setSql(sql);
        queryInfo.setQueryItems(params);
        return queryFirst(domain, queryInfo);
    }

    /**
     * 获取所有记录
     *
     * @return List
     */
    @Override
    public List<T> findAll() {
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).findAll(getDomainClass());
        }
    }

    /**
     * 根据某个字段查询
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    @Override
    public T findFirst(String propertyName, Object value) {
        QueryMap params = QueryMap.create().add(propertyName, value);
        if (params.size() == 0) return null;
        Class<T> domain = getDomainClass();
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).findFirstByProperties(domain, params);
        }
    }

    /**
     * 根据某个字段查询
     *
     * @param params 属性列表
     * @return
     */
    @Override
    public T findFirst(Map<String, Object> params) {
        if(params == null || params.size() == 0) return null;
        Class<T> domain = getDomainClass();
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).findFirstByProperties(domain, params);
        }
    }

    /**
     * 根据某个字段查询
     *
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    @Override
    public List<T> findList(String propertyName, Object value) {
        QueryMap params = QueryMap.create().add(propertyName, value);
        if (params.size() == 0) return new ArrayList<>();
        Class<T> domain = getDomainClass();
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).findByProperties(domain, params);
        }
    }

    /**
     * 根据某个字段查询
     *
     * @param domain
     * @param propertyName 属性名称（对应数据库字段）
     * @param value        属性值（对应数据库字段值）
     * @return
     */
    @Override
    public <A> List<A> findList(Class<A> domain, String propertyName, Object value) {
        QueryMap params = QueryMap.create().add(propertyName, value);
        if (params.size() == 0) return new ArrayList<>();
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).findByProperties(domain, params);
        }
    }

    /**
     * 根据某个字段查询
     *
     * @param params 属性列表
     * @return
     */
    @Override
    public List<T> findList(Map<String, Object> params) {
        if(params == null || params.size() == 0) return new ArrayList<>();
        Class<T> domain = getDomainClass();
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).findByProperties(domain, params);
        }
    }

    /**
     * 根据某个字段查询
     *
     * @param domain
     * @param params 属性列表
     * @return
     */
    @Override
    public <A> List<A> findList(Class<A> domain, Map<String, Object> params) {
        if(params == null || params.size() == 0) return new ArrayList<>();
        try (Connection con = sql2o.open()) {
            return Sql2oCrudService.of(con).findByProperties(domain, params);
        }
    }

    /**
     * 获得结果集
     *
     * @param sql SQL语句
     * @return 结果集
     */
    @Override
    public List<T> findListBySql(String sql) {
        return this.findListBySql(this.getDomainClass(), sql, null);
    }

    /**
     * 获得结果集
     *
     * @param domain
     * @param queryInfo 查询配置
     * @return 结果集
     */
    @Override
    public <A> List<A> findListBySql(Class<A> domain, QueryInfo queryInfo) {
        return queryList(domain, queryInfo);
    }

    /**
     * 获得结果集包含统计总数
     *
     * @param sqlPage  SQL分页语句
     * @param sqlCount SQL统计语句
     * @return 结果集
     */
    @Override
    public Page<T> findPage(String sqlPage, String sqlCount, QueryInfo queryInfo) {
        return this.findPage(this.getDomainClass(), sqlPage, sqlCount, queryInfo);
    }

    /**
     * 列表查询
     *
     * @param query 查询条件集合
     * @return 列表查询结果集
     */
    public List<T> findList(QueryInfo query) {
        return queryList(getDomainClass(), query);
    }

    /**
     * 列表查询
     *
     * @param query 查询条件集合
     * @return 列表查询结果集
     */
    public List<Map> findMapList(QueryInfo query) {
        return queryList(Map.class, query);
    }

    /**
     * 分页查询
     *
     * @param query 查询条件集合
     * @return 分页结果集
     */
    @Override
    public Page<T> findPage(QueryInfo query) {
        return findPage(getDomainClass(), query);
    }

    /**
     * 分页查询
     *
     * @param query 查询条件集合
     * @return 分页结果集
     */
    @Override
    public Page<Map> findPageMap(QueryInfo query) {
        return findPage(Map.class, query);
    }

    /**
     * 获得结果集
     *
     * @param sql SQL语句
     * @return 结果集
     */
    @Override
    public List<Map> findMapListBySql(String sql) {
        return this.findListBySql(Map.class, sql, null);
    }

    /**
     * 获得结果集
     *
     * @param sql SQL语句
     * @return 结果集
     */
    @Override
    public List<Map> findMapListBySql(String sql, Map<String, Object> params) {
        return this.findListBySql(Map.class, sql, params);
    }

    /**
     * 获得结果集
     *
     * @param sql SQL语句
     * @return 结果集
     */
    @Override
    public Table findTableBySql(String sql) {
        return this.findTableBySql(sql, null);
    }

    /**
     * 获得结果集
     *
     * @param sql SQL语句
     * @return 结果集
     */
    @Override
    public Table findTableBySql(String sql, Map<String, Object> params) {
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setSql(sql);
        queryInfo.setQueryItems(params);

        return findTable(queryInfo);
    }

    /**
     * 获得结果集
     *
     * @param queryInfo 查询信息
     * @return 结果集
     */
    @Override
    public Table findTable(QueryInfo queryInfo) {
        try (Connection con = sql2o.open()) {
            Query q;
            if (queryInfo.getPagination()) {
                // set page args
                long skip = (queryInfo.getPageNumber() - 1) * queryInfo.getPageSize();
                long take = queryInfo.getPageSize();
                sql2o.getQuirks().addPageArgs(skip, take, queryInfo.getQueryItems());

                SQLParts parts = getSQLParts(queryInfo.getSql());
                q = createQuery(con, parts.sqlPage, queryInfo.getQueryItems());
            } else {
                q = createQuery(con, queryInfo.getSql(), queryInfo.getQueryItems());
            }
            q.setCaseSensitive(queryInfo.isCaseSensitive());
            q.setAutoDeriveColumnNames(queryInfo.isAutoDeriveColumnNames());
            Table table = q.executeAndFetchTable();
            return table;
        }
    }

    /**
     * 获得结果集
     *
     * @param sql    SQL语句
     * @param params 参数
     * @return 结果集
     */
    @Override
    public List<T> findListBySql(String sql, Map<String, Object> params) {
        return this.findListBySql(this.getDomainClass(), sql, params);
    }

    /**
     * 获得结果集
     *
     * @param sql    SQL语句
     * @param domain 返回的实体类型
     * @return 结果集
     */
    @Override
    public <A> List<A> findListBySql(Class<A> domain, String sql) {
        return this.findListBySql(domain, sql, null);
    }

    /**
     * 获得结果集
     *
     * @param sql    SQL语句
     * @param params 参数
     * @param domain 返回的实体类型
     * @return 结果集
     */
    public <A> List<A> findListBySql(Class<A> domain, String sql, Map<String, Object> params) {
        QueryInfo queryInfo = new QueryInfo();
        queryInfo.setSql(sql);
        queryInfo.setQueryItems(params);
        return queryList(domain, queryInfo);
    }

    /**
     * 获取一个返回值
     *
     * @param sql
     * @return
     */
    @Override
    public <A> A executeScalar(String sql, Map<String, Object> params, Class<A> returnType) throws Sql2oException {
        try (Connection con = sql2o.open()) {
            Query q = createQuery(con, sql, params);
            return q.executeScalar(returnType);
        }
    }

    @Override
    public <A> A executeScalar(String sql, Class<A> returnType) throws Sql2oException {
        try (Connection con = sql2o.open()) {
            Query q = con.createQuery(sql);
            return q.executeScalar(returnType);
        }
    }

    /**
     * 获取一个返回列表值
     *
     * @param sql sql语句
     * @return 执行结果列表
     */
    @Override
    public <A> List<A> executeScalarList(String sql, Map<String, Object> params, Class<A> returnType) throws Sql2oException {
        try (Connection con = sql2o.open()) {
            Query q = createQuery(con, sql, params);
            return q.executeScalarList(returnType);
        }
    }

    @Override
    public <A> List<A> executeScalarList(String sql, Class<A> returnType) throws Sql2oException {
        try (Connection con = sql2o.open()) {
            Query q = con.createQuery(sql);
            return q.executeScalarList(returnType);
        }
    }

    /**
     * 执行存储过程，获取一个返回值
     * 执行存储过程 首字段用于处理返回值 所以存储过程写法必须是 {?=call ProcedureName(?,?...,?)}
     *
     * @param sql        SQL语句
     * @param returnType 返回类型
     * @param params     参数
     * @return
     */
    @Override
    public <A> A executeCall(String sql, Class<A> returnType, Object... params) throws Sql2oException {
        try (Connection con = sql2o.open()) {
            CallableQuery q = con.createCallQuery(sql);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    q.addParameter(i + 2, params[i]);
                }
            }
            return q.executeScalar(returnType);
        }
    }

    /**
     * 执行存储过程 存储过程写法必须是 {call ProcedureName(?,?...,?)}
     *
     * @param sql    SQL语句
     * @param params 参数
     * @return
     */
    @Override
    public void executeCall(String sql, CallableParameters params) throws Sql2oException {
        try (Connection con = sql2o.open()) {
            CallableQuery q = con.createCallQuery(sql);
            if (params != null && params.size() > 0) {
                for (int i = 0; i < params.size(); i++) {
                    CallableParameter parameter = params.getParameter(i + 1);
                    q.addParameter(parameter);
                }
            }
            q.executeScalar();
        }
    }

    /**
     * 执行SQL语句
     *
     * @param sql SQL语句
     * @return 响应行数
     */
    @Transactional
    @Override
    public int executeSql(String sql) throws Sql2oException {
        try (Connection conn = sql2o.open()) {
            return conn.createQuery(sql).executeUpdate().getResult();
        }
    }

    /**
     * 执行一条SQL语句
     *
     * @param sql    SQL语句
     * @param params 参数
     * @return 响应结果数目
     */
    @Transactional
    @Override
    public int executeSql(String sql, Map<String, Object> params) throws Sql2oException {
        try (Connection con = sql2o.open()) {
            Query query = createQuery(con, sql, params);
            return query.executeUpdate().getResult();
        }
    }

    /**
     * 批量执行SQL语句
     *
     * @param sqlList
     * @param params
     * @return
     */
    @Transactional
    @Override
    public Object executeBatch(List<String> sqlList, Map<String, Object> params) {
        try (Connection conn = sql2o.open()) {
            for (int i = 0, len = sqlList.size(); i < len; i++) {
                String sql = sqlList.get(i);
                String sub6 = sql.substring(0, 6).toLowerCase();
                if (sub6.equals("insert") || sub6.equals("update")) {
                    Query query = createQuery(conn, sql, params);
                    query.executeUpdate();
                } else if (sub6.startsWith("set") || sub6.startsWith("alter")) {
                    Query query = conn.createQuery(sql);
                    query.executeUpdate();
                } else if (sub6.equals("select")) {
                    Query query = createQuery(conn, sql, params);
                    query.setCaseSensitive(true);
                    query.setAutoDeriveColumnNames(true);
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> list = query.executeAndFetchTable().asList();
                    return list;
                }
            }
        }
        return 0;
    }

    /**
     * 获取数据库当前时间
     *
     * @return 当前时间
     */
    @Override
    public Date now() {
        return sql2o.now();
    }

    /**
     * 查询集合
     *
     * @param propertyName   属性名
     * @param propertyValues 属性值列表
     * @return
     */
    @Override
    public List<T> findIn(String propertyName, Iterable<? extends Serializable> propertyValues) {
        Class<T> domain = getDomainClass();
        PojoData pojoData = PojoData.forClass(domain);
        TableInfo ti = pojoData.getTableInfo();
        Map<String, Object> params = new HashMap<>();
        int i = 0;
        for (Object value : propertyValues) {
            params.put(propertyName + (i++), value);
        }
        String queryText = String.format("SELECT T.* FROM %s T WHERE T.%s IN (%s)",
                ti.getTableName(), propertyName, StringUtils.collectionToDelimitedString(params.keySet(), ",", ":", ""));

        try (Connection con = sql2o.open()) {
            Query query = createQuery(con, queryText, params);
            return query.executeAndFetch(domain);
        }
    }

    /**
     * SQL 查询指定类型结果，支持 LinkedCaseInsensitiveMap
     *
     * @param domain 实体类型
     * @param query  查询配置
     * @return
     * @see LinkedCaseInsensitiveMap
     */
    private <A> A queryFirst(Class<A> domain, QueryInfo query) {
        try (Connection con = sql2o.open()) {
            Query q = createQuery(con, query.getSql(), query.getQueryItems());
            if (domain == Map.class) {
                q.setCaseSensitive(query.isCaseSensitive());
                q.setAutoDeriveColumnNames(query.isAutoDeriveColumnNames());
                @SuppressWarnings("unchecked")
                List<A> list = (List<A>) q.executeAndFetchTable().asList();
                if (list.size() > 0) {
                    return list.get(0);
                }
                return null;
            }
            return q.executeAndFetchFirst(domain);
        }
    }

    /**
     * SQL 查询指定类型结果，支持 LinkedCaseInsensitiveMap
     *
     * @param domain 实体类型
     * @param query  查询配置
     * @return
     * @see LinkedCaseInsensitiveMap
     */
    private <A> List<A> queryList(Class<A> domain, QueryInfo query) {
        try (Connection con = sql2o.open()) {
            Query q = createQuery(con, query.getSql(), query.getQueryItems());
            if (domain == Map.class) {
                q.setCaseSensitive(query.isCaseSensitive());
                q.setAutoDeriveColumnNames(query.isAutoDeriveColumnNames());
                @SuppressWarnings("unchecked")
                List<A> list = (List<A>) q.executeAndFetchTable().asList();
                return list;
            }
            return q.executeAndFetch(domain);
        }
    }

    private SQLParts getSQLParts(String sqlString) {
        Integer hashCode = sqlString.hashCode();
        //缓存功能
        SQLParts parts = pagingCacheMap.get(hashCode);
        if (parts == null) {
            // log
            long startMs = System.currentTimeMillis();

            // Split the SQL
            parts = sql2o.getQuirks().getPagingHelper().SplitSQL(sqlString);

            //分页模板语句
            parts.sqlPage = sql2o.getQuirks().buildPageQuery(parts);

            long endMs = System.currentTimeMillis();
            long takeMs = (endMs - startMs);
            logger.debug("切分耗时[{}ms]", takeMs);
            if (takeMs > 10) {
                logger.warn("切分耗时[{}ms] ：{}\n", takeMs, sqlString);
            }

            pagingCacheMap.put(hashCode, parts);
        }
        return parts;
    }

    /**
     * 分页查询
     *
     * @param query 查询条件集合
     * @return 分页结果集
     */
    public <A> Page<A> findPage(Class<A> domain, QueryInfo query) {
        SQLParts parts = getSQLParts(query.getSql());
        return findPage(domain, parts.sqlPage, parts.sqlCount, query);
    }

    /**
     * 获得结果集包含统计总数
     *
     * @param sqlPage  SQL分页语句
     * @param sqlCount SQL统计语句
     * @param domain
     * @return 结果集
     */
    @Override
    public <A> Page<A> findPage(Class<A> domain, String sqlPage, String sqlCount, QueryInfo query) {
        long total = 0;
        List<A> list;
        int pageOp = query.getPageOp();
        if (pageOp == 2 || pageOp == 3) {//count or page+count
            try (Connection conn = sql2o.open()) {
                Query qCount = createQuery(conn, sqlCount, query.getQueryItems());
                total = qCount.executeScalar(long.class);
            }
        } else if (pageOp == 1) {//page
            total = Integer.MAX_VALUE;//假定有21亿行
        }

        if (pageOp == 2) {//count
            return new PageImpl<>(new ArrayList<>());
        }

        if (total > 0) {
            // set page args
            long skip = (query.getPageNumber() - 1) * query.getPageSize();
            long take = query.getPageSize();
            sql2o.getQuirks().addPageArgs(skip, take, query.getQueryItems());

            // Get the records
            try (Connection con = sql2o.open()) {
                Query qPage = createQuery(con, sqlPage, query.getQueryItems());
                // set fetchSize
                Integer fetchSize = query.getPageSize();
                if (fetchSize > total) {
                    fetchSize = (int) total;
                }
                if (fetchSize > 100) {
                    fetchSize = 100;
                }
                qPage.setFetchSize(fetchSize);
                //qPage.setMaxSize(fetchSize);
                if (domain == Map.class) {
                    qPage.setCaseSensitive(query.isCaseSensitive());
                    qPage.setAutoDeriveColumnNames(query.isAutoDeriveColumnNames());
                    Table table = qPage.executeAndFetchTable();
                    //noinspection unchecked
                    list = (List<A>) table.asList();
                } else {
                    list = qPage.executeAndFetch(domain);
                }
            }
        } else {
            // Get the records
            list = new ArrayList<>();
        }

        // Done
        return new Sql2oPageImpl<>(list, new QueryInfo(query.getPageNumber() - 1, query.getPageSize()), total);
    }

    /**
     * 创建Query对象并设置参数
     *
     * @param con    连接对象
     * @param sql    sql2o查询对象
     * @param params 一个Map<String, Object>对象
     */
    private Query createQuery(Connection con, String sql, Map<String, Object> params) {
        Query query = null;
        if ((params != null) && !params.isEmpty()) {
            Object paras = params.get("_paras");
            if (paras instanceof List) {
                //noinspection unchecked
                List<SQLParameter> parameters = (List<SQLParameter>) params.get("_paras");
                if (parameters != null) {
                    if (params.containsKey(NoQuirks.PAGING_SKIP_KEY)) {
                        //mysql, limit :take offset :skip
                        boolean isOracle = sql2o.getQuirks() instanceof OracleQuirks;
                        if (isOracle) {
                            parameters.add(new SQLParameter(null, params.get(NoQuirks.PAGING_SKIP_KEY)));
                            parameters.add(new SQLParameter(null, params.get(NoQuirks.PAGING_TAKE_KEY)));
                        } else {//mysql
                            parameters.add(new SQLParameter(null, params.get(NoQuirks.PAGING_TAKE_KEY)));
                            parameters.add(new SQLParameter(null, params.get(NoQuirks.PAGING_SKIP_KEY)));
                        }
                        sql = sql.replace(":" + NoQuirks.PAGING_SKIP_KEY, "?").replace(":" + NoQuirks.PAGING_TAKE_KEY, "?");
                    }

                    int paramIdx = 1;
                    query = con.createQueryWithoutParseQuery(sql);
                    Map<String, List<Integer>> paramNameMap = query.getParamNameToIdxMap();
                    for (SQLParameter parameter : parameters) {
                        List<Integer> indices = new ArrayList<>();
                        indices.add(paramIdx);
                        String param = "p" + paramIdx;
                        paramNameMap.put(param, indices);
                        query.addParameter(param, parameter.value);

                        paramIdx++;
                    }
                    return query;
                }
            }
            query = con.createQuery(sql);
            Map<String, List<Integer>> paramNameMap = query.getParamNameToIdxMap();
            if (!paramNameMap.isEmpty()) {
                for (String key : paramNameMap.keySet()) {
                    query.addParameter(key, params.get(key));
                }
            }
        }
        return query != null ? query : con.createQuery(sql);
    }
    //endregion
}
