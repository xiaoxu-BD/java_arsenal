package org.xiaoxu.chainofRes.high;

import java.util.function.Predicate;

public class HandlerWrapper {

    private final Handler handler;

    private final Predicate<BizContext> condition;

    public HandlerWrapper(Handler handler, Predicate<BizContext> condition) {
        this.handler = handler;
        this.condition = condition;
    }

    public boolean match(BizContext ctx) {
        return condition == null || condition.test(ctx);
    }

    public void handle(BizContext ctx) {
        handler.handle(ctx);
    }
}