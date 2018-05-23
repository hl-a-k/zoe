package com.zoe.framework.validation.internal;

import com.zoe.framework.validation.IValidator;
import com.zoe.framework.validation.validators.IPropertyValidator;

/**
 * Created by z_wu on 2014/12/9.
 */
public interface IRuleBuilder<T> {

    /**
     * Associates a validator with this the property for this rule builder.
     * @param validator
     * @return
     */
    IRuleBuilderOptions<T> setValidator(IPropertyValidator validator);

    /**
     * Associates an instance of IValidator with the current property rule.
     * @param validator
     * @return
     */
    IRuleBuilderOptions<T> setValidator(IValidator validator);
}
