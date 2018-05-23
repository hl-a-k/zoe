package com.zoe.framework.validation;

import com.zoe.framework.validation.internal.IRuleBuilderOptions;
import com.zoe.framework.validation.internal.PropertyRule;
import com.zoe.framework.validation.internal.RuleBuilder;

import java.util.Map;

/**
 * MapValidator
 * Created by caizhicong on 2017/7/31.
 */
public class MapValidator extends AbstractValidator<Map>{

    public MapValidator() {
        super(Map.class);
    }

    private String propertyPrefix = null;

    public void setPropertyPrefix(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix;
    }

    public final IRuleBuilderOptions<Map> ruleFor(String propertyName){
        return ruleFor(this.propertyPrefix, propertyName);
    }

    public final IRuleBuilderOptions<Map> ruleFor(String propertyPrefix, String propertyName){
        //Utils.checkArgument(propertyName, "Property name should not be null for ruleFor");
        PropertyRule rule = PropertyRule.create(Map.class, propertyName, getCascadeMode());
        rule.setDisplayName( propertyPrefix != null ? propertyPrefix + "." + propertyName : propertyName);
        addRule(rule);
        RuleBuilder<Map> ruleBuilder = new RuleBuilder<>(rule);
        return ruleBuilder;
    }
}
