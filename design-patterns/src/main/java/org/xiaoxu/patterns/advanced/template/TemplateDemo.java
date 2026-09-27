package org.xiaoxu.patterns.advanced.template;

/**
 * 回调式模板演示（高级版）
 *
 * <p>三种任务零子类：查询、RPC、会失败的任务，全部一行 Lambda。
 */
public class TemplateDemo {

    public static void main(String[] args) {
        TaskExecutor executor = new TaskExecutor();

        executor.execute("查询订单", () -> {
            Thread.sleep(2);                       // 模拟查库耗时
            System.out.println("  [业务] select * from t_order where id=1001");
        });

        executor.execute("调用物流RPC", () -> {
            Thread.sleep(1);
            System.out.println("  [业务] rpc -> logistics.createShipment");
        });

        System.out.println("\n========== 异常路径同样被模板接管 ==========");
        try {
            executor.execute("风控校验", () -> {
                throw new IllegalArgumentException("命中黑名单");
            });
        } catch (IllegalStateException e) {
            System.out.println("调用方拿到统一异常: " + e.getMessage()
                    + " (根因: " + e.getCause().getMessage() + ")");
        }
    }
}
