package com.zoe.framework.validation.internal;

import com.zoe.framework.validation.*;
import com.zoe.framework.validation.util.Utils;
import com.zoe.framework.validation.validators.*;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Created by z_wu on 2014/12/9.
 */
public abstract class IRuleBuilderOptions<T> implements IRuleBuilder<T>, IConfigurable<PropertyRule, IRuleBuilderOptions<T>> {
    public IRuleBuilderOptions(){
    }

    /**
     * Defines a 'not empty' validator on the current rule builder.
     * @return
     */
    public IRuleBuilderOptions notEmpty() {
        String propertyName = ((RuleBuilder)this).getRule().propertyName;
        Class<?> currentClazz = ((RuleBuilder)this).getRule().clazz;
        Object value = Utils.getDefaultValueForProperty(currentClazz, propertyName);
        setValidator(new NotEmptyValidator().init(value));
        return this;
    }

    /**
     * Defines a 'not null' validator on the current rule builder.
     * Validation will fail if the property is null.
     * Validation will fail if the property is null, an empty or the default value for the type (for example, 0 for integers)
     * @return
     */
    public IRuleBuilderOptions notNull() {
        setValidator(new NotNullValidator());
        return this;
    }

    /**
     * Defines a length validator on the current rule builder, but only for string properties.
     * Validation will fail if the length of the string is outside of the specifed range. The range is inclusive.
     * @param min
     * @param max
     * @return
     */
    public IRuleBuilderOptions length(int min, int max) {
        setValidator(new LengthValidator().init(min, max));
        return this;
    }

    /**
     * Defines a length validator on the current rule builder, but only for string properties.
     * Validation will fail if the length of the string is not equal to the length specified.
     * @param exactLength
     * @return
     */
    public IRuleBuilderOptions length(int exactLength) {
        setValidator(new ExactLengthValidator().init(exactLength));
        return this;
    }

    public IRuleBuilderOptions maxLength(int max) {
        setValidator(new MaxLengthValidator().init(max));
        return this;
    }

    /**
     * Defines a regular expression validator on the current rule builder, but only for string properties.
     * Validation will fail if the value returned by the lambda does not match the regular expression.
     * @param expressions
     * @return
     */
    public IRuleBuilderOptions matches(String... expressions) {
        setValidator(new MatchRegexValidator().init(expressions));
        return this;
    }

    /**
     * Defines a regular expression validator on the current rule builder, but only for string properties.
     * Validation will fail if the value returned by the lambda is not a valid email address.
     * @return
     */
    public IRuleBuilderOptions isEmail() {
        setValidator(new EmailValidator());
        return this;
    }

    /**
     * Defines a 'not equal' validator on the current rule builder.
     * Validation will fail if the specified value is equal to the value of the property.
     * @param toCompare
     * @param <TProperty>
     * @return
     */
    public <TProperty> IRuleBuilderOptions notEqual(TProperty toCompare) {
        notEqual(toCompare, null);
        return this;
    }

    public <TProperty> IRuleBuilderOptions notEqual(TProperty toCompare, Comparator comparator) {
        setValidator(new NotEqualValidator().init(toCompare, comparator));
        return this;
    }

    public IRuleBuilderOptions notEqual(Class<?> clazz,String property) {
        notEqual(clazz, property, null);
        return this;
    }

    public IRuleBuilderOptions notEqual(Class<?> clazz,String property, Comparator comparator) {
        setValidator(new NotEqualValidator().init(clazz, property, comparator));
        return this;
    }

    /**
     * Defines an 'equals' validator on the current rule builder.
     * Validation will fail if the specified value is not equal to the value of the property.
     * @param toCompare
     * @param <TProperty>
     * @return
     */
    public <TProperty> IRuleBuilderOptions equal(TProperty toCompare) {
        setValidator(new EqualValidator().init(toCompare, null));
        return this;
    }

    public <TProperty> IRuleBuilderOptions equal(TProperty toCompare, Comparator comparator) {
        setValidator(new EqualValidator().init(toCompare, comparator));
        return this;
    }

    public IRuleBuilderOptions equal(Class<?> clazz,String property, Comparator comparator) {
        setValidator(new EqualValidator().init(clazz, property, comparator));
        return this;
    }

    public IRuleBuilderOptions equal(Class<?> clazz,String property) {
        setValidator(new EqualValidator().init(clazz, property, null));
        return this;
    }

