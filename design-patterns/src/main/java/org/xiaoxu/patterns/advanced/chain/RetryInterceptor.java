package org.xiaoxu.patterns.advanced.chain;

/** 拦截器：失败自动重试一次。展示"包住下游结果"的能力 */
public class RetryInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws Exception {
        try {
            return chain.proceed(chain.request());
        } catch (Exception first) {
            System.out.println("[重试] 首次失败(" + first.getMessage() + ")，重试一次");
            return chain.proceed(chain.request());
        }
    }
}
