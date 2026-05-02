package org.xiaoxu.chainofRes.basic;

import org.apache.commons.lang3.StringUtils;

public class ChainOfResponsibility {


    public static void main(String[] args) {



//        test01();



//        test02();
        Pipeline pipeline = new Pipeline().add(ctx -> {
            if (ctx.getData().length() < 6) {
                ctx.stop();
            }
        }).add(ctx -> {
            if (!StringUtils.containsAny(ctx.getData(), "0123456789")) {
                ctx.stop();
            }
        }).add(ctx -> {
            if (!containsUpperCase(ctx.getData())) {
                System.out.println("该字符串不符合校验3 必须包含大写字母");
                ctx.stop();
            }
        });
        pipeline.execute(new Context("abc123"));

    }


//    要判断字符串是否必须包含大写字母，逻辑与判断数字类似，但在 Java 中有几种更优雅的写法：
//
//            1. 正则表达式（最直接）
//    使用 [A-Z] 匹配任何一个大写字母。
//
//    Java
    public static boolean containsUpperCase(String s) {
        return s != null && s.matches(".*[A-Z].*");
    }


    private static void test02() {
        Pipeline pipeline = new Pipeline().add(ctx -> {
//            去除空格
                    ctx.setData(ctx.getData().trim());
                }).add(ctx -> ctx.setData(ctx.getData().toUpperCase()))
                .add(ctx -> {
                    if (ctx.getData().contains("WORLD")) {
                        ctx.setData(  ctx.getData().replace("WORLD", "JAVA"));
                        System.out.println(ctx.getData());
                    }
                });

        pipeline.execute(new Context("  hello world  "));
    }

//    private static void test01() {
//        Pipeline pipeline = new Pipeline().add(ctx -> {
//                    System.out.println("✅ 节点1：格式校验");
//                    if (ctx.data == null) ctx.stop(); // 数据为空，直接中断
//                })
//                .add(ctx -> {
//                    System.out.println("✅ 节点2：敏感词校验");
//                    if (ctx.data.contains("违规")) {
//                        System.out.println("🚫 发现违规，拦截！");
//                        ctx.stop();
//                    }
//                }).add(ctx -> {
//                            System.out.println("✅ 节点3：入库");
//                        }
//                );
//
//
//        System.out.println("--- 测试正常数据 ---");
//        pipeline.execute(new Context("正常内容"));
//
//
//        System.out.println("--- 测试违规数据 ---");
//        pipeline.execute(new Context("违规数据"));
//    }
}