    /**
     * Defines a 'less than' validator on the current rule builder.
     * The validation will succeed if the property value is less than the specified value.
     * The validation will fail if the property value is greater than or equal to the specified value.
     * @param valueToCompare
     * @param <TProperty>
     * @return
     */
    public <TProperty extends Comparable> IRuleBuilderOptions lessThan(TProperty valueToCompare) {
        setValidator(new LessThanValidator().init(valueToCompare));
        return this;
    }

    /**
     * Defines a 'less than' validator on the current rule builder with specified property.
     * The validation will succeed if the property value is less than the specified value.
     * The validation will fail if the property value is greater than or equal to the specified value.
     * @param clazz
     * @param property
     * @return
     */
    public IRuleBuilderOptions lessThan(Class<?>  clazz, String property) {
        setValidator(new LessThanValidator().init(clazz, property));
        return this;
    }

    /**
     * Defines a 'less than or equal' validator on the current rule builder.
     * The validation will succeed if the property value is less than or equal to the specified value.
     * The validation will fail if the property value is greater than the specified value.
     * @param valueToCompare
     * @param <TProperty>
     * @return
     */
    public <TProperty extends Comparable> IRuleBuilderOptions lessThanOrEqualTo(TProperty valueToCompare) {
        setValidator(new LessThanOrEqualValidator().init(valueToCompare));
        return this;
    }

    /**
     * Defines a 'less than or equal' validator on the current rule builder with specified property.
     * The validation will succeed if the property value is less than or equal to the specified value.
     * The validation will fail if the property value is greater than the specified value.
     * @param clazz
     * @param property
     * @return
     */
    public <TProperty extends Comparable> IRuleBuilderOptions lessThanOrEqualTo(Class<?> clazz, String property) {
        setValidator(new LessThanOrEqualValidator().init(clazz, property));
        return this;
    }

    /**
     * Defines a 'greater than' validator on the current rule builder.
     * The validation will succeed if the property value is greater than the specified value.
     * The validation will fail if the property value is less than or equal to the specified value.
     * @param valueToCompare
     * @param <TProperty>
     * @return
     */
    public <TProperty extends Comparable> IRuleBuilderOptions greaterThan(TProperty valueToCompare) {
        setValidator(new GreaterThanValidator().init(valueToCompare));
        return this;
    }

    public <TProperty extends Comparable> IRuleBuilderOptions greaterThan(Class<?> clazz, String property) {
        setValidator(new GreaterThanValidator().init(clazz, property));
        return this;
    }

    /**
     * Defines a 'greater than or equal' validator on the current rule builder.
     * The validation will succeed if the property value is greater than or equal the specified value.
     * The validation will fail if the property value is less than the specified value.
     * @param valueToCompare
     * @param <TProperty>
     * @return
     */
    public <TProperty extends Comparable> IRuleBuilderOptions greaterThanOrEqualTo(TProperty valueToCompare) {
        setValidator(new GreaterThanOrEqualValidator().init(valueToCompare));
        return this;
    }

    /**
     * Defines a 'greater than or equal' validator on the current rule builder with specified property.
     * The validation will succeed if the property value is greater than or equal the specified value.
     * The validation will fail if the property value is less than the specified value.
     * @param clazz
     * @param property
     * @return
     */
    public IRuleBuilderOptions greaterThanOrEqualTo(Class<?> clazz, String property) {
        setValidator(new GreaterThanOrEqualValidator().init(clazz, property));
        return this;
    }

    /**
     * Defines an 'inclusive between' validator on the current rule builder, but only for properties that is Comparable.
     * Validation will fail if the value of the property is outside of the specifed range. The range is inclusive.
     * @param from
     * @param to
     * @param <TProperty>
     * @return
     */
    public <TProperty extends Comparable<TProperty>> IRuleBuilderOptions between(TProperty from, TProperty to) {
        setValidator(new BetweenValidator<TProperty>().init(from, to));
        return this;
    }

    /**
     * 是否为日期格式：例如 2017-08-01
     * @return
     */
    public IRuleBuilderOptions isDate() {
        setValidator(new DateValidator("yyyy-MM-dd"));
        return this;
    }

    /**
     * 是否为日期时间格式：例如 2017-08-01 18:00:00
     * @return
     */
    public IRuleBuilderOptions isDateTime() {
        setValidator(new DateValidator());
        return this;
    }

    /**
     * 是否为日期格式
     * @param format 设置日期格式：例如 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public IRuleBuilderOptions isDate(String format) {
        setValidator(new DateValidator(format));
        return this;
    }

    /**
     * 是否为日期格式
     * @return
     */
    public IRuleBuilderOptions isMobile() {
        setValidator(new MobileValidator());
        return this;
    }

