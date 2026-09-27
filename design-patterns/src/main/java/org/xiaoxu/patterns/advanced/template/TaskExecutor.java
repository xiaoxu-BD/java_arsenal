package org.xiaoxu.patterns.advanced.template;

/**
 * 任务执行器（高级版模板：回调 + Lambda，替代继承）
 *
 * <p>basic 版用"继承 + 钩子"做模板，痛点：Java 单继承，名额被模板类占了；
 * 每种任务一个子类，类膨胀；子类还能覆盖非 hook 方法破坏流程。
 *
 * <p>工程上的主流做法是<b>组合回调</b>：流程骨架仍然锁死，差异步骤
 * 通过函数式接口传入。JdbcTemplate.execute / 线程池 submit(Callable) 都是这个思路，
 * 匿名内部类退化成一行 Lambda，新增任务类型 = 写一个 lambda，不新增类。
 */
public class TaskExecutor {

    /** 差异步骤的抽象：一个可抛业务异常的动作 */
    @FunctionalInterface
    public interface Task {

        void run() throws Exception;
    }

    /** 模板方法：前置 -> 业务 -> 后置 -> 异常 -> 耗时，骨架锁死 */
    public final void execute(String taskName, Task task) {
        System.out.println("[执行器] " + taskName + " 开始");
        long start = System.nanoTime();
        try {
            task.run();                                  // 差异步骤：lambda 进来
        } catch (Exception e) {
            // 统一异常语义：翻译成运行时异常向上抛，调用方只需处理一类异常
            throw new IllegalStateException(taskName + " 执行失败", e);
        } finally {
            System.out.println("[执行器] " + taskName + " 结束, 耗时="
                    + (System.nanoTime() - start) / 1000 + "μs");
        }
    }
}
