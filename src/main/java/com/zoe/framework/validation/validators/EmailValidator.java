package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * Created by z_wu on 2014/12/19.
 */
@Component(Validators.EMAIL)
public class EmailValidator extends PropertyValidator {

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        return validator.isEmail((String) context.getPropertyValue());
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "illegal-email";
    }
}
