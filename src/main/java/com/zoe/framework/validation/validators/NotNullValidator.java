package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * Created by z_wu on 2014/12/11.
 */
@Component(Validators.NOT_NULL)
public class NotNullValidator extends PropertyValidator implements IPropertyValidator{

    @Override
    protected boolean ignoreNullOrEmpty(PropertyValidatorContext context) {
        return false;
    }

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        return context.getPropertyValue() != null;
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "null";
    }
}
