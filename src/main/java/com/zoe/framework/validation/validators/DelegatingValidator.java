package com.zoe.framework.validation.validators;

import com.zoe.framework.validation.results.ValidationFailure;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by z_wu on 2014/12/17.
 */
public class DelegatingValidator implements IPropertyValidator{
    private boolean condition;
    private Function<PropertyValidatorContext,Boolean> function;
    private String errorCode;
    private boolean supportsStandaloneValidation;

    private IPropertyValidator innerValidator;
    public DelegatingValidator(boolean condition, IPropertyValidator innerValidator) {
        this.condition = condition;
        this.innerValidator = innerValidator;
    }

    public DelegatingValidator(Function<PropertyValidatorContext,Boolean> condition, IPropertyValidator innerValidator) {
        this.function = condition;
        this.innerValidator = innerValidator;
    }

    @Override
    public List<ValidationFailure> validate(PropertyValidatorContext context) {
        if (this.function != null) {
            if (this.function.apply(context))
                return innerValidator.validate(context);
            return new ArrayList<>();
        }
        if (condition)
            return innerValidator.validate(context);
        return new ArrayList<>();
    }

    public IPropertyValidator getInnerValidator() {
        return innerValidator;
    }

    public void setInnerValidator(IPropertyValidator innerValidator) {
        this.innerValidator = innerValidator;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isSupportsStandaloneValidation() {
        return supportsStandaloneValidation;
    }

    public void setSupportsStandaloneValidation(boolean supportsStandaloneValidation) {
        this.supportsStandaloneValidation = supportsStandaloneValidation;
    }
}
