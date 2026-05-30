package org.xiaoxu.designpattern.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 责任链执行器 — 自动收集所有 RequestHandler，按 order 排序后依次执行
 *
 * Spring Boot 核心用法：
 * 注入 List<RequestHandler> 自动收集所有实现
 * 按 getOrder() 排序后形成链
 */
@Slf4j
@Component
public class RequestChain {

    private final List<RequestHandler> handlers;

    public RequestChain(List<RequestHandler> handlers) {
        // 按 order 排序，构建责任链
        this.handlers = handlers.stream()
                .sorted(Comparator.comparingInt(RequestHandler::getOrder))
                .toList();
    }

    /**
     * 执行整条链
     */
    public void doChain(RequestContext context) {
        execute(context, 0);
    }

    /**
     * 递归执行：当前处理器 → 调用 chain.proceed() → 下一个处理器
     */
    private void execute(RequestContext context, int index) {
        if (index >= handlers.size()) {
            return;
        }
        RequestHandler handler = handlers.get(index);
        log.debug("【责任链】执行: {} (order={})", handler.getClass().getSimpleName(), handler.getOrder());
        handler.handle(context, new InnerChain(context, index + 1));
    }

    /**
     * 内部链实现，传递给处理器供其调用 proceed()
     */
    private class InnerChain extends RequestChain {
        private final RequestContext context;
        private final int nextIndex;

        InnerChain(RequestContext context, int nextIndex) {
            super(RequestChain.this.handlers);
            this.context = context;
            this.nextIndex = nextIndex;
        }

        @Override
        public void doChain(RequestContext context) {
            execute(context, nextIndex);
        }
    }
}
