package com.zoe.framework.sql2o.reflection;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.zoe.framework.sql2o.Sql2oException;
import com.zoe.framework.sql2o.data.PojoData;
import com.zoe.framework.sql2o.tools.AbstractCache;
import com.zoe.framework.sql2o.tools.CamelCaseUtils;

/**
 * Stores metadata for a POJO.
 */
public class PojoMetadata {

    private static final Cache caseSensitiveFalse = new Cache();
    private static final Cache caseSensitiveTrue = new Cache();
    private final PropertyAndFieldInfo propertyInfo;
    private final Map<String, String> columnMappings;
    private final FactoryFacade factoryFacade = FactoryFacade.getInstance();

    private boolean caseSensitive;
    private boolean autoDeriveColumnNames;
    public final boolean throwOnMappingFailure;
    private Class clazz;

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public boolean isAutoDeriveColumnNames() {
        return autoDeriveColumnNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PojoMetadata that = (PojoMetadata) o;

        return autoDeriveColumnNames == that.autoDeriveColumnNames && caseSensitive == that.caseSensitive && clazz.equals(that.clazz)
                && columnMappings.equals(that.columnMappings) && propertyInfo.equals(that.propertyInfo);

    }

    @Override
    public int hashCode() {
        int result = (caseSensitive ? 1 : 0);
        result = 31 * result + clazz.hashCode();
        return result;
    }

    public PojoMetadata(Class clazz) {
        this.caseSensitive = false;
        this.autoDeriveColumnNames = true;
        this.clazz = clazz;
        this.columnMappings = Collections.emptyMap();

        this.propertyInfo = getPropertyInfoThroughCache();
        this.throwOnMappingFailure = false;
    }

    public PojoMetadata(Class clazz, boolean caseSensitive, boolean autoDeriveColumnNames, Map<String, String> columnMappings, boolean throwOnMappingError) {
        this.caseSensitive = caseSensitive;
        this.autoDeriveColumnNames = autoDeriveColumnNames;
        this.clazz = clazz;
        this.columnMappings = columnMappings == null ? Collections.<String, String>emptyMap() : columnMappings;

        this.propertyInfo = getPropertyInfoThroughCache();
        this.throwOnMappingFailure = throwOnMappingError;
    }

    public ObjectConstructor getObjectConstructor() {
        return propertyInfo.objectConstructor;
    }

    private PropertyAndFieldInfo getPropertyInfoThroughCache() {
        return (caseSensitive ? caseSensitiveTrue : caseSensitiveFalse).get(clazz, this);
    }

    private PropertyAndFieldInfo initializePropertyInfo() {
        HashMap<String, IMember> propertyMembers = new HashMap<String, IMember>();
        HashMap<String, Field> fields = new HashMap<String, Field>();

        Class theClass = clazz;
        ObjectConstructor objectConstructor = factoryFacade.newConstructor(theClass);

        PojoData.forClass(theClass);
        Map<String, PojoProperty> propertyMap = PojoIntrospector.collectProperties(theClass);
        for (PojoProperty property : propertyMap.values()) {
            Field f = property.getField();
            String propertyName = property.columnName();
            if (propertyName == null) continue;
            propertyName = caseSensitive ? propertyName : propertyName.toLowerCase();
            propertyMembers.put(propertyName, factoryFacade.newMember(f));

            // prepare methods. Methods will override fields, if both exists.
            if (property.getGetMethod() != null || (property.getSetMethod() != null && property.getSetMethod().getParameterTypes().length == 1)) {
                propertyMembers.put(propertyName, factoryFacade.newMember(property.getGetMethod(), property.getSetMethod()));
            }

            fields.put(propertyName, f);
        }

        return new PropertyAndFieldInfo(propertyMembers, fields, objectConstructor);
    }

    public Map<String, String> getColumnMappings() {
        return columnMappings;
    }

    public IMember getPropertyMember(String propertyName) {
        IMember member = getPropertyMemberIfExists(propertyName);

        if (member != null) {
            return member;
        } else {
            String errorMsg = "Property with name '" + propertyName + "' not found on class " + this.clazz.toString();
            if (this.caseSensitive) {
                errorMsg += " (You have turned on case sensitive property search. Is this intentional?)";
            }
            throw new Sql2oException(errorMsg);
        }
    }

    public IMember getPropertyMemberIfExists(String propertyName) {
        String name = this.caseSensitive ? propertyName : propertyName.toLowerCase();

        if (this.columnMappings.containsKey(name)) {
            name = this.columnMappings.get(name);
        }

        if (autoDeriveColumnNames) {
            name = CamelCaseUtils.underscoreToCamelCase(name);
            if (!this.caseSensitive)
                name = name.toLowerCase();
        }

        return propertyInfo.propertyMembers.get(name);
    }

    public Class getType() {
        return this.clazz;
    }

    public Object getValueOfProperty(String propertyName, Object object) {
        IMember member = getPropertyMember(propertyName);

        return member.getProperty(object);
    }

    private static class Cache extends AbstractCache<Class, PropertyAndFieldInfo, PojoMetadata> {
        @Override
        protected PropertyAndFieldInfo evaluate(Class key, PojoMetadata param) {
            return param.initializePropertyInfo();
        }
    }

    private static class PropertyAndFieldInfo {
        // since this class is private we can just use field access
        // to make HotSpot a little less work for inlining
        public final Map<String, IMember> propertyMembers;
        public final Map<String, Field> fields;
        public final ObjectConstructor objectConstructor;

        private PropertyAndFieldInfo(Map<String, IMember> propertyMembers, Map<String, Field> fields,
                                     ObjectConstructor objectConstructor) {

            this.propertyMembers = propertyMembers;
            this.fields = fields;
            this.objectConstructor = objectConstructor;
        }
    }
}
