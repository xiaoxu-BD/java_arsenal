# 事务失效演示 - 快速开始

## 一分钟快速体验

### 1. 初始化数据库

```bash
# 连接 MySQL 并执行初始化脚本
mysql -u root -p < src/main/resources/sql/transaction_demo_init.sql
```

### 2. 启动应用

```bash
cd business-work
mvn spring-boot:run
```

### 3. 运行演示

**方式一：使用测试脚本**

Windows:
```bash
test-transaction-demo.bat
```

Linux/Mac:
```bash
chmod +x test-transaction-demo.sh
./test-transaction-demo.sh
```

**方式二：手动调用接口**

```bash
# 查询初始状态
curl "http://localhost:8080/demo/transaction/status?productId=1"

# 演示错误方式（多线程事务失效）
curl -X POST "http://localhost:8080/demo/transaction/wrong?userId=1&productId=1"

# 查询状态（验证事务失效）
curl "http://localhost:8080/demo/transaction/status?productId=1"
```

---

## 演示结果说明

### 错误方式演示

**调用前**：
```json
{
  "当前库存": 100,
  "订单总数": 0
}
```

**调用后**：
```json
{
  "当前库存": 100,  // 库存回滚了
  "订单总数": 1,    // 但订单没有回滚！
  "演示说明": "库存应回滚到原值，但订单已创建（事务失效）"
}
```

**问题**：库存和订单数据不一致！

---

## 核心知识点

### 1. 为什么多线程会导致事务失效？

```
主线程（有事务）              新线程（无事务）
    │                              │
    ├─ 扣减库存（在事务中）          │
    │                              ├─ 创建订单（自动提交）
    ├─ 抛出异常                    │
    │                              │
    ├─ 事务回滚 ✘                 │
    │   （库存恢复）               │
    │                              ├─ 订单已提交，无法回滚 ✘
```

### 2. Spring 事务基于 ThreadLocal

```java
// 每个线程有独立的事务上下文
ThreadLocal<TransactionStatus> transactionContext;

// 主线程：有事务上下文
// 新线程：没有事务上下文（自动提交模式）
```

### 3. 解决方案

| 方案 | 适用场景 | 优点 | 缺点 |
|------|----------|------|------|
| TransactionTemplate | 复杂事务控制 | 灵活，可在任意线程使用 | 代码冗长 |
| 编程式事务 | 简单场景 | 代码简洁 | 不适合多线程 |
| @TransactionalEventListener | 事务提交后执行 | Spring 原生支持 | 只能用于事务后 |
| 分布式事务（Seata） | 跨服务事务 | 强一致性 | 性能开销大 |

---

## 常见问题

### Q1: 为什么 @Async 方法不生效？

**检查清单**：
1. ✅ 启动类添加 `@EnableAsync`
2. ✅ 方法必须是 `public`
3. ✅ 不能在同一个类中调用（需要通过代理）

### Q2: 如何让多线程共享同一个事务？

**答案**：不推荐，但可以：
1. 使用 `TransactionTemplate` 手动管理
2. 使用编程式事务
3. 使用分布式事务框架

### Q3: @Async 和 @Transactional 可以一起用吗？

**答案**：可以，但要注意：
- @Async 方法会开启新事务
- 与调用者事务相互独立
- 如果需要在同一事务，不要使用 @Async

---

---

## 扩展学习

### 1. 事务传播行为

```java
@Transactional(propagation = Propagation.REQUIRED)  # 默认，加入当前事务
@Transactional(propagation = Propagation.REQUIRES_NEW)  # 开启新事务
@Transactional(propagation = Propagation.NESTED)  # 嵌套事务
```

### 2. 事务隔离级别

```java
@Transactional(isolation = Isolation.READ_COMMITTED)  # 读已提交
@Transactional(isolation = Isolation.REPEATABLE_READ)  # 可重复读
@Transactional(isolation = Isolation.SERIALIZABLE)  # 串行化
```

### 3. 超时设置

```java
@Transactional(timeout = 30)  # 30秒超时
```

### 4. 只读事务

```java
@Transactional(readOnly = true)  # 只读事务，优化性能
```

---

## 参考资料

- [Spring 事务传播行为](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#tx-propagation)
- [@Transactional 注解陷阱](https://www.baeldung.com/spring-transactional-propagation-isolation)
- [Spring 异步处理](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [分布式事务 Seata](https://seata.io/zh-cn/docs/overview/what-is-seata.html)