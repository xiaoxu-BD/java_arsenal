//package org.xiaoxu.web_boot.utils;
//
//import com.alibaba.fastjson2.JSON;
//import com.google.common.collect.ImmutableSet;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.util.Assert;
//import org.springframework.util.StopWatch;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.function.Function;
//
///**
// * @className: RemoteCallUtils
// * @author: xiaoxu
// * @date: 2026/1/5 20:27
// * @Version: 1.0
// * @description:
// */
//@Slf4j
//public class RemoteCallUtils {
//
//    public static <T, R> R call(Function<T, R> function, T request, String requestName) {
//        return call(function, request, requestName, true, false);
//    }
//
//
//
//    public static <T, R> R call(Function<T, R> function, T request, String requestName, boolean checkResponse,
//                                boolean checkResponseCode) {
//        StopWatch stopWatch = new StopWatch();
//        R response = null;
//        try {
//
//            stopWatch.start();
//            response = function.apply(request);
//            stopWatch.stop();
//            if (checkResponse) {
//
//                Assert.notNull(response, REMOTE_CALL_RESPONSE_IS_NULL.name());
//
//                if (!isResponseValid(response)) {
//                    log.error("Response Invalid on Remote Call request {} , response {}",
//                            JSON.toJSONString(request),
//                            JSON.toJSONString(response));
//
//                    throw new RemoteCallException(JSON.toJSONString(response), REMOTE_CALL_RESPONSE_IS_FAILED);
//                }
//            }
//            if (checkResponseCode) {
//
//                Assert.notNull(response, REMOTE_CALL_RESPONSE_IS_NULL.name());
//
//                if (!isResponseCodeValid(response)) {
//                    log.error("Response code Invalid on Remote Call request {} , response {}",
//                            JSON.toJSONString(request),
//                            JSON.toJSONString(response));
//
//                    throw new RemoteCallException(JSON.toJSONString(response), REMOTE_CALL_RESPONSE_IS_FAILED);
//                }
//            }
//
//        } catch (IllegalAccessException | InvocationTargetException e) {
//            log.error("Catch Exception on Remote Call :" + e.getMessage(), e);
//            throw new IllegalArgumentException("Catch Exception on Remote Call " + e.getMessage(), e);
//        } catch (Throwable e) {
//            log.error("request exception {}", JSON.toJSONString(request));
//            log.error("Catch Exception on Remote Call :" + e.getMessage(), e);
//            throw e;
//        } finally {
//            if (log.isInfoEnabled()) {
//
//                log.info("## Method={} ,## 耗时={}ms ,## [请求报文]:{},## [响应报文]:{}", requestName,
//                        stopWatch.getTotalTimeMillis(),
//                        JSON.toJSONString(request), JSON.toJSONString(response));
//            }
//        }
//
//        return response;
//    }
//
//
//    private static ImmutableSet<String> SUCCESS_CHECK_METHOD = ImmutableSet.of("isSuccess", "isSucceeded",
//            "getSuccess");
//
//    private static <R> boolean isResponseValid(R response)
//            throws IllegalAccessException, InvocationTargetException {
//        Method successMethod = null;
//        Method[] methods = response.getClass().getMethods();
//        for (Method method : methods) {
//            String methodName = method.getName();
//            if (SUCCESS_CHECK_METHOD.contains(methodName)) {
//                successMethod = method;
//                break;
//            }
//        }
//        if (successMethod == null) {
//            return true;
//        }
//
//        return (Boolean) successMethod.invoke(response);
//    }
//}
