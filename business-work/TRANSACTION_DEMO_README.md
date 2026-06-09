# 多线程 + @Transactional 事务失效演示

## 演示场景说明

本演示展示在多线程环境下 `@Transactional` 注解失效的常见场景及其解决方案。

## 前置准备

### 1. 确保数据库表存在

```sql
-- 商品表
CREATE TABLE IF NOT EXISTS `t_product` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL
);

-- 商品库存表
CREATE TABLE IF NOT EXISTS `t_product_stock` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `remain` INT NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_product_id` (`product_id`)
);

-- 订单表
CREATE TABLE IF NOT EXISTS `t_order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `order_no` VARCHAR(64) NOT NULL,
  `product_name` VARCHAR(100),
  `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `modify_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` TINYINT DEFAULT 0
);

-- 插入测试数据
INSERT INTO `t_product` (`id`, `name`, `price`) VALUES (1, '测试商品', 99.99);
INSERT INTO `t_product_stock` (`id`, `product_id`, `remain`) VALUES (1, 1, 100);
INSERT INTO `t_user` (`id`, `name`, `status`) VALUES (1, '测试用户', 1);
```

### 2. 启动应用

```bash
cd business-work
mvn spring-boot:run
```

## 演示接口

### 1. 查询当前状态

```bash
curl "http://localhost:8080/demo/transaction/status?productId=1"
```

**预期结果**：
```json
{
  "当前库存": 100,
  "订单总数": 0
}
```

---

### 2. 错误方式：多线程事务失效

```bash
curl -X POST "http://localhost:8080/demo/transaction/wrong?userId=1&productId=1"
```

**预期结果**：
```json
{
  "当前库存": 100,
  "订单总数": 1,
  "演示说明": "库存应回滚到原值，但订单已创建（事务失效）"
}
```

**问题分析**：
1. 主线程在 `@Transactional` 方法中扣减库存
2. 新线程创建订单，但不在主线程的事务中
3. 主线程抛异常回滚，库存恢复
4. 但新线程的订单已经提交，无法回滚
5. **结果：数据不一致（库存不变，但订单已创建）**

---

### 3. 正确方式1：使用 TransactionTemplate

```bash
curl -X POST "http://localhost:8080/demo/transaction/correct1?userId=1&productId=1"
```

**预期结果**：
```json
{
  "当前库存": 99,
  "订单总数": 2,
  "演示说明": "使用 TransactionTemplate，事务正确管理"
}
```

**解决方案**：
- 使用 `TransactionTemplate` 手动管理事务边界
- 在新线程中显式开启事务
- 异常时手动标记回滚

---

### 4. 正确方式2：编程式事务

```bash
curl -X POST "http://localhost:8080/demo/transaction/correct2?userId=1&productId=1"
```

**预期结果**：
```json
{
  "当前库存": 98,
  "订单总数": 3,
  "演示说明": "编程式事务，所有操作在同一事务中"
}
```

**解决方案**：
- 避免在事务中使用多线程
- 所有数据库操作在同一个事务中完成
- 异步任务在事务提交后执行

---

### 5. @Async 事务问题

```bash
curl -X POST "http://localhost:8080/demo/transaction/async?userId=1&productId=1"
```

**预期结果**：
```json
{
  "当前库存": 98,
  "订单总数": 4,
  "演示说明": "@Async 方法在新事务中执行，与调用者事务无关"
}
```

**问题分析**：
1. `@Async` 方法默认开启新事务（`PROPAGATION_REQUIRED`）
2. 与调用者的事务相互独立
3. 调用者回滚时，`@Async` 方法的事务已提交
4. **结果：数据不一致**

---

## 事务失效的根本原因

### 1. ThreadLocal 存储事务上下文

```java
// Spring 事务基于 ThreadLocal
public abstract class TransactionSynchronizationManager {
    private static final ThreadLocal<Map<Object, Object>> resources = 
        new NamedThreadLocal<>("Transactional resources");
    
    // 每个线程有独立的事务上下文
    // 新线程无法获取主线程的事务
}
```

### 2. @Transactional 基于 AOP 代理

```java
// 伪代码：Spring 事务代理逻辑
public class TransactionInterceptor {
    public Object invoke(MethodInvocation invocation) {
        // 1. 获取当前线程的事务
        TransactionStatus tx = getTransaction(txAttr);
        
        try {
            // 2. 执行目标方法
            Object result = invocation.proceed();
            
            // 3. 提交事务
            commit(tx);
            return result;
        } catch (Exception ex) {
            // 4. 回滚事务
            rollback(tx);
            throw ex;
        }
    }
}
```

### 3. 新线程没有事务上下文

```java
@Transactional
public void wrongWay() {
    // 主线程：有事务上下文
    stockService.deduct();  // 在事务中
    
    new Thread(() -> {
        // 新线程：没有事务上下文
        orderService.create();  // 不在事务中（自动提交）
    }).start();
    
    throw new RuntimeException();  // 主线程回滚
    // 但 orderService.create() 已经提交，无法回滚
}
```

---

## 最佳实践

### 1. 避免在事务中使用多线程

```java
// ❌ 错误
@Transactional
public void wrong() {
    stockService.deduct();
    new Thread(() -> orderService.create()).start();
    throw new RuntimeException();
}

// ✅ 正确
@Transactional
public void correct() {
    stockService.deduct();
    orderService.create();  // 在同一事务中
}
```

### 2. 使用 TransactionTemplate 管理复杂事务

```java
public void complexTransaction() {
    transactionTemplate.execute(status -> {
        try {
            stockService.deduct();
            orderService.create();
            return null;
        } catch (Exception e) {
            status.setRollbackOnly();
            throw e;
        }
    });
}
```

### 3. 事务提交后执行异步任务

```java
@Transactional
public void withAsyncAfterCommit() {
    // 1. 事务内操作
    stockService.deduct();
    orderService.create();
    
    // 2. 事务提交后执行异步任务
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 事务提交后执行
                asyncService.sendNotification();
            }
        }
    );
}
```

### 4. 使用 @TransactionalEventListener

```java
@Service
public class OrderService {
    
    @Transactional
    public void createOrder() {
        // 创建订单
        orderMapper.insert(order);
        
        // 发布事件
        applicationEventPublisher.publishEvent(new OrderCreatedEvent(order));
    }
}

@Component
public class OrderEventListener {
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        // 事务提交后执行
        notificationService.send(event.getOrder());
    }
}
```

---

## 常见问题

### Q1: 为什么 @Async 方法不生效？

**A1**: 检查是否满足以下条件：
1. 启动类添加 `@EnableAsync`
2. @Async 方法必须是 `public`
3. @Async 方法不能在同一个类中调用（需要通过代理调用）

### Q2: 如何让多线程在同一个事务中？

**A2**: 不推荐，但可以使用以下方式：
1. 使用 `TransactionTemplate` 手动管理
2. 使用编程式事务
3. 使用分布式事务（如 Seata）

### Q3: @Async 和 @Transactional 可以一起用吗？

**A3**: 可以，但要注意：
1. @Async 方法会开启新事务
2. 与调用者事务相互独立
3. 如果需要在同一事务，不要使用 @Async

---

## 参考资料

- [Spring 事务传播行为](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#tx-propagation)
- [@Transactional 注解陷阱](https://www.baeldung.com/spring-transactional-propagation-isolation)
- [Spring 异步处理](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)