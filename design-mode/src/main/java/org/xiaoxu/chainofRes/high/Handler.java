package org.xiaoxu.chainofRes.high;

@FunctionalInterface
public interface Handler {

    void handle(BizContext ctx);
}