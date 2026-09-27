package org.xiaoxu.patterns.advanced.chain;

/** 拦截器：鉴权。给下行请求补充令牌——展示"加工请求"的能力 */
public class AuthInterceptor implements Interceptor {

    private final String token;

    public AuthInterceptor(String token) {
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) throws Exception {
        Request authenticated = chain.request()
                .withHeader("Authorization", "Bearer " + token);
        return chain.proceed(authenticated);
    }
}
