package com.zoe.framework.validation.internal;

import com.zoe.framework.validation.IValidationRule;
import com.zoe.framework.validation.ValidationContext;

/**
 * Determines whether or not a  rule should execute
 */
public interface IValidatorSelector {
    /**
     * Determines whether or not a rule should execute.
     * @param rule The rule
     * @param propertyPath Property path (eg Customer.Address.Line1)
     * @param context Contextual information
     * @return Whether or not the validator can execute.
     */
   public boolean canExecute(IValidationRule rule, String propertyPath, ValidationContext context);
}
