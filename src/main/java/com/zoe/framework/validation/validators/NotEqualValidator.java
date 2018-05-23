package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import com.zoe.framework.validation.util.Utils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Member;
import java.util.Comparator;

/**
 * Created by z_wu on 2014/12/19.
 */
@Component(Validators.NOT_EQUAL)
public class NotEqualValidator extends PropertyValidator implements IComparisonValidator {
    private Comparator comparator ;
    private Member memberToCompare;
    private Object valueToCompare;

    public NotEqualValidator init(Class<?> clazz, String property){
        this.memberToCompare = Utils.getMember(clazz, property);
        return this;
    }

    public NotEqualValidator init(Class<?> clazz, String property, Comparator comparator){
        this.memberToCompare = Utils.getMember(clazz, property);
        this.comparator = comparator;
        return this;
    }
    public NotEqualValidator init(Object comparisonValue){
        this.valueToCompare = comparisonValue;
        return this;
    }

    public NotEqualValidator init(Object comparisonValue, Comparator comparator){
        this.valueToCompare = comparisonValue;
        this.comparator = comparator;
        return this;
    }

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        Object  comparisonValue = Utils.getComparisonValue(context, memberToCompare, valueToCompare);
        boolean result = !compare(comparisonValue, context.getPropertyValue());
        return result;
    }

    @Override
    public Comparison getComparison() {
        return Comparison.notEqual;
    }

    protected boolean compare(Object comparisonValue, Object propertyValue){
        if(comparator != null){
            return comparator.compare(comparisonValue, propertyValue) == 0;
        }
        return comparisonValue.equals(propertyValue);
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
        return new Object[]{args[0], valueToCompare};
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "equal";
    }
}
