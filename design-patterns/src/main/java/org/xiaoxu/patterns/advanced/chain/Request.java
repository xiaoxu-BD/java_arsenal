package org.xiaoxu.patterns.advanced.chain;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求对象：不可变。拦截器要"改请求"时只能派生新对象（withHeader），
 * 链上任何一环都改不坏别人手里的旧引用——这是并发安全的根基。
 */
public final class Request {

    private final String method;
    private final String url;
    private final Map<String, String> headers;

    public Request(String url) {
        this("GET", url, Map.of());
    }

    private Request(String method, String url, Map<String, String> headers) {
        this.method = method;
        this.url = url;
        this.headers = headers;
    }

    /** 派生：返回加了新 header 的新请求，原请求不动 */
    public Request withHeader(String key, String value) {
        Map<String, String> copy = new HashMap<>(headers);
        copy.put(key, value);
        return new Request(method, url, Map.copyOf(copy));
    }

    public String getMethod() { return method; }
    public String getUrl() { return url; }
    public Map<String, String> getHeaders() { return headers; }

    @Override
    public String toString() {
        return method + " " + url + " " + headers;
    }
}
