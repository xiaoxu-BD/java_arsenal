# Spring 事务全面总结

## 一、事务基础

### 1. 什么是事务？

事务是一组操作，要么全部成功，要么全部失败。

### 2. ACID 特性

| 特性 | 说明 | Spring 实现 |
|------|------|-------------|
| **A**tomicity（原子性） | 事务是不可分割的工作单位 | 通过 Undo Log 实现回滚 |
| **C**onsistency（一致性） | 事务前后数据保持一致 | 由应用层保证 |
| **I**solation（隔离性） | 并发事务之间互不干扰 | 通过锁和 MVCC 实现 |
| **D**urability（持久性） | 事务提交后永久保存 | 通过 Redo Log 实现 |

---

## 二、Spring 事务使用方式

### 1. 声明式事务（推荐）

```java
@Transactional(rollbackFor = Exception.class)
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    accountMapper.deduct(fromId, amount);
    accountMapper.add(toId, amount);
}
```

### 2. 编程式事务

```java
// 方式一：TransactionTemplate
@Autowired
private TransactionTemplate transactionTemplate;

public void transfer() {
    transactionTemplate.executeWithoutResult(status -> {
        try {
            accountMapper.deduct(fromId, amount);
            accountMapper.add(toId, amount);
        } catch (Exception e) {
            status.setRollbackOnly();
        }
    });
}

// 方式二：PlatformTransactionManager
@Autowired
private PlatformTransactionManager transactionManager;

public void transfer() {
    TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
    try {
        accountMapper.deduct(fromId, amount);
        accountMapper.add(toId, amount);
        transactionManager.commit(status);
    } catch (Exception e) {
        transactionManager.rollback(status);
    }
}
```

---

## 三、@Transactional 注解详解

### 1. 核心属性

```java
@Transactional(
    propagation = Propagation.REQUIRED,     // 传播行为
    isolation = Isolation.DEFAULT,          // 隔离级别
    timeout = -1,                           // 超时时间（秒）
    readOnly = false,                       // 是否只读
    rollbackFor = Exception.class,          // 回滚异常
    noRollbackFor = RuntimeException.class, // 不回滚异常
    transactionManager = "txManager"        // 指定事务管理器
)
```

### 2. 传播行为（Propagation）

| 传播行为 | 说明 | 常用场景 |
|----------|------|----------|
| **REQUIRED**（默认） | 有事务就加入，没有就新建 | 大多数业务方法 |
| **REQUIRES_NEW** | 总是新建事务，挂起当前事务 | 日志记录、独立子业务 |
| **NESTED** | 有事务就创建嵌套事务，没有就新建 | 部分回滚场景 |
| **SUPPORTS** | 有事务就加入，没有就非事务执行 | 查询方法 |
| **NOT_SUPPORTED** | 非事务执行，有事务就挂起 | 不需要事务的方法 |
| **MANDATORY** | 必须在事务中，没有就抛异常 | 强制要求事务的方法 |
| **NEVER** | 必须非事务，有事务就抛异常 | 不允许事务的方法 |

#### 传播行为示例

```java
@Service
public class OrderService {
    @Autowired
    private LogService logService;

    @Transactional
    public void createOrder() {
        // 1. 创建订单（在当前事务中）
        orderMapper.insert(order);

        // 2. 记录日志（REQUIRES_NEW，独立事务）
        logService.saveLog("订单创建");  // 即使订单回滚，日志也保存

        // 3. 扣减库存（REQUIRED，加入当前事务）
        stockService.deduct(productId);
    }
}

@Service
public class LogService {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(String msg) {
        logMapper.insert(new Log(msg));
    }
}
```

### 3. 隔离级别（Isolation）

| 隔离级别 | 脏读 | 不可重复读 | 幻读 | 性能 |
|----------|------|------------|------|------|
| **READ_UNCOMMITTED** | ✅ | ✅ | ✅ | 最高 |
| **READ_COMMITTED** | ❌ | ✅ | ✅ | 高 |
| **REPEATABLE_READ**（MySQL默认） | ❌ | ❌ | ✅ | 中 |
| **SERIALIZABLE** | ❌ | ❌ | ❌ | 最低 |

#### 并发问题说明

