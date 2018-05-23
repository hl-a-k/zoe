package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * Created by z_wu on 2014/12/19.
 */
@Component(Validators.EXACT_LENGTH)
public class ExactLengthValidator extends LengthValidator {

    public ExactLengthValidator init(int length){
        init(length, length);
        return this;
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "not-length";
    }
}
