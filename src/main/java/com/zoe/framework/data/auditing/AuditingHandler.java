package com.zoe.framework.data.auditing;

import com.zoe.framework.data.jpa.domain.AuditableEntity;
import com.zoe.framework.data.jpa.domain.Tenantable;
import com.zoe.framework.data.jpa.domain.ValidableEntity;
import com.zoe.framework.data.multitenancy.TenantContext;
import com.zoe.framework.sql2o.util.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.auditing.*;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.Persistable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

/**
 * Auditing handler to mark entity objects created and modified.
 *
 * @author Oliver Gierke
 * @since 1.5
 */
public class AuditingHandler implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditingHandler.class);

    private final DefaultAuditableBeanWrapperFactory factory;

    private DateTimeProvider dateTimeProvider = CurrentDateTimeProvider.INSTANCE;
    private AuditorAware<?> auditorAware;
    private boolean dateTimeForNow = true;
    private boolean modifyOnCreation = true;
    
    /**
     * Creates a new {@link AuditableBeanWrapper}
     * metadata via reflection.
     * @since 1.10
     */
    public AuditingHandler() {
        this.factory = new DefaultAuditableBeanWrapperFactory();
    }

    /**
     * Setter to inject a {@code AuditorAware} component to retrieve the current auditor.
     *
     * @param auditorAware must not be {@literal null}.
     */
    public void setAuditorAware(final AuditorAware<?> auditorAware) {

        Assert.notNull(auditorAware, "AuditorAware must not be null!");
        this.auditorAware = auditorAware;
    }

    /**
     * Setter do determine if {@link AuditableEntity#setCreateTime(Date)}  and
     * {@link AuditableEntity#setModifyTime(Date)} shall be filled with the current Java time. Defaults to
     * {@code true}. One might set this to {@code false} to use database features to set entity time.
     *
     * @param dateTimeForNow the dateTimeForNow to set
     */
    public void setDateTimeForNow(boolean dateTimeForNow) {
        this.dateTimeForNow = dateTimeForNow;
    }

    /**
     * Set this to false if you want to treat entity creation as modification and thus set the current date as
     * modification date, too. Defaults to {@code true}.
     *
     * @param modifyOnCreation if modification information shall be set on creation, too
     */
    public void setModifyOnCreation(boolean modifyOnCreation) {
        this.modifyOnCreation = modifyOnCreation;
    }

    /**
     * Sets the {@link DateTimeProvider} to be used to determine the dates to be set.
     *
     * @param dateTimeProvider
     */
    public void setDateTimeProvider(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider == null ? CurrentDateTimeProvider.INSTANCE : dateTimeProvider;
    }

    /**
     * Marks the given object as created.
     *
     * @param source
     */
    public void markCreated(Object source) {
        touch(source, true);
    }

    /**
     * Marks the given object as modified.
     *
     * @param source
     */
    public void markModified(Object source) {
        touch(source, false);
    }

    /**
     * Returns whether the given source is considered to be auditable in the first place
     *
     * @param source can be {@literal null}.
     * @return
     */
    protected final boolean isAuditable(Object source) {
        return factory.getBeanWrapperFor(source) != null;
    }

    private void touch(Object target, boolean isNew) {
        checkOnCreate(target, isNew);

        AuditableBeanWrapper wrapper = factory.getBeanWrapperFor(target);

        if (wrapper == null) {
            return;
        }

        Object auditor = touchAuditor(wrapper, isNew);
        Calendar now = dateTimeForNow ? touchDate(wrapper, isNew) : null;

        Object defaultedNow = now == null ? "not set" : now;
        Object defaultedAuditor = auditor == null ? "unknown" : auditor;

        LOGGER.debug("Touched {} - Last modification at {} by {}", target, defaultedNow, defaultedAuditor);
    }

    /**
     * 创建记录是要执行的检查
     * @param target
     */
    private void checkOnCreate(Object target, boolean isNew){
        if(!isNew) return;
        if(target instanceof Persistable){
            //主键自动赋值
            Persistable entity = (Persistable)target;
            if(entity.isNew()) {
                AnnotationAuditingMetadata metadata = AnnotationAuditingMetadata.getMetadata(target.getClass());
                Field field = metadata.getIdField();
                if (field != null && field.getType() == String.class) {
                    ReflectionUtils.makeAccessible(field);
                    ReflectionUtils.setField(field, target, Guid.newGuid());
                }
            }
        }
        if(target instanceof ValidableEntity){
            //设置有效标志
            ValidableEntity entity = (ValidableEntity)target;
            if(entity.getValidFlag() == null){
                entity.setValidFlag(ValidableEntity.VALID);
            }
        }
        if(target instanceof Tenantable){
            //设置租户ID
            Tenantable entity = (Tenantable) target;
            if (entity.getTenantId() == null) {
                String tenantId = TenantContext.getTenant();
                if (tenantId != null) {
                    entity.setTenantId(TenantContext.getTenant());
                }
            }
        }
    }

    /**
     * Sets modifying and creating auditioner. Creating auditioner is only set on new auditables.
     *
     * @param wrapper
     * @return
     */
    private Object touchAuditor(AuditableBeanWrapper wrapper, boolean isNew) {

        if (null == auditorAware) {
            return null;
        }

        Object auditor = auditorAware.getCurrentAuditor();

        if (isNew) {
            wrapper.setCreatedBy(auditor);
            if (!modifyOnCreation) {
                return auditor;
            }
        }

        wrapper.setLastModifiedBy(auditor);
        return auditor;
    }

    /**
     * Touches the auditable regarding modification and creation date. Creation date is only set on new auditables.
     *
     * @param wrapper
     * @return
     */
    private Calendar touchDate(AuditableBeanWrapper wrapper, boolean isNew) {

        Calendar now = dateTimeProvider.getNow();

        if (isNew) {
            wrapper.setCreatedDate(now);
            if (!modifyOnCreation) {
                return now;
            }
        }

        wrapper.setLastModifiedDate(now);
        return now;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() {

        if (auditorAware == null) {
            LOGGER.debug("No AuditorAware set! Auditing will not be applied!");
        }
    }
}
