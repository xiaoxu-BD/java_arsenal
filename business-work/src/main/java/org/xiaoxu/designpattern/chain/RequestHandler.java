package org.xiaoxu.designpattern.chain;

/**
 * 责任链处理器接口
 */
public interface RequestHandler {

    /**
     * 处理请求
     * @param context 上下文
     * @param chain   链（用于传递给下一个处理器）
     */
    void handle(RequestContext context, RequestChain chain);

    /**
     * 排序值（越小越先执行）
     */
    int getOrder();
}