```java
// 脏读：读到未提交的数据
事务A: update balance = 100 (未提交)
事务B: select balance → 100 (脏读)
事务A: rollback
// B读到的数据不存在了

// 不可重复读：两次读取结果不同
事务A: select balance → 100
事务B: update balance = 200, commit
事务A: select balance → 200 (不可重复读)

// 幻读：两次查询记录数不同
事务A: select count(*) → 10
事务B: insert 一条, commit
事务A: select count(*) → 11 (幻读)
```

### 4. rollbackFor 属性

```java
// ❌ 错误：默认只回滚 RuntimeException 和 Error
@Transactional
public void wrong() {
    throw new Exception("checked exception");  // 不会回滚！
}

// ✅ 正确：指定 rollbackFor
@Transactional(rollbackFor = Exception.class)
public void correct() {
    throw new Exception("checked exception");  // 会回滚
}
```

---

## 四、事务失效场景（重点）

### 1. 方法不是 public

```java
@Service
public class UserService {
    @Transactional
    private void wrong() { ... }  // ❌ 事务失效

    @Transactional
    public void correct() { ... } // ✅ 事务生效
}
```

**原因**：Spring AOP 基于代理，只能代理 public 方法。

### 2. 同类方法调用

```java
@Service
public class OrderService {
    public void createOrder() {
        this.saveOrder();  // ❌ 直接调用，事务失效
    }

    @Transactional
    public void saveOrder() { ... }
}
```

**原因**：同类调用走 `this.method()`，绕过代理。

**解决方案**：
```java
// 方案1：拆分到不同类
@Service
public class OrderHelper {
    @Transactional
    public void saveOrder() { ... }
}

// 方案2：注入自身代理
@Autowired
@Lazy
private OrderService self;

public void createOrder() {
    self.saveOrder();  // 通过代理调用
}

// 方案3：AopContext
public void createOrder() {
    ((OrderService) AopContext.currentProxy()).saveOrder();
}
```

### 3. 异常被吞掉

```java
@Transactional
public void wrong() {
    try {
        orderMapper.insert(order);
    } catch (Exception e) {
        log.error("异常", e);  // ❌ 异常被吞掉，不会回滚
    }
}

@Transactional
public void correct() {
    try {
        orderMapper.insert(order);
    } catch (Exception e) {
        throw new RuntimeException(e);  // ✅ 重新抛出，会回滚
    }
}
```

### 4. 异常类型不匹配

```java
@Transactional(rollbackFor = RuntimeException.class)
public void wrong() throws Exception {
    throw new Exception();  // ❌ 不会回滚，Exception 不是 RuntimeException
}

@Transactional(rollbackFor = Exception.class)
public void correct() throws Exception {
    throw new Exception();  // ✅ 会回滚
}
```

### 5. 多线程调用

```java
@Transactional
public void wrong() {
    stockService.deduct(1, productId);  // 主线程操作

    new Thread(() -> {
        orderService.createOrder();     // ❌ 新线程，不在事务中
    }).start();

    throw new RuntimeException();  // 主线程回滚，但订单已提交
}
```

**原因**：事务基于 ThreadLocal，新线程无法获取主线程的事务上下文。

### 6. 数据库引擎不支持

```sql
-- ❌ MyISAM 不支持事务
CREATE TABLE t_order (...) ENGINE=MyISAM;

-- ✅ InnoDB 支持事务
CREATE TABLE t_order (...) ENGINE=InnoDB;
```

### 7. 没有被 Spring 管理

```java
// ❌ 没有 @Service/@Component，Spring 不管理
public class OrderService {
    @Transactional
    public void createOrder() { ... }
}

// ✅ 被 Spring 管理
@Service
public class OrderService {
    @Transactional
    public void createOrder() { ... }
}
```

### 8. final 或 static 方法

```java
@Service
public class OrderService {
    @Transactional
    public final void wrong() { ... }  // ❌ 无法代理

    @Transactional
    public static void wrong2() { ... } // ❌ 无法代理
}
```

---

## 五、@Async + @Transactional 组合

### 1. 问题场景

```java
@Service
public class OrderService {
    @Transactional
    public void createOrder() {
        orderMapper.insert(order);
        asyncNotify();  // ❌ 同类调用，@Async 失效
    }

    @Async
    public void asyncNotify() {
        // 这里会在同一个线程同步执行
    }
}
```

### 2. 正确用法

```java
@Service
public class OrderService {
    @Autowired
    private NotifyService notifyService;

    @Transactional
    public void createOrder() {
        orderMapper.insert(order);
        notifyService.asyncNotify();  // ✅ 跨类调用，异步执行
    }
}

@Service
public class NotifyService {
    @Async
    public void asyncNotify() {
        // 异步执行，在新事务中
    }
}
```

