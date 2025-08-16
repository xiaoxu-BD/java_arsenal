package org.xiaoxu.web_boot.utils.deal;

import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class HandleTransactionFailureScenarios {

    private static final Logger log = LoggerFactory.getLogger(HandleTransactionFailureScenarios.class);
    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private ApplicationContext applicationContext;

    public CompletableFuture<Void> processAsyncWithTransaction() {
        return CompletableFuture.runAsync(() -> {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.execute(status -> {
                Map<String, String> cohToCustomerMap = new HashMap<>();
                //使用容器来调用生成代理对象,调用他的方法从而不会产生self-invocation
                HandleTransactionFailureScenarios proxy = applicationContext.getBean(HandleTransactionFailureScenarios.class);
                try {
                    proxy.handleInvoiceData(cohToCustomerMap);
                    proxy.handleThirdData(cohToCustomerMap);
                    proxy.handleThirdListData(cohToCustomerMap);
                } catch (Exception e) {
                    status.setRollbackOnly();
                    log.error("Transaction failed: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
                return null;
            });

        });

    }
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void handleInvoiceData(Map<String, String> cohToCustomerMap) {
        PageHelper.startPage(1, 10);
        List<String> customerNoList = cohToCustomerMap.values().stream().collect(Collectors.toList());
        //业务方法 after according by @Transactional rollback
        //查询 比对 更新
        //doSomething
    }

    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void handleThirdData(Map<String, String> cohToCustomerMap) {

        PageHelper.startPage(1, 10);
        List<String> customerNoList = cohToCustomerMap.values().stream().collect(Collectors.toList());
        //业务方法 after according by @Transactional rollback
        //查询 比对 更新
        //doSomething
    }
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void handleThirdListData(Map<String, String> cohToCustomerMap) {

        PageHelper.startPage(1, 10);
        List<String> customerNoList = cohToCustomerMap.values().stream().collect(Collectors.toList());
        //业务方法 after according by @Transactional rollback
        //查询 比对 更新
        //doSomething
    }
}
