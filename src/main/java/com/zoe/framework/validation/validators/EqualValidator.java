package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import com.zoe.framework.validation.util.Utils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Member;
import java.util.Comparator;

/**
 * Created by z_wu on 2014/12/17.
 * TODO: equal validator for property
 */
@Component(Validators.EQUAL)
public class EqualValidator extends PropertyValidator implements IComparisonValidator {
    private Member memberToCompare;
    private Object valueToCompare;
    private Comparator comparator;

    public EqualValidator init(Object valueToCompare, Comparator comparator){
        this.valueToCompare = valueToCompare;
        this.comparator = comparator;
        return this;
    }

    public EqualValidator init(Class<?> clazz,String property, Comparator comparator){
        this.memberToCompare = Utils.getMember(clazz, property);
        this.comparator = comparator;
        return this;
    }

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        Object comparisonValue = getComparisonValue(context);
        boolean result = compare(comparisonValue, context.getPropertyValue());
        if(!result){
            return false;
        }
        return true;
    }

    private Object getComparisonValue(PropertyValidatorContext context) {
        return  Utils.getComparisonValue(context, memberToCompare, valueToCompare);
    }

    @Override
    public Comparison getComparison() {
        return Comparison.equal;
    }

    @Override
    public Member getMemberToCompare() {
        return memberToCompare;
    }

    @Override
    public Object getValueToCompare() {
        return valueToCompare;
    }

    protected boolean compare(Object comparisonValue, Object propertyValue) {
        if(comparator != null) {
            return comparator.compare(comparisonValue, propertyValue) == 0;
        }
        return comparisonValue.equals(propertyValue);
    }

    @Override
    protected Object[] getFailureMessageArgs(PropertyValidatorContext context) {
        Object[] args = super.getFailureMessageArgs(context);
        return new Object[]{args[0], valueToCompare};
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "not-equal";
    }
}
