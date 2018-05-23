package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * NotEmptyValidator
 * Created by z_wu on 2014/12/11.
 */
@Component(Validators.NOT_EMPTY)
public class NotEmptyValidator extends PropertyValidator implements  IPropertyValidator{

    private Object defaultValueForType;

    public NotEmptyValidator init(Object defaultValueForType){
        this.defaultValueForType = defaultValueForType;
        return this;
    }

    @Override
    protected boolean ignoreNullOrEmpty(PropertyValidatorContext context) {
        return false;
    }

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        if(context.getPropertyValue() == null
                ||isInvalidString(context.getPropertyValue())
                ||isEmptyCollection(context.getPropertyValue())
                ||(context.getPropertyValue() == null || context.getPropertyValue().equals(defaultValueForType))) {
            return false;
        }
        return true;
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "empty";
    }

    boolean isInvalidString(Object value) {
        return value instanceof String && isNullOrWhitespace((String) value);
    }

    boolean isNullOrWhitespace(String value){
        if(value != null) {
            char[] chars = (value).toCharArray();
            for (char c : chars) {
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean isEmptyCollection(Object propertyValue){
        if(propertyValue == null) return false;
        if(propertyValue instanceof Iterable) {
            Iterable<?> collection = (Iterable) propertyValue;
            return !collection.iterator().hasNext();
        }
        return false;
    }
}