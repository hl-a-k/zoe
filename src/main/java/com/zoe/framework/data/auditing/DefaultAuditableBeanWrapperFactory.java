package com.zoe.framework.data.auditing;

import com.zoe.framework.data.jpa.domain.AuditableEntity;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.auditing.AuditableBeanWrapper;
import org.springframework.data.auditing.AuditableBeanWrapperFactory;
import org.springframework.data.convert.Jsr310Converters;
import org.springframework.data.convert.ThreeTenBackPortConverters;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.data.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * A factory class to {@link AuditableBeanWrapper} instances.
 *
 * @author Oliver Gierke
 * @since 1.5
 */
class DefaultAuditableBeanWrapperFactory implements AuditableBeanWrapperFactory {

    /**
     * Returns an {@link AuditableBeanWrapper} if the given object is capable of being equipped with auditing information.
     *
     * @param source the auditing candidate.
     * @return
     */
    @SuppressWarnings("unchecked")
    public AuditableBeanWrapper getBeanWrapperFor(Object source) {

        if (source == null) {
            return null;
        }

        if (source instanceof AuditableEntity) {
            return new DefaultAuditableBeanWrapperFactory.AuditableInterfaceBeanWrapper((AuditableEntity<Object, ?>) source);
        }
        if(source instanceof Map){
            return new DefaultAuditableBeanWrapperFactory.AuditableMapBeanWrapper((Map<String,Object>) source);
        }

        AnnotationAuditingMetadata metadata = AnnotationAuditingMetadata.getMetadata(source.getClass());

        if (metadata.isAuditable()) {
            return new DefaultAuditableBeanWrapperFactory.ReflectionAuditingBeanWrapper(source);
        }

        return null;
    }

    /**
     * An {@link AuditableBeanWrapper} that works with objects implementing
     *
     * @author Oliver Gierke
     */
    static class AuditableMapBeanWrapper extends DefaultAuditableBeanWrapperFactory.DateConvertingAuditableBeanWrapper {

        private final Map<String,Object> auditable;

        public AuditableMapBeanWrapper(Map<String,Object> auditable) {
            this.auditable = auditable;
        }

        /*
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setCreatedBy(java.lang.Object)
         */
        public void setCreatedBy(Object value) {
            auditable.put("createUser", value);
        }

        /*
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setCreatedDate(org.joda.time.DateTime)
         */
        public void setCreatedDate(Calendar value) {
            auditable.put("createTime", value.getTime());
        }

        /*
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setLastModifiedBy(java.lang.Object)
         */
        public void setLastModifiedBy(Object value) {
            auditable.put("modifyUser", value);
        }

        /*
         * (non-Javadoc)
         * @see AuditableBeanWrapper#getLastModifiedDate()
         */
        @Override
        public Calendar getLastModifiedDate() {
            return getAsCalendar(auditable.get("modifyTime"));
        }

        /*
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setLastModifiedDate(org.joda.time.DateTime)
         */
        public void setLastModifiedDate(Calendar value) {
            auditable.put("modifyTime", value.getTime());
        }
    }

    /**
     * An {@link AuditableBeanWrapper} that works with objects implementing
     *
     * @author Oliver Gierke
     */
    static class AuditableInterfaceBeanWrapper extends DefaultAuditableBeanWrapperFactory.DateConvertingAuditableBeanWrapper {

        private final AuditableEntity<Object, ?> auditable;

        public AuditableInterfaceBeanWrapper(AuditableEntity<Object, ?> auditable) {
            this.auditable = auditable;
        }

        /* 
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setCreatedBy(java.lang.Object)
         */
        public void setCreatedBy(Object value) {
            auditable.setCreateUser(value);
        }

        /*
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setCreatedDate(org.joda.time.DateTime)
         */
        public void setCreatedDate(Calendar value) {
            auditable.setCreateTime(value.getTime());
        }

        /* 
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setLastModifiedBy(java.lang.Object)
         */
        public void setLastModifiedBy(Object value) {
            auditable.setModifyUser(value);
        }

        /* 
         * (non-Javadoc)
         * @see AuditableBeanWrapper#getLastModifiedDate()
         */
        @Override
        public Calendar getLastModifiedDate() {
            return getAsCalendar(auditable.getModifyTime());
        }

        /*
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setLastModifiedDate(org.joda.time.DateTime)
         */
        public void setLastModifiedDate(Calendar value) {
            auditable.setModifyTime(value.getTime());
        }
    }

    /**
     * Base class for {@link AuditableBeanWrapper} implementations that might need to convert {@link Calendar} values into
     * compatible types when setting date/time information.
     *
     * @author Oliver Gierke
     * @since 1.8
     */
    abstract static class DateConvertingAuditableBeanWrapper implements AuditableBeanWrapper {

        private static final boolean IS_JODA_TIME_PRESENT = ClassUtils.isPresent("org.joda.time.DateTime",
                DefaultAuditableBeanWrapperFactory.ReflectionAuditingBeanWrapper.class.getClassLoader());

        private final ConversionService conversionService;

