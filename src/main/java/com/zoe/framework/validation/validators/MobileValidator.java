package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * MobileValidator
 * Created by caizhicong on 2017/8/1.
 */
@Component(Validators.MOBILE)
public class MobileValidator extends PropertyValidator {

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        return validator.isMobile((String) context.getPropertyValue());
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "illegal-mobile";
    }
}
