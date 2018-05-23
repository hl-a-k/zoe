package com.zoe.framework.validation.validators;

import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * Created by caizhicong on 2017/08/25.
 */
@Component(Validators.MATCH_REGEX)
public class MatchRegexValidator extends PropertyValidator{

    private String[] expressions;

    public MatchRegexValidator init(String... expressions){
        this.expressions = expressions;
        return this;
    }

    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        String value = (String) context.getPropertyValue();
        boolean result = false;
        for (String expression : expressions) {
            result |= validator.isMatchRegex(expression, value);
        }
        return result;
    }

    @Override
    protected Object[] getFailureMessageArgs(PropertyValidatorContext context) {
        Object[] args = super.getFailureMessageArgs(context);
        return new Object[]{args[0], expressions};
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "not-match-regex";
    }
}
