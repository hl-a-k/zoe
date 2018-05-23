package com.zoe.framework.data.jpa.domain.auditing;

import com.zoe.framework.data.jpa.domain.BaseEntity;
import com.zoe.framework.sql2o.util.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.auditing.CurrentDateTimeProvider;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.Map;

/**
 * BaseEntity AuditingHandler
 * Created by caizhicong on 2017/7/6.
 */
@Deprecated
public class BaseEntityAuditingHandler implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEntityAuditingHandler.class);
    private DateTimeProvider dateTimeProvider;
    private AuditorAware<String> auditorAware;
    private boolean dateTimeForNow;
    private boolean modifyOnCreation;

    public BaseEntityAuditingHandler() {
        this.dateTimeProvider = CurrentDateTimeProvider.INSTANCE;
        this.dateTimeForNow = true;
        this.modifyOnCreation = true;
    }

    public void setAuditorAware(AuditorAware<String> auditorAware) {
        Assert.notNull(auditorAware, "AuditorAware must not be null!");
        this.auditorAware = auditorAware;
    }

    public void setDateTimeForNow(boolean dateTimeForNow) {
        this.dateTimeForNow = dateTimeForNow;
    }

    public void setModifyOnCreation(boolean modifyOnCreation) {
        this.modifyOnCreation = modifyOnCreation;
    }

    public void setDateTimeProvider(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = (dateTimeProvider == null?CurrentDateTimeProvider.INSTANCE:dateTimeProvider);
    }

    public void markCreated(BaseEntity source) {
        this.touch(source, true);
    }

    public void markModified(BaseEntity source) {
        this.touch(source, false);
    }

    private void touch(BaseEntity target, boolean isNew) {
        //主键自动赋值
        if(isNew && target.isNew()) target.setId(Guid.newGuid());

        Object auditor = this.touchAuditor(target, isNew);
        Date now = this.dateTimeForNow?this.touchDate(target, isNew):null;
        Object defaultedNow = now == null?"not set":now;
        Object defaultedAuditor = auditor == null?"unknown":auditor;
        LOGGER.debug("Touched {} - Last modification at {} by {}", target, defaultedNow, defaultedAuditor);
    }

    private Object touchAuditor(BaseEntity wrapper, boolean isNew) {
        if (null == this.auditorAware) {
            return null;
        } else {
            String auditor = this.auditorAware.getCurrentAuditor();
            if (isNew) {
                if (wrapper.getValidFlag() == null) {
                    wrapper.setValidFlag(1);
                }
                wrapper.setCreateUser(auditor);
                if (!this.modifyOnCreation) {
                    return auditor;
                }
            }

            wrapper.setModifyUser(auditor);
            return auditor;
        }
    }

    private Date touchDate(BaseEntity wrapper, boolean isNew) {
        Date now = this.dateTimeProvider.getNow().getTime();
        if(isNew) {
            wrapper.setCreateTime(now);
            if(!this.modifyOnCreation) {
                return now;
            }
        }
        wrapper.setModifyTime(now);
        return now;
    }

    public void afterPropertiesSet() {
        if(this.auditorAware == null) {
            LOGGER.debug("No AuditorAware set! Auditing will not be applied!");
        }
    }

    public void markCreated(Map<String,Object> source) {
        this.touch(source, true);
    }

    public void markModified(Map<String,Object> source) {
        this.touch(source, false);
    }

    private void touch(Map<String,Object> target, boolean isNew) {
        //主键自动赋值
        if(isNew && target.get("id") == null) target.put("id", Guid.newGuid());

        Object auditor = this.touchAuditor(target, isNew);
        Date now = this.dateTimeForNow?this.touchDate(target, isNew):null;
        Object defaultedNow = now == null?"not set":now;
        Object defaultedAuditor = auditor == null?"unknown":auditor;
        LOGGER.debug("Touched {} - Last modification at {} by {}", target, defaultedNow, defaultedAuditor);
    }

    private Object touchAuditor(Map<String,Object> wrapper, boolean isNew) {
        if(null == this.auditorAware) {
            return null;
        } else {
            String auditor = this.auditorAware.getCurrentAuditor();
            if(isNew) {
                wrapper.putIfAbsent("validFlag", 1);
                wrapper.put("createUser", auditor);
                if(!this.modifyOnCreation) {
                    return auditor;
                }
            }

            wrapper.put("modifyUser", auditor);
            return auditor;
        }
    }

    private Date touchDate(Map<String,Object> wrapper, boolean isNew) {
        Date now = this.dateTimeProvider.getNow().getTime();
        if(isNew) {
            wrapper.put("createTime", now);
            if(!this.modifyOnCreation) {
                return now;
            }
        }
        wrapper.put("modifyTime", now);
        return now;
    }
}