    /**
     * 是否为ID格式
     * @return
     */
    public IRuleBuilderOptions isId() {
        setValidator(new IdValidator());
        return this;
    }

    /**
     * 是否为身份证号码格式（15位或18位）
     * @return
     */
    public IRuleBuilderOptions isIdCard() {
        setValidator(new IdCardValidator());
        return this;
    }

    /**
     * 是否为身份证号码格式（15位）
     * @return
     */
    public IRuleBuilderOptions isIdCard15() {
        setValidator(new IdCardValidator().init(15));
        return this;
    }

    /**
     * 是否为身份证号码格式（18位）
     * @return
     */
    public IRuleBuilderOptions isIdCard18() {
        setValidator(new IdCardValidator().init(18));
        return this;
    }

    /**
     * 设置当前校验器的错误代码
     * @param code 错误代码
     * @return
     */
    public IRuleBuilderOptions errorCode(int code){
        PropertyValidator validator = ((PropertyValidator)((RuleBuilder)this).getCurrent());
        validator.setFailureCode(code);
        return this;
    }

    /**
     * 设置当前校验器的错误消息
     * @param msg 错误消息
     * @return
     */
    public IRuleBuilderOptions errorMsg(String msg){
        PropertyValidator validator = ((PropertyValidator)((RuleBuilder)this).getCurrent());
        validator.setErrorMessageKey(msg);
        return this;
    }

    /**
     * Specifies a condition limiting when the validator should run.
     * @param predicate
     * @param <T>
     * @return
     */
    public <T> IRuleBuilderOptions when(Boolean predicate){
        return when(predicate, ApplyConditionTo.AllValidators);
    }

    public <T> IRuleBuilderOptions when(final Boolean predicate, final ApplyConditionTo applyConditionTo){
        Utils.checkArgument(predicate, "Predicate should not be null for calling");
        return this.configure(new Action1<PropertyRule>() {
            @Override
            public void doAction(PropertyRule rule) {
                rule.applyCondition(predicate, applyConditionTo);
            }
        });
    }

    public <T> IRuleBuilderOptions when(final Function<PropertyValidatorContext,Boolean> predicate, final ApplyConditionTo applyConditionTo){
        Utils.checkArgument(predicate, "Predicate should not be null for calling");
        return this.configure(new Action1<PropertyRule>() {
            @Override
            public void doAction(PropertyRule rule) {
                rule.applyCondition(predicate, applyConditionTo);
            }
        });
    }

    /**
     * Specifies a condition limiting when the validator should not run.
     * @param predicate
     * @return
     */
    public IRuleBuilderOptions unless(Predicate predicate){
        return this.unless(predicate, ApplyConditionTo.AllValidators);
    }

    public IRuleBuilderOptions unless(Predicate predicate, ApplyConditionTo applyConditionTo){
        return this.when(!predicate.doPredicate(), applyConditionTo);
    }

    public <T> IRuleBuilderOptions cascade(final CascadeMode cascadeMode){
        return this.configure(new Action1<PropertyRule>() {
            @Override
            public void doAction(PropertyRule instance) {
                instance.setCascadeMode(cascadeMode);
            }
        });
    }

    public ICollectionValidatorRuleBuilder setCollectionValidator(IValidator validator, Function<Object, Object> function){
        ChildCollectionValidatorAdaptor adaptor = new ChildCollectionValidatorAdaptor(validator, function);
        this.setValidator(adaptor);
        return new CollectionValidatorRuleBuilder(this, adaptor);
    }

    public class CollectionValidatorRuleBuilder extends ICollectionValidatorRuleBuilder {
        IRuleBuilder ruleBuilder;
        ChildCollectionValidatorAdaptor adaptor;

        public CollectionValidatorRuleBuilder(IRuleBuilder ruleBuilder, ChildCollectionValidatorAdaptor adaptor){
            this.ruleBuilder = ruleBuilder;
            this.adaptor = adaptor;
        }

        @Override
        public IRuleBuilderOptions setValidator(IPropertyValidator validator) {
            return ruleBuilder.setValidator(validator);
        }

        @Override
        public IRuleBuilderOptions setValidator(IValidator validator) {
            return ruleBuilder.setValidator(validator);
        }

        @Override
        public Object configure(Action1 configure) {
            return ((IRuleBuilderOptions)ruleBuilder).configure(configure);
        }

        @Override
        public ICollectionValidatorRuleBuilder where(Predicate1 predicate){
            Utils.checkArgument(predicate, "predicate should ne be null for where calling");
            adaptor.predicate = predicate;
            return this;
        }
    }
}
