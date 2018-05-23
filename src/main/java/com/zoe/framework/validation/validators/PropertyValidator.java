package com.zoe.framework.validation.validators;


import com.zoe.framework.i18n.MessageService;
import com.zoe.framework.util.Converter;
import com.zoe.framework.validation.results.ValidationFailure;
import com.zoe.framework.context.SpringContextHolder;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by z_wu on 2014/12/11.
 * //TODO:to deal with custom format
 */
public abstract class PropertyValidator implements IPropertyValidator {

    private String errorMessageKey;
    private String[] errorArgKeys;

    @Autowired
    protected MessageService message;

    @Autowired
    protected Converter converter;

    @Autowired
    protected com.zoe.framework.util.Validator validator;

    private boolean inited = false;

    public void init() {
        if (inited) return;
        if (message == null) message = SpringContextHolder.getBean(MessageService.class);
        if (converter == null) converter = SpringContextHolder.getBean(Converter.class);
        if (validator == null) validator = SpringContextHolder.getBean(com.zoe.framework.util.Validator.class);
        inited = true;
    }

    protected abstract boolean isValid(PropertyValidatorContext context);

    protected boolean ignoreNullOrEmpty(PropertyValidatorContext context) {
        return StringUtils.isEmpty(context.getPropertyValue());
    }

    @Override
    public List<ValidationFailure> validate(PropertyValidatorContext context) {
        List<ValidationFailure> failures = new ArrayList<>();
        //context.messageFormatter().appendPropertyName(context.propertyDescription());
        if (ignoreNullOrEmpty(context)) {
            return failures;
        }
        this.init();
        if (!isValid(context)) {
            failures.add(createValidationError(context));
        }
        return failures;
    }

    public ValidationFailure createValidationError(PropertyValidatorContext context) {
        //context.messageFormatter().appendAdditionalArguments(customFormatArgs.toArray());
        //String error = context.messageFormatter().buildMessage(getDefaultFailureMessageKey());
        String error = getFailureMessage(context);
        Integer code = getFailureCode();
        ValidationFailure failure = new ValidationFailure(context.propertyName, error, context.getPropertyValue(), code);
        return failure;
    }

    public String getErrorMessageKey() {
        return errorMessageKey;
    }

    public void setErrorMessageKey(String errorMessageKey) {
        this.errorMessageKey = errorMessageKey;
    }

    public String[] getErrorArgKeys() {
        return errorArgKeys;
    }

    public void setErrorArgKeys(String[] errorArgKeys) {
        this.errorArgKeys = errorArgKeys;
    }

    private int failureCode = 0;

    public int getFailureCode() {
        return failureCode;
    }

    public void setFailureCode(int failureCode) {
        this.failureCode = failureCode;
    }

    public String getFailureMessage(PropertyValidatorContext context) {
        return message.get(StringUtils.isEmpty(errorMessageKey) ? getDefaultFailureMessageKey() : errorMessageKey,
                getFailureMessageArgs(context));
    }

    protected Object[] getFailureMessageArgs(PropertyValidatorContext context) {
        //String key = validate.getParentValidate() != null ? validate.getParentValidate().key() : "";
        //key += "." + validate.parameter();
        String key = context.propertyDescription();
        if (ArrayUtils.isEmpty(errorArgKeys))
            return new Object[]{message.get(key)};

        Object[] args = new Object[errorArgKeys.length];
        for (int i = 0; i < args.length; i++)
            args[i] = message.get(errorArgKeys[i]);

        return args;
    }

    protected String getDefaultFailureMessageKey() { return null; }
}
