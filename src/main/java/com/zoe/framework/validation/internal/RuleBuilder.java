package com.zoe.framework.validation.internal;
import com.zoe.framework.validation.Action1;
import com.zoe.framework.validation.IValidator;
import com.zoe.framework.validation.exception.ArgumentNullException;
import com.zoe.framework.validation.validators.ChildValidatorAdaptor;
import com.zoe.framework.validation.validators.IPropertyValidator;

/**
 * Created by z_wu on 2014/12/9.
 */
public class RuleBuilder<T> extends IRuleBuilderOptions<T> {
    private final PropertyRule rule;

    private IPropertyValidator current;

    public IRuleBuilderOptions<T> setValidator(IPropertyValidator validator) {
        if(validator == null)
            throw  new ArgumentNullException("Cannot pass a null validator to setValidator.");
        this.current = validator;
        rule.addValidator(validator);
        return this;
    }

    @Override
    public IRuleBuilderOptions<T> setValidator(IValidator validator){
        if(validator == null)
            throw  new ArgumentNullException("Cannot pass a null validator to setValidator.");
        setValidator(new ChildValidatorAdaptor(validator));
        return this;
    }

    @Override
    public IRuleBuilderOptions<T> configure(Action1 configure) {
        configure.doAction(rule);
        return this;
    }

    public RuleBuilder(PropertyRule rule) {
        this.rule = rule;
    }

    public PropertyRule getRule(){
        return rule;
    }

    public IPropertyValidator getCurrent() {
        return current;
    }
}
