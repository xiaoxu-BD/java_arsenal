package org.xiaoxu.patterns.advanced.chain;

/** 拦截器：日志（请求下行打一条，响应上行打一条） */
public class LoggingInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws Exception {
        Request request = chain.request();
        System.out.println("[日志] --> " + request.getMethod() + " " + request.getUrl()
                + " headers=" + request.getHeaders());

        Response response = chain.proceed(request);   // 下行

        System.out.println("[日志] <-- " + response.code() + " body=" + response.body());
        return response;                              // 上行
    }
}
