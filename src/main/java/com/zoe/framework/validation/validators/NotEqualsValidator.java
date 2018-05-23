package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * Created by caizhicong on 2017/8/1.
 */
@Component(Validators.NOT_EQUALS)
public class NotEqualsValidator extends PropertyValidator {

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        String[] names = converter.toArray(context.propertyName, ",");
        return !context.getPropertyValue(names[0]).equals(context.getPropertyValue(names[1]));
    }

    @Override
    protected Object[] getFailureMessageArgs(PropertyValidatorContext context) {
        String[] names = converter.toArray(context.propertyName, ",");
        return new Object[]{message.get(names[0]), message.get(names[1])};
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "equals";
    }
}
