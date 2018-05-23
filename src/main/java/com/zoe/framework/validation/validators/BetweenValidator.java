package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import com.zoe.framework.validation.exception.ArgumentOutOfRangeException;
import org.springframework.stereotype.Component;

/**
 * BetweenValidator
 * Created by z_wu on 2014/12/18.
 */
@Component(Validators.BETWEEN)
public class BetweenValidator<TProperty extends Comparable<TProperty>> extends PropertyValidator {

    private TProperty from;
    private TProperty to;
    private Class<TProperty> clazz;

    public BetweenValidator<?> init(TProperty from,TProperty to){
        this.from = from;
        this.to = to;
        if(to.compareTo(from) == -1){
            throw new ArgumentOutOfRangeException("Argument 'to' should be larger than from.");
        }
        //noinspection unchecked
        this.clazz = (Class<TProperty>) from.getClass();
        return this;
    }

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        TProperty propertyValue =  context.getPropertyValue(clazz);
        return propertyValue == null || !(propertyValue.compareTo(from) < 0 || propertyValue.compareTo(to) > 0);
    }

    @Override
    protected Object[] getFailureMessageArgs(PropertyValidatorContext context) {
        Object[] args = super.getFailureMessageArgs(context);
        return new Object[]{args[0], from, to, context.getPropertyValue()};
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "not-between";
    }
}
