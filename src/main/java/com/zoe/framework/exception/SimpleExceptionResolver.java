package com.zoe.framework.exception;

import com.zoe.framework.OpResult;
import com.zoe.framework.json.JsonHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Spring MVC 异常处理
 *
 * Created by caizhicong on 2016/1/13.
 */
public class SimpleExceptionResolver extends SimpleMappingExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception ex) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if(handlerMethod == null){
            return super.doResolveException(request, response, handlerMethod, ex);
        }

        ResponseBody body = handlerMethod.getMethodAnnotation(ResponseBody.class);
        // 判断有没有@ResponseBody的注解没有的话调用父方法
        if (body == null) {
            return super.doResolveException(request, response, handlerMethod, ex);
        }
        ModelAndView mv = new ModelAndView();
        // 设置状态码,注意这里不能设置成500，设成500JQuery不会出错误提示
        // 并且不会有任何反应
        response.setStatus(HttpStatus.OK.value());
        // 设置ContentType
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // 避免乱码
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, must-revalidate");
        try {
            ex.printStackTrace();
            PrintWriter writer = response.getWriter();
            OpResult opResult = OpResult.failedResult().msg("发生异常：" + ex.getMessage());
            writer.write(JsonHelper.serialize(opResult));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mv;
    }
}
