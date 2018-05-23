package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * Created by caizhicong on 2017/8/1.
 */
@Component(Validators.DATE_TIME)
public class DateValidator extends PropertyValidator {

    private String format;

    public DateValidator(){
    }

    public DateValidator(String format){
        this.format = format;
    }

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        if (format != null) {
            return converter.toDate((String) context.getPropertyValue(), format) != null;
        }
        return converter.toDate(context.getPropertyValue()) != null;
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "illegal-date-time";
    }
}
