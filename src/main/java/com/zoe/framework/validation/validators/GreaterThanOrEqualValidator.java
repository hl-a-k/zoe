package com.zoe.framework.validation.validators;


import com.zoe.framework.validate.Validators;

/**
 * Created by z_wu on 2014/12/18.
 */
public class GreaterThanOrEqualValidator<TProperty extends Comparable<TProperty>> extends AbstractComparisonValidator<TProperty> {

    public GreaterThanOrEqualValidator init(TProperty value){
        super.init(value);
        return this;
    }

    public GreaterThanOrEqualValidator init(Class<?> clazz, String property){
        super.init(clazz, property);
        return this;
    }

    @Override
    public boolean isValid(TProperty value, TProperty valueToCompare) {
        return value.compareTo(valueToCompare) >= 0;
    }

    @Override
    public Comparison getComparison() {
        return Comparison.greaterThanOrEqual;
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "not-greater-than-or-equal";
    }
}
