package org.xiaoxu.chainofRes.high;

import java.math.BigDecimal;

public class ChainOfRes {
    public static void main(String[] args) {
        BizContext ctx = new BizContext();
        ctx.put("amount", new BigDecimal("1500"));

        Pipeline pipeline = new Pipeline()
                .add(c -> System.out.println("参数校验"))
                .add(c -> System.out.println("金额大于1000，执行风控"),
                        c -> c.get("amount", BigDecimal.class).compareTo(new BigDecimal("1000")) > 0)
                .add(c -> System.out.println("创建订单"))
                .add(c -> System.out.println("发送 MQ"));

        pipeline.execute(ctx);
    }
}
