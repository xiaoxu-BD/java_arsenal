package org.xiaoxu.patterns.advanced.chain;

import java.util.List;
import java.util.function.Function;

/**
 * 真实链实现：持有序号，proceed 时"递归地"把自己推向下一个拦截器
 *
 * <p>巧妙之处：链条不是 for 循环跑的，而是每次 proceed 都造一个"下一条更短的链"，
 * 调用栈天然形成 请求下行(递归下降) -> 响应上行(逐层返回) 的结构。
 * OkHttp 的 RealInterceptorChain 就是这么写的。
 */
final class RealInterceptorChain implements Interceptor.Chain {

    private final List<Interceptor> interceptors;
    private final int index;
    /** 链尾目标：真正执行请求的函数（demo 里是 mock 服务端） */
    private final Function<Request, Response> target;
    private final Request request;

    RealInterceptorChain(List<Interceptor> interceptors, int index,
                         Function<Request, Response> target, Request request) {
        this.interceptors = interceptors;
        this.index = index;
        this.target = target;
        this.request = request;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response proceed(Request request) throws Exception {
        if (index >= interceptors.size()) {
            // 链走完：到达真正的目标调用
            return target.apply(request);
        }
        // 把"更短的链"交给当前拦截器，由它决定何时放行
        RealInterceptorChain next = new RealInterceptorChain(interceptors, index + 1, target, request);
        return interceptors.get(index).intercept(next);
    }
}
