package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * Created by z_wu on 2014/12/19.
 */
@Component(Validators.MAX_LENGTH)
public class MaxLengthValidator extends LengthValidator {

    public MaxLengthValidator init(int max){
        super.init(0, max);
        return this;
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "over-max-length";
    }
}