        /**
         * Creates a new {@link DefaultAuditableBeanWrapperFactory.DateConvertingAuditableBeanWrapper}.
         */
        public DateConvertingAuditableBeanWrapper() {

            DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();

            if (IS_JODA_TIME_PRESENT) {
                conversionService.addConverter(DefaultAuditableBeanWrapperFactory.CalendarToDateTimeConverter.INSTANCE);
                conversionService.addConverter(DefaultAuditableBeanWrapperFactory.CalendarToLocalDateTimeConverter.INSTANCE);
            }

            for (Converter<?, ?> converter : Jsr310Converters.getConvertersToRegister()) {
                conversionService.addConverter(converter);
            }

            for (Converter<?, ?> converter : ThreeTenBackPortConverters.getConvertersToRegister()) {
                conversionService.addConverter(converter);
            }

            this.conversionService = conversionService;
        }

        /**
         * Returns the {@link Calendar} in a type, compatible to the given field.
         *
         * @param value can be {@literal null}.
         * @param targetType must not be {@literal null}.
         * @param source must not be {@literal null}.
         * @return
         */
        protected Object getDateValueToSet(Calendar value, Class<?> targetType, Object source) {

            if (value == null) {
                return null;
            }

            if (Calendar.class.equals(targetType)) {
                return value;
            }

            if (conversionService.canConvert(Calendar.class, targetType)) {
                return conversionService.convert(value, targetType);
            }

            if (conversionService.canConvert(Date.class, targetType)) {

                Date date = conversionService.convert(value, Date.class);
                return conversionService.convert(date, targetType);
            }

            throw new IllegalArgumentException(String.format("Invalid date type for member %s! Supported types are %s.",
                    source, AnnotationAuditingMetadata.SUPPORTED_DATE_TYPES));
        }

        /**
         * Returns the given object as {@link Calendar}.
         *
         * @param source can be {@literal null}.
         * @return
         */
        protected Calendar getAsCalendar(Object source) {

            if (source == null || source instanceof Calendar) {
                return (Calendar) source;
            }

            // Apply conversion to date if necessary and possible
            source = !(source instanceof Date) && conversionService.canConvert(source.getClass(), Date.class) ? conversionService
                    .convert(source, Date.class) : source;

            return conversionService.convert(source, Calendar.class);
        }
    }

    /**
     * An {@link AuditableBeanWrapper} implementation that sets values on the target object using refelction.
     *
     * @author Oliver Gierke
     */
    static class ReflectionAuditingBeanWrapper extends DefaultAuditableBeanWrapperFactory.DateConvertingAuditableBeanWrapper {

        private final AnnotationAuditingMetadata metadata;
        private final Object target;

        /**
         * Creates a new {@link DefaultAuditableBeanWrapperFactory.ReflectionAuditingBeanWrapper} to set auditing data on the given target object.
         *
         * @param target must not be {@literal null}.
         */
        public ReflectionAuditingBeanWrapper(Object target) {

            Assert.notNull(target, "Target object must not be null!");

            this.metadata = AnnotationAuditingMetadata.getMetadata(target.getClass());
            this.target = target;
        }

        /* 
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setCreatedBy(java.lang.Object)
         */
        public void setCreatedBy(Object value) {
            setField(metadata.getCreatedByField(), value);
        }

        /*
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setCreatedDate(java.util.Calendar)
         */
        public void setCreatedDate(Calendar value) {
            setDateField(metadata.getCreatedDateField(), value);
        }

        /* 
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setLastModifiedBy(java.lang.Object)
         */
        public void setLastModifiedBy(Object value) {
            setField(metadata.getLastModifiedByField(), value);
        }

        /* 
         * (non-Javadoc)
         * @see AuditableBeanWrapper#getLastModifiedDate()
         */
        @Override
        public Calendar getLastModifiedDate() {

            return getAsCalendar(org.springframework.util.ReflectionUtils.getField(metadata.getLastModifiedDateField(),
                    target));
        }

        /*
         * (non-Javadoc)
         * @see AuditableBeanWrapper#setLastModifiedDate(java.util.Calendar)
         */
        public void setLastModifiedDate(Calendar value) {
            setDateField(metadata.getLastModifiedDateField(), value);
        }

        /**
         * Sets the given field to the given value if the field is not {@literal null}.
         *
         * @param field
         * @param value
         */
        private void setField(Field field, Object value) {

            if (field != null) {
                ReflectionUtils.setField(field, target, value);
            }
        }

        /**
         * Sets the given field to the given value if the field is not {@literal null}.
         *
         * @param field
         * @param value
         */
        private void setDateField(Field field, Calendar value) {

            if (field == null) {
                return;
            }
            ReflectionUtils.setField(field, target, getDateValueToSet(value, field.getType(), field));
        }
    }

    private enum CalendarToDateTimeConverter implements Converter<Calendar, DateTime> {

        INSTANCE;

        @Override
        public DateTime convert(Calendar source) {
            return new DateTime(source);
        }
    }

    private enum CalendarToLocalDateTimeConverter implements Converter<Calendar, LocalDateTime> {

        INSTANCE;

        @Override
        public LocalDateTime convert(Calendar source) {
            return new LocalDateTime(source);
        }
    }
}
