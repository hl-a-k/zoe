package com.zoe.framework.validation.internal;

import com.zoe.framework.validation.*;
import com.zoe.framework.validation.results.ValidationFailure;
import com.zoe.framework.validation.util.Utils;
import com.zoe.framework.validation.validators.DelegatingValidator;
import com.zoe.framework.validation.validators.IPropertyValidator;
import com.zoe.framework.validation.validators.PropertyValidatorContext;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by z_wu on 2014/12/8.
 * Defines a rule associated with a property.
 * TODO: property display name
 *       assume property display name same as the property name
 */
public class PropertyRule implements IValidationRule {
    private final List<IPropertyValidator> validators = new ArrayList<>();
    public Member member;
    private String ruleSet;
    public Class<?> clazz;
    public IPropertyValidator currentValidator;
    public String propertyName;
    private String displayName;

    public PropertyRule(Class<?> clazz, String propertyName,Member m, CascadeMode c){
        this.member = m;
        this.propertyName = propertyName;
        ValidatorOptions.cascadeMode = c;
        this.clazz = clazz;
    }

    public static PropertyRule create(Class<?> clazz, String propertyName, CascadeMode c){
        Member m = Utils.getMember(clazz, propertyName);
        return new PropertyRule(clazz, propertyName, m, c);
    }

    public void addValidator(IPropertyValidator validator) {
        currentValidator = validator;
        validators.add(validator);
    }

    public  void replaceValidator(IPropertyValidator orignal, IPropertyValidator newValidator){
        int index = validators.indexOf(orignal);
        if(index > -1){
            validators.set(index, newValidator);
            if(currentValidator.equals(orignal))
                currentValidator = newValidator;
        }
    }

    public String getDisplayName() {
        return displayName != null ? displayName : propertyName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public List<IPropertyValidator> validators() {
        return validators;
    }

    @Override
    public List<ValidationFailure> valiadate(ValidationContext context)  {
        ensureValidPropertyName();
        CascadeMode c = getCascadeMode();
        boolean hasAnyFailure = false;
        List<ValidationFailure> results = new ArrayList<>();
        for (IPropertyValidator validator : validators){
            if(!context.selector().canExecute(this, propertyName, context)){
                return new ArrayList<>();
            }
            results.addAll(invokePropertyValidator(context, validator, propertyName));
            boolean hasFailure;
            hasAnyFailure = hasFailure = results.size() > 0;
            if (c == CascadeMode.StopOnFirstFailure && hasFailure) {
                break;
            }
        }
        if(hasAnyFailure){
           //output the result without exception, callback can apply here if needed
           //throw new ValidationException(String.format("Validation to %s fails",propertyName));
        }
        return  results;
    }

    protected List<ValidationFailure> invokePropertyValidator(ValidationContext context, IPropertyValidator validator, String propertyName) {
        PropertyValidatorContext  propertyContext = new PropertyValidatorContext(context, this, propertyName);
        return validator.validate(propertyContext);
    }

    public CascadeMode getCascadeMode(){
        return ValidatorOptions.cascadeMode;
    }

    public void setCascadeMode(CascadeMode cascadeMode){
        ValidatorOptions.cascadeMode = cascadeMode;
    }

    private void ensureValidPropertyName() {
        if (propertyName == null) {
            throw new RuntimeException("Property name could not be null. Please specify either a custom property name by calling 'WithName'.");
        }
    }

    private String buildPropertyName(ValidationContext context) {
        return context.getPropertyChain().buildPropertyName(propertyName);
    }

    public void applyCondition(boolean predicate, ApplyConditionTo applyConditionTo){
        if(applyConditionTo == ApplyConditionTo.AllValidators){
            for(IPropertyValidator validator : validators){
                DelegatingValidator wrappedValidator = new DelegatingValidator(predicate, validator);
                replaceValidator(validator, wrappedValidator);
            }
        }else{
            DelegatingValidator wrappedValidator = new DelegatingValidator(predicate, currentValidator);
            replaceValidator(currentValidator, wrappedValidator);
        }
    }

    public void applyCondition(Function<PropertyValidatorContext,Boolean> predicate, ApplyConditionTo applyConditionTo){
        if(applyConditionTo == ApplyConditionTo.AllValidators){
            for(IPropertyValidator validator : validators){
                DelegatingValidator wrappedValidator = new DelegatingValidator(predicate, validator);
                replaceValidator(validator, wrappedValidator);
            }
        }else{
            DelegatingValidator wrappedValidator = new DelegatingValidator(predicate, currentValidator);
            replaceValidator(currentValidator, wrappedValidator);
        }
    }

    @Override
    public String getRuleSet() {
        return ruleSet;
    }

    public void setRuleSet(String ruleSet) {
        this.ruleSet = ruleSet;
    }
}
