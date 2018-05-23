package com.zoe.framework.validate;

import com.alibaba.fastjson.JSONObject;
import com.zoe.framework.OpResult;
import com.zoe.framework.util.RequestUtil;
import com.zoe.framework.validation.CascadeMode;
import com.zoe.framework.validation.IValidator;
import com.zoe.framework.validation.MapValidator;
import com.zoe.framework.validation.internal.IRuleBuilderOptions;
import com.zoe.framework.validation.results.ValidationFailure;
import com.zoe.framework.validation.results.ValidationResult;
import com.zoe.framework.validation.validators.IPropertyValidator;
import com.zoe.framework.validation.validators.PropertyValidator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lpw
 */
@Component
public class ValidatorsImpl implements Validators {

    private Map<String, List<MapValidator>> validators = new ConcurrentHashMap<>();

    private final BeanFactory beanFactory;

    @Autowired
    public ValidatorsImpl(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object validate(ValidateWrapper2 wrapper2) {
        HttpServletRequest request = wrapper2.getRequest();
        String cacheKey = request.getMethod() + request.getRequestURI();
        List<MapValidator> validatorList = validators.computeIfAbsent(cacheKey, v -> {
            List<MapValidator> validators2 = new ArrayList<>();
            if (wrapper2.getCurrent().validates().length > 0) {
                MapValidator mapValidator = new MapValidator();
                mapValidator.setCascadeMode(CascadeMode.StopOnFirstFailure);
                for (Validate validate : wrapper2.getCurrent().validates()) {
                    IRuleBuilderOptions<Map> ruleBuilderOptions = mapValidator.ruleFor(Map.class, validate.parameter());
                    IPropertyValidator validator = createPropertyValidator(validate, ruleBuilderOptions);
                    if (validator != null) ruleBuilderOptions.setValidator(validator);
                }
                validators2.add(mapValidator);
            }
            Set<Class<? extends IValidator>> validators = new HashSet<>();
            Collections.addAll(validators, wrapper2.getCurrent().validators());
            if (wrapper2.getParent() != null) {
                Collections.addAll(validators, wrapper2.getParent().validators());
            }
            if (validators.size() > 0) {
                for (Class<? extends IValidator> validatorClass : validators) {
                    MapValidator validator = (MapValidator) beanFactory.getBean(validatorClass, IValidator.class);
                    if (validator == null) continue;
                    validator.setCascadeMode(CascadeMode.StopOnFirstFailure);
                    validators2.add(validator);
                }
            }
            return validators2;
        });

        if (validatorList.size() == 0) return null;

        Map<String, Object> map = RequestUtil.of(wrapper2.getRequest()).getQueryItems();
        for (int i = 0; i < validatorList.size(); i++) {
            MapValidator validator = validatorList.get(i);
            String[] ruleSets = wrapper2.getCurrent().groups().length == 0 ? null : wrapper2.getCurrent().groups();
            ValidationResult validationResult = validator.validate(map, null, ruleSets);
            if (!validationResult.isValid()) {
                ValidationFailure failure = validationResult.errors().get(0);
                Integer errorCode = failure.getErrorCode() != null ? failure.getErrorCode() : wrapper2.getErrorCode(failure.propertyName());
                OpResult result = OpResult.failedResult();
                if (errorCode == null || errorCode < 100) errorCode = Failure.ValidateError.getErrorCode();
                result.code(errorCode).msg(failure.errorMessage()).put("parameter", new JSONObject()
                        .fluentPut("name", failure.propertyName())
                        .fluentPut("value", failure.attemptedValue()));
                return result;
            }
        }
        return null;
    }

    private IPropertyValidator createPropertyValidator(Validate validate, IRuleBuilderOptions<Map> ruleBuilderOptions) {
        switch (validate.validator()) {
            case Validators.BETWEEN:
                if (validate.number().length >= 2) {
                    ruleBuilderOptions.between(validate.number()[0], validate.number()[1]);
                }
                return null;
            case Validators.DATE_TIME:
                ruleBuilderOptions.isDate();
                return null;
            case Validators.EMAIL:
                ruleBuilderOptions.isEmail();
                return null;
            case Validators.ID:
                ruleBuilderOptions.isId();
                return null;
            case Validators.EQUAL:
                if (validate.number().length >= 1) {
                    ruleBuilderOptions.equal(validate.number()[0]);
                }
                return null;
            case Validators.NOT_EQUAL:
                if (validate.number().length >= 1) {
                    ruleBuilderOptions.notEqual(validate.number()[0]);
                }
                return null;
            case Validators.GREATER_THAN:
                if (validate.number().length >= 1) {
                    ruleBuilderOptions.greaterThan(validate.number()[0]);
                }
                return null;
            case Validators.LESS_THAN:
                if (validate.number().length >= 1) {
                    ruleBuilderOptions.lessThan(validate.number()[0]);
                }
                return null;
            case Validators.LENGTH:
                if (validate.number().length == 1) {
                    ruleBuilderOptions.length(validate.number()[0]);
                } else if (validate.number().length >= 2) {
                    ruleBuilderOptions.length(validate.number()[0], validate.number()[1]);
                }
                return null;
            case Validators.MAX_LENGTH:
                if (validate.number().length >= 1) {
                    ruleBuilderOptions.maxLength(validate.number()[0]);
                }
                return null;
            case Validators.MATCH_REGEX:
                if (validate.string().length >= 1) {
                    ruleBuilderOptions.matches(validate.string()[0]);
                }
                return null;
        }

        PropertyValidator validator = beanFactory.getBean(validate.validator(), PropertyValidator.class);
        if (validator == null)
            throw new NullPointerException("验证器[" + validate.validator() + "]不存在！");
        validator.setFailureCode(validate.failureCode());
        validator.setErrorMessageKey(validate.failureKey());
        return validator;
    }
}