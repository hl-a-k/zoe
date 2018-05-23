package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import com.zoe.framework.validation.exception.ArgumentOutOfRangeException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by z_wu on 2014/12/19.
 */
@Component(Validators.LENGTH)
public class LengthValidator extends PropertyValidator{

   public int min;
   public int max;

    public LengthValidator init(int min, int max){
        this.min = min;
        this.max = max;
        if(max < min)
            throw new ArgumentOutOfRangeException("'Max' should be larger than min.");
        return this;
    }

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        if(StringUtils.isEmpty(context.getPropertyValue())) return  true;
        int length = context.getPropertyValue() == null ? 0 : context.getPropertyValue().toString().length();
        return !(length < min || length > max);
    }

    @Override
    protected Object[] getFailureMessageArgs(PropertyValidatorContext context) {
        int length = context.getPropertyValue() == null ? 0 : context.getPropertyValue().toString().length();
        Object[] args = super.getFailureMessageArgs(context);
        return new Object[]{args[0], min, max, length};
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "out-of-length";
    }
}
