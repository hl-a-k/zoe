package com.zoe.framework.validation;

import com.zoe.framework.validation.internal.IRuleBuilderOptions;

/**
 * Created by z_wu on 2014/12/17.
 */
public interface InlineRuleCreator<T> {
    IRuleBuilderOptions inlineRuleCreator(InlineValidator<T> validator);
}
