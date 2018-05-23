package com.zoe.framework.validation.validators;

import com.zoe.framework.util.CardUtils;
import com.zoe.framework.validate.Validators;
import org.springframework.stereotype.Component;

/**
 * 身份证校验器
 * Created by caizhicong on 2017/8/5.
 */
@Component(Validators.IDCARD)
public class IdCardValidator extends PropertyValidator {

    public int length;

    public IdCardValidator init(int length){
        this.length = length;
        return this;
    }


    @Override
    protected boolean isValid(PropertyValidatorContext context) {
        String value = (String)context.getPropertyValue();
        if(length == 18) return CardUtils.isIdCard18(value);
        if(length == 15) return CardUtils.isIdCard15(value);
        return CardUtils.isIdCard(value);
    }

    @Override
    protected String getDefaultFailureMessageKey() {
        return Validators.PREFIX + "illegal-id-card";
    }
}
