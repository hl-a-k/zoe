package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * Created by z_wu on 2014/12/18.
 */
@Component(Validators.GREATER_THAN)
public class GreaterThanValidator<TProperty extends Comparable<TProperty>> extends AbstractComparisonValidator<TProperty>{

    public GreaterThanValidator init(TProperty value){
        super.init(value);
        return this;
    }

    public GreaterThanValidator init(Class<?>  clazz, String property){
        super.init(clazz, property);
        return this;
    }

    @Override
    public boolean isValid(TProperty value, TProperty valueToCompare) {
        return  value.compareTo(valueToCompare) > 0;
    }

    @Override
    public Comparison getComparison() {
        return Comparison.greaterThan;
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "not-greater-than";
    }
}
