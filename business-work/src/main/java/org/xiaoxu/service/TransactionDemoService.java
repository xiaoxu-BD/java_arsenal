package org.xiaoxu.service;

/**
 * 多线程 + @Transactional 事务失效演示服务
 */
public interface TransactionDemoService {

    /**
     * 错误方式：在 @Transactional 方法中开启新线程
     * 问题：新线程中的操作不在同一个事务中，主线程回滚时新线程操作不会回滚
     */
    void wrongWay(Long userId, Long productId);

    /**
     * 正确方式1：使用 TransactionTemplate 手动管理事务
     */
    void correctWayWithTransactionTemplate(Long userId, Long productId);

    /**
     * 正确方式2：使用编程式事务 + 线程池
     */
    void correctWayWithProgrammaticTx(Long userId, Long productId);

    /**
     * 演示 @Async 方法中的事务问题
     * 问题：@Async 方法默认开启新事务，与调用者事务无关
     */
    void asyncTransactionProblem(Long userId, Long productId);

    /**
     * 查询当前库存和订单数量（用于验证事务是否生效）
     */
    java.util.Map<String, Object> queryStatus(Long productId);
}