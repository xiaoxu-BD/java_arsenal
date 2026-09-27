package org.xiaoxu.patterns.advanced.chain;

/**
 * 拦截器（高级版责任链，OkHttp 同款结构）
 *
 * <p>与 basic 请假链的区别：basic 是"传递型"——处理不了才给下家；
 * 拦截器链是"加工型"——每个环节都能看到并加工请求，调用 {@code chain.proceed}
 * 拿到下游返回的响应后再做后置处理，形成"请求下行 / 响应上行"的双向漏斗。
 *
 * <p>日常同款：Servlet Filter、Spring Interceptor、Netty Pipeline、网关过滤器链。
 */
public interface Interceptor {

    /** 每个拦截器：前置处理 -> proceed 交给下游 -> 拿到响应后置处理 -> 返回 */
    Response intercept(Chain chain) throws Exception;

    /** 链的抽象：当前请求 + 把请求传给下游的能力 */
    interface Chain {

        /** 当前节点看到的请求（可能已被上游加工过） */
        Request request();

        /** 调用下游：链尾之后就是真正的目标调用 */
        Response proceed(Request request) throws Exception;
    }
}
