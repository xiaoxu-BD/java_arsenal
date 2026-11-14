package org.xiaoxu.wecommail.service;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xiaoxu.wecommail.dto.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static java.lang.Thread.sleep;

@Service
public class MockService {


    private static final Logger log = LoggerFactory.getLogger(MockService.class);



    @Resource
    private ExecutorService taskExecutor;

    public UserInfo getUserInfo(Long userId) {
        return UserInfo.builder()
                .userId(userId)
                .userName("张三")
                .email("zhangsan@example.com")
                .age(28)
                .build();
    }


    public List<Order> getRecentOrders(Long userId) {
        return Arrays.asList(
                Order.builder().orderId(1001L).userId(userId).productName("机械键盘")
                        .amount(new BigDecimal("399.00")).createTime(LocalDateTime.now().minusDays(1)).build(),
                Order.builder().orderId(1002L).userId(userId).productName("显示器")
                        .amount(new BigDecimal("1299.00")).createTime(LocalDateTime.now().minusDays(3)).build(),
                Order.builder().orderId(1003L).userId(userId).productName("鼠标垫")
                        .amount(new BigDecimal("29.90")).createTime(LocalDateTime.now().minusDays(5)).build()
        );
    }

    public Integer getUserPoints(Long userId) {
        return 2680;
    }

    public Long fetchUserId(String username) {
        // 模拟耗时
        sleep(100);
        // 模拟数据：根据 username 返回固定 userId
        return switch (username) {
            case "Alice" -> 1001L;
            case "bob"   -> 1002L;
            default      -> 9999L;
        };
    }

    public UserDetail queryUserDetail(Long userId) {
        sleep(150);
        return UserDetail.builder()
                .userId(userId)
                .username(userId == 1001L ? "Alice" : "bob")
                .nickname(userId == 1001L ? "爱丽丝" : "鲍勃")
                .email((userId == 1001L ? "Alice" : "bob") + "@example.com")
                .build();
    }



    public EnrichedDetail enrichWithPermissions(UserDetail detail) {
        sleep(80);
        List<String> perms = detail.getUserId() == 1001L
                ? List.of("user:read", "order:read", "order:write")
                : List.of("user:read");
        return EnrichedDetail.builder()
                .detail(detail)
                .permissions(perms)
                .build();
    }

    public void sendToClient(EnrichedDetail result) {
        throw new IllegalArgumentException("故意参数异常");
//        System.out.println("[sendToClient] " + JSON.toJSONString(result));
    }


    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    /**
     *  并行执行多个任务
     * @param userId 参数
     * @return 返回最终的结果
     */
    public UserDashboardVO getUserDashboard(Long userId) {
        // 并行执行三个独立查询
        CompletableFuture<UserInfo> userFuture = CompletableFuture.supplyAsync(
                () -> getUserInfo(userId), taskExecutor
        );

        CompletableFuture<List<Order>> orderFuture = CompletableFuture.supplyAsync(
                () -> getRecentOrders(userId), taskExecutor
        );

        CompletableFuture<Integer> pointsFuture = CompletableFuture.supplyAsync(
                () -> getUserPoints(userId), taskExecutor
        );

        // 等待所有任务完成并组合结果
        CompletableFuture.allOf(userFuture, orderFuture, pointsFuture).join();

        return UserDashboardVO.builder()
                .userInfo(userFuture.join())
                .orders(orderFuture.join())
                .points(pointsFuture.join())
                .build();
    }




    public void processOne(String username) {
        CompletableFuture
                .supplyAsync(() -> fetchUserId(username), taskExecutor)
                .thenApply(userId -> queryUserDetail(userId))
                .thenApply(detail -> enrichWithPermissions(detail))
                .thenAccept(result -> sendToClient(result))
                .exceptionally(ex -> {
                    log.error("处理失败, username={}", username, ex);
                    return null;
                });
    }
}
