package com.zoe.framework.validation.validators;

import com.zoe.framework.validation.Predicate1;
import com.zoe.framework.validation.internal.IRuleBuilderOptions;

/**
 * Created by z_wu on 2015/1/4.
 */
public abstract class ICollectionValidatorRuleBuilder extends IRuleBuilderOptions {

   public abstract ICollectionValidatorRuleBuilder where(Predicate1 predicate);
}
