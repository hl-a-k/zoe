package com.zoe.framework.validation;

import com.alibaba.fastjson.JSONObject;
import com.zoe.framework.validation.results.ValidationResult;

import java.util.Map;


/**
 * Created by caizhicong on 2017/7/31.
 */
public class MapValidationTests {

    public static void main(String[] args) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","a");

        MapValidator validator = new MapValidator();
        validator.ruleFor(Map.class,"name").notEmpty().greaterThan(1);
        ValidationResult result = validator.validate(jsonObject);
        System.out.println(result);
    }
}
