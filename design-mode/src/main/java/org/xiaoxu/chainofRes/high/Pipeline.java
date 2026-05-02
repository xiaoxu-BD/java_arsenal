package org.xiaoxu.chainofRes.high;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Pipeline {

    private final List<HandlerWrapper> handlers = new ArrayList<>();

    public Pipeline add(Handler handler) {
        handlers.add(new HandlerWrapper(handler, null));
        return this;
    }

    public Pipeline add(Handler handler, Predicate<BizContext> condition) {
        handlers.add(new HandlerWrapper(handler, condition));
        return this;
    }

    public void execute(BizContext ctx) {
        for (HandlerWrapper wrapper : handlers) {
            if (ctx.isStopped()) {
                break;
            }

            if (!wrapper.match(ctx)) {
                continue;
            }

            try {
                wrapper.handle(ctx);
            } catch (Exception e) {
                ctx.addError(e.getMessage());
                ctx.stop("Handler 执行异常：" + e.getMessage());
            }
        }
    }
}