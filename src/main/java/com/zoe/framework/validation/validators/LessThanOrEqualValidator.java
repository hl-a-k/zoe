package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;

/**
 * Created by z_wu on 2014/12/18.
 */
public class LessThanOrEqualValidator<TProperty extends Comparable<TProperty>> extends AbstractComparisonValidator<TProperty> {

    public LessThanOrEqualValidator init(TProperty value){
        super.init(value);
        return this;
    }

    public LessThanOrEqualValidator init(Class<?>  clazz, String property){
        super.init(clazz, property);
        return this;
    }

    @Override
    public boolean isValid(TProperty value, TProperty valueToCompare) {
        return value.compareTo(valueToCompare) <= 0;
    }

    @Override
    public Comparison getComparison() {
        return Comparison.lessThanOrEqual;
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "not-less-than-or-equal";
    }
}
