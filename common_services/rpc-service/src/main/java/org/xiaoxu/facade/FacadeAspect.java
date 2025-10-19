package org.xiaoxu.facade;

import com.alibaba.fastjson2.JSON;
import jakarta.validation.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.xiaoxu.base.utils.BeanValidator;
import org.xiaoxu.exception.BizException;
import org.xiaoxu.exception.SystemException;
import org.xiaoxu.response.BaseResponse;
import org.xiaoxu.response.ResponseCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @className: FacadeAspect
 * @author: xiaoxu
 * @date: 2025/10/19 16:18
 * @Version: 1.0
 * @description:
 */
@Aspect
@Component
@Order(Integer.MIN_VALUE)
public class FacadeAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacadeAspect.class);



    @Around("@annotation(org.xiaoxu.facade.Facade)")
    public Object facade(ProceedingJoinPoint  pjp) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();


        Method method = ((MethodSignature) pjp.getSignature()).getMethod();

        Object[] args = pjp.getArgs();

        Class  returnType = method.getReturnType();


        for (Object parameter : args) {

            try {
                BeanValidator.validateObject(parameter);
            } catch (ValidationException e) {
                printErrorLog(stopWatch, method, args, "failed to validate", null, e);
                return getFailedResponse(returnType, e);
            }
        }
        try {
            // 目标方法执行
            Object response = pjp.proceed();
            enrichObject(response);
            printInfoLog(stopWatch, method, args, "end to execute", response, null);
            return response;
        } catch (Throwable throwable) {
            // 如果执行异常，则返回一个失败的response
            printErrorLog(stopWatch, method, args, "failed to execute", null, throwable);
            return getFailedResponse(returnType, throwable);
        }

    }


    private void printInfoLog(StopWatch stopWatch, Method method, Object[] args, String action, Object response, Throwable throwable) {
        try {
            //因为此处有JSON.toJSONString，可能会有异常，需要进行捕获，避免影响主干流程
            LOGGER.info(getInfoMessage(action, stopWatch, method, args, response, throwable), throwable);
            // 如果校验失败，则返回一个失败的response
        } catch (Exception e1) {
            LOGGER.error("log failed", e1);
        }
    }


    private void enrichObject(Object response) {
        if (response instanceof BaseResponse) {
            if (((BaseResponse) response).getSuccess()) {
                //如果状态是成功的，需要将未设置的responseCode设置成SUCCESS
                if (StringUtils.isEmpty(((BaseResponse) response).getResponseCode())) {
                    ((BaseResponse) response).setResponseCode(ResponseCode.SUCCESS.name());
                }
            } else {
                //如果状态是成功的，需要将未设置的responseCode设置成BIZ_ERROR
                if (StringUtils.isEmpty(((BaseResponse) response).getResponseCode())) {
                    ((BaseResponse) response).setResponseCode(ResponseCode.BIZ_ERROR.name());
                }
            }
        }
    }

    private void printErrorLog(StopWatch stopWatch, Method method, Object[] args, String action, Object response,
                               Throwable throwable) {
        try {
            //因为此处有JSON.toJSONString，可能会有异常，需要进行捕获，避免影响主干流程
            LOGGER.error(getInfoMessage(action, stopWatch, method, args, response, throwable), throwable);
            // 如果校验失败，则返回一个失败的response
        } catch (Exception e1) {
            LOGGER.error("log failed", e1);
        }
    }



    private String getInfoMessage(String action, StopWatch stopWatch, Method method, Object[] args, Object response,
                                  Throwable exception) {

        StringBuilder stringBuilder = new StringBuilder(action);
        stringBuilder.append(" ,method = ");
        stringBuilder.append(method.getName());
        stringBuilder.append(" ,cost = ");
        stringBuilder.append(stopWatch.getTime()).append(" ms");
        if (response instanceof BaseResponse) {
            stringBuilder.append(" ,success = ");
            stringBuilder.append(((BaseResponse) response).getSuccess());
        }
        if (exception != null) {
            stringBuilder.append(" ,success = ");
            stringBuilder.append(false);
        }
        stringBuilder.append(" ,args = ");
        stringBuilder.append(JSON.toJSONString(Arrays.toString(args)));

        if (response != null) {
            stringBuilder.append(" ,resp = ");
            stringBuilder.append(JSON.toJSONString(response));
        }

        if (exception != null) {
            stringBuilder.append(" ,exception = ");
            stringBuilder.append(exception.getMessage());
        }

        if (response instanceof BaseResponse) {
            BaseResponse baseResponse = (BaseResponse) response;
            if (!baseResponse.getSuccess()) {
                stringBuilder.append(" , execute_failed");
            }
        }

        return stringBuilder.toString();
    }


    /**
     * 定义并返回一个通用的失败响应
     */
    private Object getFailedResponse(Class returnType, Throwable throwable)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        //如果返回值的类型为BaseResponse 的子类，则创建一个通用的失败响应
        if (returnType.getDeclaredConstructor().newInstance() instanceof BaseResponse) {
            BaseResponse response = (BaseResponse) returnType.getDeclaredConstructor().newInstance();
            response.setSuccess(false);
            if (throwable instanceof BizException bizException) {
                response.setResponseMessage(bizException.getErrorCode().getMessage());
                response.setResponseCode(bizException.getErrorCode().getCode());
            } else if (throwable instanceof SystemException systemException) {
                response.setResponseMessage(systemException.getErrorCode().getMessage());
                response.setResponseCode(systemException.getErrorCode().getCode());
            } else {
                response.setResponseMessage(throwable.toString());
                response.setResponseCode(ResponseCode.BIZ_ERROR.name());
            }

            return response;
        }

        LOGGER.error(
                "failed to getFailedResponse , returnType (" + returnType + ") is not instanceof BaseResponse");
        return null;
    }

}
