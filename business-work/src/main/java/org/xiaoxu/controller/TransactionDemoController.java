package org.xiaoxu.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.xiaoxu.service.TransactionDemoService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/demo/transaction")
@RequiredArgsConstructor
public class TransactionDemoController {

    private final TransactionDemoService transactionDemoService;

    /**
     * 演示1：错误方式 - 多线程事务失效
     *
     * 预期结果：
     * 1. 主线程扣减库存后回滚
     * 2. 新线程创建的订单不会回滚（事务失效）
     * 3. 最终：库存不变，但订单已创建（数据不一致）
     */
    @PostMapping("/wrong")
    public Map<String, Object> wrongWay(@RequestParam Long userId, @RequestParam Long productId) {
        log.info("========== 开始演示：错误方式 ==========");
        try {
            transactionDemoService.wrongWay(userId, productId);
        } catch (Exception e) {
            log.info("捕获异常: {}", e.getMessage());
        }

        // 查询状态验证
        Map<String, Object> status = transactionDemoService.queryStatus(productId);
        status.put("演示说明", "库存应回滚到原值，但订单已创建（事务失效）");
        return status;
    }

    /**
     * 演示2：正确方式1 - 使用 TransactionTemplate
     *
     * 预期结果：
     * 1. 事务正确回滚
     * 2. 库存和订单都回滚到原值
     */
    @PostMapping("/correct1")
    public Map<String, Object> correctWay1(@RequestParam Long userId, @RequestParam Long productId) {
        log.info("========== 开始演示：正确方式1 ==========");
        transactionDemoService.correctWayWithTransactionTemplate(userId, productId);

        Map<String, Object> status = transactionDemoService.queryStatus(productId);
        status.put("演示说明", "使用 TransactionTemplate，事务正确管理");
        return status;
    }

    /**
     * 演示3：正确方式2 - 编程式事务
     *
     * 预期结果：
     * 1. 事务正确提交
     * 2. 库存扣减，订单创建
     */
    @PostMapping("/correct2")
    public Map<String, Object> correctWay2(@RequestParam Long userId, @RequestParam Long productId) {
        log.info("========== 开始演示：正确方式2 ==========");
        transactionDemoService.correctWayWithProgrammaticTx(userId, productId);

        Map<String, Object> status = transactionDemoService.queryStatus(productId);
        status.put("演示说明", "编程式事务，所有操作在同一事务中");
        return status;
    }

    /**
     * 演示4：@Async 事务问题
     *
     * 预期结果：
     * 1. 主线程回滚
     * 2. @Async 方法在新事务中已提交，不会回滚
     * 3. 最终：库存不变，但异步订单已创建
     */
    @PostMapping("/async")
    public Map<String, Object> asyncProblem(@RequestParam Long userId, @RequestParam Long productId) {
        log.info("========== 开始演示：@Async 事务问题 ==========");
        try {
            transactionDemoService.asyncTransactionProblem(userId, productId);
        } catch (Exception e) {
            log.info("捕获异常: {}", e.getMessage());
        }

        // 等待异步方法执行完成
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Map<String, Object> status = transactionDemoService.queryStatus(productId);
        status.put("演示说明", "@Async 方法在新事务中执行，与调用者事务无关");
        return status;
    }

    /**
     * 查询当前状态
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus(@RequestParam Long productId) {
        return transactionDemoService.queryStatus(productId);
    }
}