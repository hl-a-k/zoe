package com.zoe.framework.validation.validators;

import com.zoe.framework.validation.util.Utils;
import com.zoe.framework.util.CastUtils;

import java.lang.reflect.Member;

/**
 * AbstractComparisonValidator
 * Created by z_wu on 2014/12/18.
 */
public abstract class AbstractComparisonValidator<TProperty extends Comparable<TProperty>>  extends PropertyValidator implements IComparisonValidator{

    public abstract boolean isValid(TProperty value, TProperty valueToCompare);

    private Member memberToCompare;
    private Object valueToCompare;
    private TProperty value;
    private Class<TProperty> clazz;

    protected AbstractComparisonValidator init(TProperty value){
        Utils.checkArgument(value, "Comparable value should not be null for AbstractComparisonValidator initialization");
        valueToCompare = value;
        //noinspection unchecked
        this.clazz = (Class<TProperty>) value.getClass();
        return this;
    }

    protected AbstractComparisonValidator init(Class<?> clazz, String property){
        Member member = Utils.getMember(clazz, property);
        this.memberToCompare = member;
        return this;
    }

    @Override
    protected final boolean isValid(PropertyValidatorContext context) {
        if (context.getPropertyValue() == null) {
            // If we're working with a nullable type then this rule should not be applied.
            // If you want to ensure that it's never null then a NotNull rule should also be applied.
            return true;
        }
        Object val = Utils.getComparisonValue(context, memberToCompare, valueToCompare);
        value = CastUtils.cast(val, clazz);
        TProperty propertyValue = context.getPropertyValue(clazz);
        return isValid(propertyValue, value);
    }

    @Override
    public Member getMemberToCompare() {
        return memberToCompare;
    }

    @Override
    public Object getValueToCompare() {
        return valueToCompare;
    }

    @Override
    protected Object[] getFailureMessageArgs(PropertyValidatorContext context) {
        Object[] args = super.getFailureMessageArgs(context);
        return new Object[]{args[0], value};
    }
}
