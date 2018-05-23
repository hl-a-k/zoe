package com.zoe.framework.validation.validators;

import com.zoe.framework.validation.ValidationContext;
import com.zoe.framework.validation.internal.MessageFormatter;
import com.zoe.framework.validation.internal.PropertyRule;
import com.zoe.framework.util.CastUtils;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by z_wu on 2014/12/8.
 */
public class PropertyValidatorContext {
    private final MessageFormatter messageFormatter = new MessageFormatter();
    private boolean propertyValueSet;
    private Object propertyValue;
    public ValidationContext parentContext;
    public PropertyRule propertyRule;
    public String propertyName;
    private int count;
    public PropertyValidatorContext(ValidationContext parentContext, PropertyRule rule, String propertyName)
    {
        this.parentContext = parentContext;
        this.propertyRule = rule;
        this.propertyName = propertyName;
    }
    public String propertyDescription(){
        return propertyRule.getDisplayName();
    }

    public Object instance(){
        return parentContext.instanceToValidate();
    }

    public MessageFormatter messageFormatter(){
        return messageFormatter;
    }

    public Object getPropertyValue(){
        if(!propertyValueSet) {
            propertyValue = getPropertyValue(propertyName);
            propertyValueSet = true;
        }
        return propertyValue;
    }

    public Object getPropertyValue(String propertyName){
        Object propertyValue;
        Class<?> clazz = instance().getClass();
        if (Map.class.isAssignableFrom(clazz)) {
            propertyValue = getMapValue((Map<String,Object>)instance(), propertyName);
        } else {
            propertyValue = getValue(propertyName);
        }
        return propertyValue;
    }

    public <T> T getPropertyValue(Class<T> returnClass){
        Object propertyValue = getPropertyValue();
        return CastUtils.cast(propertyValue, returnClass);
    }

    public <T> T getPropertyValue(String propertyName, Class<T> returnClass){
        Object propertyValue = getPropertyValue(propertyName);
        return CastUtils.cast(propertyValue, returnClass);
    }

    public void setPropertyValue(Object propertyValue){
        this.propertyValueSet = true;
        this.propertyValue = propertyValue;
    }

    private Object getValue() {
        return getValue(propertyName);
    }

    /**
     * used to get propagated properties like "address.line1"
     * e.g.
     * TestValidator validator = new TestValidator();
     * validator.ruleFor(Person.class, "address.lin1").notNull();
     */
    private Object getValue(String propertyName) {
        Object value = null;
        String[] props = propertyName.split("\\.");
        Class<?> clazz = instance().getClass();
        Object instance = instance();
        boolean exist;
        while (props.length > 0 && count < props.length) {
            try {
                Field field = clazz.getDeclaredField(props[count]);
                field.setAccessible(true);
                value = field.get(instance);
                if(value != null) {
                    clazz = value.getClass();
                    instance = value;
                }
                exist = true;
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                exist = false;
            }
            if(!exist)
                break;
            count ++;
        }
        return value;
    }

    private Object getMapValue(Map<String, Object> map, String path){
        // TODO: field should support javascript expressions, eg: "user.name" or
        // "user.addresses[0].zipCode"
        // REVISIT: naive implementation for ".". pending implementation for
        // arrays.
        Object currentObject = null;
        String[] fields = path.split("\\.");
        for (int i = 0; i < fields.length; i++) {
            String currField = fields[i];
            currentObject = map.get(currField);
            if (i < fields.length-1)//not the last field in path. it should not be null and should be a map.
                if (currentObject != null && currentObject instanceof Map<?, ?>)
                    map = (Map<String, Object>) currentObject;
                else
                    throw new IllegalArgumentException(
                            String.format(
                                    "Unexpected non Json Object (map) while evaluating field %s in path : %s : Object value is : %s",
                                    currField, path, currentObject));
        }
        return currentObject;
    }
}
