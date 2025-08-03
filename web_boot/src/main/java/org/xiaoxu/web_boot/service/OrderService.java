package org.xiaoxu.web_boot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
@Slf4j
@Service
public class OrderService {

    @Async("highThread")
    public void processHighPriorityOrder(String orderId) {
        System.out.println("处理高优先级订单: " + orderId + " 线程：" + Thread.currentThread().getName());
        // 模拟业务
    }

    @Async("lowThread")
    public void processLowPriorityLog(String message) {
        System.out.println("记录日志: " + message + " 线程：" + Thread.currentThread().getName());
    }

    @Async("highThread")
    public CompletableFuture<String> asyncGetResult(String id) {
        return CompletableFuture.completedFuture("处理结果：" + id);
    }

    @Autowired
    private ThreadPoolExecutor highThreadPool;
    public void aiPoolUse(String orderId){
        highThreadPool.execute(()->{
            log.info("开始处理订单 {},当前线程:{}",orderId,Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                log.info("订单处理完成 {}",orderId);
            }
        });
    }


    @Async("highThread")
    public void processOrder(String orderId) {
        System.out.println("（@Async）开始处理订单：" + orderId + " 当前线程：" + Thread.currentThread().getName());
        try {
            Thread.sleep(1000); // 模拟耗时
        } catch (InterruptedException ignored) {}
        System.out.println("（@Async）订单处理完成：" + orderId);
    }

    @Autowired
    private PlatformTransactionManager transactionManager;

    public void testTransaction() {
        DefaultTransactionDefinition def  = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            // 事务逻辑
            System.out.println(1/0);
            transactionManager.commit(status);
        } catch (Exception e) {
            log.info("事务处理，回滚{}",e.getMessage());
            transactionManager.rollback(status);
        }


    }
}
