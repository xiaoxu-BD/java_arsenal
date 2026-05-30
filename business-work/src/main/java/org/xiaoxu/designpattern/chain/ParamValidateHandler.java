package org.xiaoxu.designpattern.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 责任链处理器1 — 参数校验
 */
@Slf4j
@Component
public class ParamValidateHandler implements RequestHandler {

    @Override
    public void handle(RequestContext context, RequestChain chain) {
        log.info("【校验】参数校验通过");
        context.put("validated", true);
        // 校验通过，传递给下一个处理器
        chain.doChain(context);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
