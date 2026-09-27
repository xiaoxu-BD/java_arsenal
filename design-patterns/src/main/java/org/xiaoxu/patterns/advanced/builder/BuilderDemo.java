package org.xiaoxu.patterns.advanced.builder;

import java.util.Map;

/**
 * 不可变对象 + Builder 演示（高级版）
 */
public class BuilderDemo {

    public static void main(String[] args) {
/*        System.out.println("========== 1. 正常构建 ==========");
        HttpRequest request = HttpRequest.builder("https://api.example.com/orders")
                .method("post")                          // 自动转大写
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer token-xxx")
                .body("{\"skuId\": 1001}")
                .timeout(3000)
                .build();
        System.out.println(request);*/

        System.out.println("\n========== 2. 带病参数统一拦截 ==========");
        tryBuild(HttpRequest.builder("ftp://example.com"));      // 协议不对
        tryBuild(HttpRequest.builder("https://a.com").timeout(-1)); // 超时为负

     /*   System.out.println("\n========== 3. 不可变 + 防御性拷贝 ==========");
        Map<String, String> headers = request.getHeaders();
        try {
            headers.put("X-Hack", "1");                  // 拿到的是只读视图
        } catch (UnsupportedOperationException e) {
            System.out.println("外部改内部 headers 被拒绝: " + e.getClass().getSimpleName());
        }
        // 没有任何 setter，对象一旦 build 就无法被改——多线程共享零风险
        System.out.println("全字段 final、无 setter，线程安全天然成立");*/
    }

    private static void tryBuild(HttpRequest.Builder builder) {
        try {
            builder.build();
        } catch (IllegalArgumentException e) {
            System.out.println("拦截: " + e.getMessage());
        }
    }
}
