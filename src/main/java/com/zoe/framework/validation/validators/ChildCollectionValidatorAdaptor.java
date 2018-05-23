package com.zoe.framework.validation.validators;

import com.zoe.framework.validation.IValidator;
import com.zoe.framework.validation.Predicate1;
import com.zoe.framework.validation.ValidationContext;
import com.zoe.framework.validation.results.ValidationFailure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * Created by z_wu on 2014/12/19.
 */
public class ChildCollectionValidatorAdaptor implements IPropertyValidator {
    private IValidator childValidator;
    public Predicate1 predicate;
    private Function<Object, Object> function;

    public ChildCollectionValidatorAdaptor(IValidator childValidator, Function<Object, Object> function){
        this.childValidator = childValidator;
        this.function = function;
    }

    @Override
    public List<ValidationFailure> validate(PropertyValidatorContext context) {
        List<ValidationFailure> validationFailures = new ArrayList<>();
        if(context.propertyRule.member == null){
            throw new RuntimeException("Nested validators can only be used with Member Expressions.");
        }
        Object value = context.getPropertyValue();
        if(value != null && function != null) {
            value = function.apply(value);
        }
        Iterable<Object> collection =  value instanceof Iterable ? (Iterable)value : null;
        if(collection == null)
            return new ArrayList<>();
        Iterator<Object> it = collection.iterator();

        while (it.hasNext()){
            Object element = it.next();
            boolean result = false;
            if(predicate == null)
                result = true;
            if(predicate != null)
                result = predicate.doPredicate(element);
            if(element == null || !result)
                continue;
            ValidationContext newContext = context.parentContext.cloneForChildValidator(element);
            newContext.getPropertyChain().add(context.propertyRule.member);
            List<ValidationFailure> vfs = childValidator.validate(newContext).errors();
            validationFailures.addAll(vfs);
        }
        return validationFailures;
    }

    public IValidator getValidator(){
        return childValidator;
    }
}
