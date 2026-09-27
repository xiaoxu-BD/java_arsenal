package org.xiaoxu.patterns.advanced.chain;

/** 响应对象：record 保证不可变 */
public record Response(int code, String body) {

    public boolean isSuccessful() {
        return code >= 200 && code < 300;
    }
}
