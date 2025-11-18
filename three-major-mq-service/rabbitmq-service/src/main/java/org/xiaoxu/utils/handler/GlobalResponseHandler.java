package org.xiaoxu.utils.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.xiaoxu.utils.Result;

/**
 * @className: GlobalResponseHandler
 * @author: xiaoxu
 * @date: 2025/11/18 10:27
 * @Version: 1.0
 * @description:
 */
@RestControllerAdvice
@Slf4j
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {



    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 记录日志
        log.error("系统发生未知异常", e);
        // 返回一个通用的错误信息
        return Result.error(500,e.getMessage());
    }

    // 在 GlobalResponseHandler 中注入 ObjectMapper
    private final ObjectMapper objectMapper;

    // 使用构造器注入
    public GlobalResponseHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        //return false 就是不拦截处理
//        return false;
        // 如果返回类型本身就是 Result，或者被特定注解标记，则不再包装，避免重复包装
        return  !returnType.getParameterType().equals(Result.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        if (body instanceof Result) {
            return body;
        }

        // 如果 body 为 null，返回一个无数据的成功结果
        if (body == null) {
            return Result.success();
        }

        // 如果返回值是 String 类型，需要特殊处理
        if (body instanceof String) {
            try {
                // 使用 Jackson/ObjectMapper 将 Result 对象序列化为 JSON 字符串
                return objectMapper.writeValueAsString(Result.success(body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("序列化响应结果失败", e);
            }
        }

        // 否则，用成功结果包装 body
        return Result.success(body);
    }
}
