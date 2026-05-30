package org.xiaoxu.designpattern.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 责任链处理器2 — 权限校验
 */
@Slf4j
@Component
public class AuthCheckHandler implements RequestHandler {

    @Override
    public void handle(RequestContext context, RequestChain chain) {
        log.info("【权限】用户权限校验通过");
        context.put("authorized", true);
        // 权限通过，传递给下一个处理器
        chain.doChain(context);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