### 3. 事务提交后执行

```java
@Service
public class OrderService {
    @Transactional
    public void createOrder() {
        orderMapper.insert(order);

        // 方式一：TransactionSynchronization
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notifyService.sendMsg();  // 事务提交后执行
                }
            }
        );

        // 方式二：@TransactionalEventListener
        applicationEventPublisher.publishEvent(new OrderCreatedEvent(order));
    }
}

@Component
public class OrderEventListener {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        notifyService.sendMsg();  // 事务提交后执行
    }
}
```

---

## 六、事务最佳实践

### 1. 保持事务短小

```java
// ❌ 错误：事务中包含远程调用
@Transactional
public void wrong() {
    orderMapper.insert(order);
    restTemplate.postForObject("http://payment/pay", ...);  // 远程调用
    emailService.send(email);  // 发邮件
}

// ✅ 正确：事务只包含数据库操作
public void correct() {
    orderMapper.insert(order);
    paymentService.pay();  // 事务外调用
    emailService.send(email);
}
```

### 2. 避免大事务

```java
// ❌ 错误：一个事务包含太多操作
@Transactional
public void bigTransaction() {
    // 100 个数据库操作...
}

// ✅ 正确：拆分事务
public void splitTransaction() {
    transactionTemplate.executeWithoutResult(status -> {
        // 第一批操作
    });
    transactionTemplate.executeWithoutResult(status -> {
        // 第二批操作
    });
}
```

### 3. 只读事务优化

```java
// 查询方法使用只读事务
@Transactional(readOnly = true)
public User getUser(Long id) {
    return userMapper.selectById(id);
}
```

### 4. 指定 rollbackFor

```java
// ✅ 永远指定 rollbackFor
@Transactional(rollbackFor = Exception.class)
public void save() { ... }
```

### 5. 正确处理异常

```java
@Transactional(rollbackFor = Exception.class)
public void save() {
    try {
        orderMapper.insert(order);
    } catch (Exception e) {
        // 记录日志后重新抛出
        log.error("保存失败", e);
        throw e;  // 必须抛出，否则不回滚
    }
}
```

---

## 七、常见面试题

### Q1: @Transactional 失效的场景有哪些？

**A**：
1. 方法不是 public
2. 同类方法调用（this 调用）
3. 异常被 catch 吞掉
4. rollbackFor 类型不匹配
5. 多线程调用
6. 数据库不支持事务（MyISAM）
7. 没有被 Spring 管理
8. final/static 方法

### Q2: REQUIRED 和 REQUIRES_NEW 的区别？

**A**：
- REQUIRED：有事务就加入，没有就新建（默认）
- REQUIRES_NEW：总是新建事务，挂起当前事务

### Q3: 事务传播行为有哪些？

**A**：7 种，常用的是 REQUIRED、REQUIRES_NEW、NESTED

### Q4: 如何实现部分回滚？

**A**：使用 NESTED 传播行为 + savepoint

### Q5: @Async 和 @Transactional 的关系？

**A**：
- @Async 方法默认开启新事务
- 同类调用 @Async 会失效（变成同步）
- 需要跨类调用才能生效

### Q6: Spring 事务基于什么实现？

**A**：
- 基于 AOP 代理（JDK 动态代理或 CGLIB）
- 事务上下文通过 ThreadLocal 存储
- 每个线程有独立的事务上下文

---

## 八、总结图

```
                    Spring 事务
                        │
        ┌───────────────┼───────────────┐
        │               │               │
    声明式事务        编程式事务      事务属性
   @Transactional    TransactionTemplate
        │               │               │
        │               │       ┌───────┼───────┐
        │               │       │       │       │
    AOP 代理          手动控制   传播行为  隔离级别  超时/只读
        │                       │       │
   ┌────┴────┐           ┌──────┴──┐    │
   │         │           │         │    │
 public    同类调用    REQUIRED  REQUIRES_NEW  ...
 方法      会失效      (默认)    (新建事务)
```

---

## 参考资料

- [Spring 官方文档 - 事务管理](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [Spring 事务传播行为详解](https://www.baeldung.com/spring-transactional-propagation-isolation)
- [@Transactional 注解陷阱](https://www.baeldung.com/transaction-configuration-with-jpa-and-spring)