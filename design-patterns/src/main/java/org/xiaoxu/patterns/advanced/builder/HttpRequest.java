package org.xiaoxu.patterns.advanced.builder;

import java.util.HashMap;
import java.util.Map;

/**
 * 不可变 HTTP 请求对象（高级版 Builder）
 *
 * <p>与 basic 版的三点工程化差距：
 * <ol>
 *   <li><b>不可变</b>：全字段 final、无 setter，build() 之后任何线程拿到都绝对安全；</li>
 *   <li><b>统一校验</b>：参数合法性在 build() 一次性校验，绝不产出半成品/带病对象；</li>
 *   <li><b>防御性拷贝</b>：headers 外部改不到内部 Map，双向隔离。</li>
 * </ol>
 *
 * <p>同款结构：OkHttp {@code Request.Builder()}、Spring Security 配置 Builder、
 * {@code List.copyOf()} 不可变集合。
 */
public final class HttpRequest {

    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final String body;
    private final int timeoutMillis;

    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = Map.copyOf(builder.headers);   // 防御性拷贝：内外互不干扰
        this.body = builder.body;
        this.timeoutMillis = builder.timeoutMillis;
    }

    public String getUrl() { return url; }
    public String getMethod() { return method; }
    public String getBody() { return body; }
    public int getTimeoutMillis() { return timeoutMillis; }

    /** 返回不可变视图：调用方拿到的 headers 永远改不动 */
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        return "HttpRequest{method='" + method + "', url='" + url + "', headers=" + headers
                + ", body='" + body + "', timeout=" + timeoutMillis + "ms}";
    }

    public static Builder builder(String url) {
        return new Builder(url);
    }

    public static final class Builder {

        private final String url;                      // 必填：构造即约束
        private String method = "GET";                // 可选：默认值兜底
        private final Map<String, String> headers = new HashMap<>();
        private String body;
        private int timeoutMillis = 5000;

        private Builder(String url) {
            this.url = url;
        }

        public Builder method(String method) {
            this.method = method.toUpperCase();
            return this;
        }

        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder timeout(int timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        /** 统一校验：所有"带病参数"在这里拦下，错误信息带着字段名，方便定位 */
        public HttpRequest build() {
            if (url == null || url.isBlank()) {
                throw new IllegalArgumentException("url 不能为空");
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new IllegalArgumentException("url 必须以 http:// 或 https:// 开头: " + url);
            }
            if (timeoutMillis <= 0) {
                throw new IllegalArgumentException("timeoutMillis 必须为正数: " + timeoutMillis);
            }
            if (body != null && !"GET".equals(method) && !"POST".equals(method)
                    && !"PUT".equals(method) && !"PATCH".equals(method)) {
                throw new IllegalArgumentException(method + " 请求不允许携带 body");
            }
            return new HttpRequest(this);
        }
    }
}
