package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * Created by z_wu on 2014/12/19.
 */
@Component(Validators.ID)
public class IdValidator extends PropertyValidator {

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        String value = (String)context.getPropertyValue();
        return validator.isMatchRegex("^[a-fA-F0-9]{32}$", value) ||
                validator.isMatchRegex("^[a-fA-F0-9]{8}(-[a-fA-F0-9]{4}){3}-[a-fA-F0-9]{12}$", value);
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "illegal-id";
    }
}
