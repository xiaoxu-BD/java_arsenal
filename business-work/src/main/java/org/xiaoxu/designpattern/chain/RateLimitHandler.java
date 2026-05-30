package org.xiaoxu.designpattern.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 责任链处理器3 — 限流检查
 */
@Slf4j
@Component
public class RateLimitHandler implements RequestHandler {

    @Override
    public void handle(RequestContext context, RequestChain chain) {
        log.info("【限流】限流检查通过");
        context.put("rateLimited", false);
        // 限流通过，传递给下一个处理器（链尾）
        chain.doChain(context);
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
