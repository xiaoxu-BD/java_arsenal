package org.xiaoxu.designpattern.chain;

import java.util.HashMap;
import java.util.Map;

/**
 * 责任链上下文 — 在链上传递的数据载体
 *
 * 各处理器通过 context.get/put 交换数据
 */
public class RequestContext {

    private final Map<String, Object> attributes = new HashMap<>();

    public void put(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) attributes.get(key);
    }

    public boolean has(String key) {
        return attributes.containsKey(key);
    }
}
