package com.zoe.framework.validation.validators;

import com.zoe.framework.validation.results.ValidationFailure;

import java.util.List;

/**
 * Created by z_wu on 2014/12/4.
 */
public interface IPropertyValidator {

   List<ValidationFailure> validate(PropertyValidatorContext context);
}
