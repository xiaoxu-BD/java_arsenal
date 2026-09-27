package org.xiaoxu.patterns.advanced.chain;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 拦截器链演示（高级版责任链）
 *
 * <p>链的组装顺序：日志 -> 重试 -> 鉴权 -> [mock 服务端]
 * 运行输出可以看到两个方向的漏斗：
 * 请求下行（日志先看到裸请求，鉴权给它加了 token 才到服务端），
 * 响应上行（服务端结果依次经过重试、日志的后置处理）。
 */
public class InterceptorChainDemo {

    public static void main(String[] args) throws Exception {
        AtomicInteger serverCalls = new AtomicInteger();

        // 链尾目标：mock 服务端。首次调用抛异常（考验重试），且要求带 token 才放行
        java.util.function.Function<Request, Response> server = request -> {
            if (serverCalls.incrementAndGet() == 1) {
                throw new IllegalStateException("连接超时");
            }
            if (!request.getHeaders().containsKey("Authorization")) {
                return new Response(401, "未登录");
            }
            return new Response(200, "{\"data\":\"订单列表\"}");
        };

        List<Interceptor> interceptors = List.of(
                new LoggingInterceptor(),
                new RetryInterceptor(),
                new AuthInterceptor("token-9527"));

        Request request = new Request("https://api.example.com/orders");
        RealInterceptorChain head = new RealInterceptorChain(interceptors, 0, server, request);

        Response response = head.proceed(request);
        System.out.println("\n最终结果: code=" + response.code() + " body=" + response.body());
        System.out.println("服务端实际被调用 " + serverCalls.get() + " 次（第一次被重试兜住）");
    }
}
