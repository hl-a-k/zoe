package com.zoe.framework.validation.exception;

import com.zoe.framework.validation.results.ValidationFailure;

import java.util.List;

/**
 * Created by z_wu on 2014/12/9.
 */
public class ValidationException extends IllegalArgumentException{
    public List<ValidationFailure> errors;
    public ValidationException(List<ValidationFailure> errors){
        super(buildErrorMessage(errors));
        this.errors = errors;
    }

    private static String buildErrorMessage(List<ValidationFailure> errors){
        StringBuilder sb = new StringBuilder();
        sb.append("Validation failed:");
        for (ValidationFailure f : errors){
            sb.append("\r\n--" + f.errorMessage());
        }
        return sb.toString();
    }
}
