package com.zoe.framework.validate;

import com.zoe.framework.json.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ValidateInterceptor
 * Created by caizhicong on 2017/7/13.
 */
@Component
public class ValidateInterceptor extends HandlerInterceptorAdapter {

    private Logger logger = LoggerFactory.getLogger(ValidateInterceptor.class);

    private final Validators validators;

    @Autowired
    public ValidateInterceptor(Validators validators) {
        this.validators = validators;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            ValidateRequestMapping validate = method.getMethodAnnotation(ValidateRequestMapping.class);
            if (validate != null && validate.value().length > 0) {
                ValidateRequestMapping parent = ((HandlerMethod) handler).getBeanType().getAnnotation(ValidateRequestMapping.class);
                ValidateWrapper2 wrapper2 = new ValidateWrapper2(request, validate, parent);
                Object validateResult = validators.validate(wrapper2);
                if (validateResult != null) {
                    writeResponse(response, validateResult);
                    return false; //检验不通过
                }
            }
        }
        return true;
    }

    private void writeResponse(HttpServletResponse response, Object data) {
        try {
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            PrintWriter writer = response.getWriter();
            writer.write(JsonHelper.serialize(data));
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            logger.error("输出错误信息发生异常！message={}", ex.getMessage());
        }
    }
}
