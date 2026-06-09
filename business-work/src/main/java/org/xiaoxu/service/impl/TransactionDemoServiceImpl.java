package org.xiaoxu.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.xiaoxu.service.AsyncOrderService;
import org.xiaoxu.service.OrderService;
import org.xiaoxu.service.StockService;
import org.xiaoxu.service.TransactionDemoService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionDemoServiceImpl implements TransactionDemoService {

    private final StockService stockService;
    private final OrderService orderService;
    private final AsyncOrderService asyncOrderService;
    private final TransactionTemplate transactionTemplate;
    private final JdbcTemplate jdbcTemplate;

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    /**
     * 错误方式：在 @Transactional 方法中开启新线程
     *
     * 问题分析：
     * 1. @Transactional 基于 ThreadLocal 存储事务上下文
     * 2. 新线程无法获取主线程的事务上下文
     * 3. 主线程回滚时，新线程的数据库操作已经提交，无法回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void wrongWay(Long userId, Long productId) {
        log.info("【错误方式】开始执行，主线程: {}", Thread.currentThread().getName());

        // 1. 主线程操作 - 在事务中
        stockService.realDeductAmount(1, productId);
        log.info("【错误方式】主线程扣减库存完成");

        // 2. 新线程操作 - 不在事务中！
        CompletableFuture.runAsync(() -> {
            log.info("【错误方式】新线程开始，线程: {}", Thread.currentThread().getName());
            orderService.createOrder("演示商品", userId, productId);
            log.info("【错误方式】新线程创建订单完成");
        }, executor);

        // 3. 模拟异常 - 主线程回滚
        try {
            Thread.sleep(100); // 等待新线程执行
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("【错误方式】抛出异常，主线程将回滚");
        throw new RuntimeException("模拟异常 - 主线程回滚");
    }

    /**
     * 正确方式1：使用 TransactionTemplate 手动管理事务
     *
     * 优点：可以在任意位置（包括新线程）手动管理事务边界
     */
    @Override
    public void correctWayWithTransactionTemplate(Long userId, Long productId) {
        log.info("【正确方式1】开始执行");

        // 在新线程中使用 TransactionTemplate
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            transactionTemplate.executeWithoutResult(status -> {
                try {
                    log.info("【正确方式1】事务内执行，线程: {}", Thread.currentThread().getName());
                    stockService.realDeductAmount(1, productId);
                    orderService.createOrder("演示商品", userId, productId);

                    // 模拟异常 - 事务会回滚
                     throw new RuntimeException("模拟异常");
                } catch (Exception e) {
                    log.error("【正确方式1】事务回滚", e);
                    status.setRollbackOnly(); // 标记回滚
                }
            });
        }, executor);

        future.join();
        log.info("【正确方式1】执行完成");
    }

    /**
     * 正确方式2：使用编程式事务 + 线程池
     *
     * 将整个业务逻辑放在同一个事务中执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void correctWayWithProgrammaticTx(Long userId, Long productId) {
        log.info("【正确方式2】开始执行，主线程: {}", Thread.currentThread().getName());

        // 方案：不使用多线程，或者确保多线程操作在事务提交后执行
        // 这里演示：先执行所有数据库操作，再执行异步任务

        // 1. 事务内操作
        stockService.realDeductAmount(1, productId);
        orderService.createOrder("演示商品", userId, productId);
        log.info("【正确方式2】事务内操作完成");

        // 2. 如果需要异步操作，应该在事务提交后执行
        // 可以使用 TransactionSynchronizationManager.registerSynchronization()
        // 或者使用 @TransactionalEventListener
    }

    /**
     * 演示 @Async 方法中的事务问题
     *
     * 问题：@Async 方法默认会开启一个新事务（PROPAGATION_REQUIRED）
     * 与调用者的事务是相互独立的
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void asyncTransactionProblem(Long userId, Long productId) {
        log.info("【Async问题】开始执行，主线程: {}", Thread.currentThread().getName());

        // 主线程操作
        stockService.realDeductAmount(1, productId);
        log.info("【Async问题】主线程扣减库存完成");

        // 调用 @Async 方法 - 通过代理调用，异步执行
        asyncOrderService.asyncCreateOrder(userId, productId);

        // 主线程抛异常
        throw new RuntimeException("主线程异常 - 但异步方法已提交");
    }

    /**
     * 查询当前状态（用于验证事务是否生效）
     */
    @Override
    public Map<String, Object> queryStatus(Long productId) {
        Map<String, Object> status = new HashMap<>();

        // 查询库存
        Integer stock = stockService.queryAndLockInventory(productId);
        status.put("当前库存", stock);

        // 查询订单数量
        Long orderCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_order", Long.class);
        status.put("订单总数", orderCount);

        return status;
    }
}