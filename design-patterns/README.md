# design-patterns —— 常用设计模式（纯 JavaSE）

> 只依赖 JDK，不引入任何第三方框架；每个模式都可以直接运行对应包下 `*Demo.java` 的 `main` 方法查看效果。

## 两个维度

| 维度 | 包 | 定位 | 写法特点 |
| --- | --- | --- | --- |
| **初级 basic** | `org.xiaoxu.patterns.basic.*` | 给老师做课堂案例演示 | 一个模式一个小故事（咖啡、请假、充电器…），角色最少、类图标准，方便画图讲解 |
| **高级 advanced** | `org.xiaoxu.patterns.advanced.*` | 日常开发能直接抄的代码规范 | 贴真实业务（订单、支付、日志），优先考虑线程安全、不可变、可扩展（对修改关闭） |

## 模式总目录（13 个市面常用模式）

| # | 模式 | 分类 | 初级案例（basic 包） | 高级实践（advanced 包） | 框架中的身影 |
| --- | --- | --- | --- | --- | --- |
| 1 | 单例 Singleton | 创建型 | `singleton/` 饿汉、懒汉 DCL、多线程验证 | `singleton/` Holder 配置中心、枚举单号器、反射/序列化攻防演示 | `Runtime.getRuntime()`、Spring 单例 Bean |
| 2 | 工厂 Factory | 创建型 | `factory/` 简单工厂、工厂方法、抽象工厂 | `factory/` 策略注册表工厂，运行期注册零 if-else | `Calendar.getInstance()`、`Executors` |
| 3 | 建造者 Builder | 创建型 | `builder/` 组装电脑，链式调用 | `builder/` 不可变 HttpRequest：统一校验 + 防御性拷贝 | `StringBuilder`、OkHttp `Request.Builder` |
| 4 | 原型 Prototype | 创建型 | `prototype/` 克隆羊，浅/深拷贝对比 | `prototype/` 序列化深拷贝工具 + 防御性 getter | `Object.clone()`、ArrayList 拷贝构造 |
| 5 | 适配器 Adapter | 结构型 | `adapter/` 220V 转 5V 充电器（类/对象两种） | `adapter/` 老日志系统适配到统一日志网关 | `InputStreamReader`、`HandlerAdapter` |
| 6 | 装饰器 Decorator | 结构型 | `decorator/` 咖啡加奶加糖，层层套娃 | `decorator/` 存储/日志/重试自由组合，模拟网络抖动重试 | Java IO 流、`Collections.sync*` |
| 7 | 代理 Proxy | 结构型 | `proxy/` 明星与经纪人（静态代理） | `proxy/` JDK 动态代理：耗时监控 + 异常透传（AOP 原理） | Spring AOP、MyBatis Mapper、RPC stub |
| 8 | 外观 Facade | 结构型 | `facade/` 家庭影院一键观影 | `facade/` OrderFacade 编排库存/支付/物流 | slf4j 门面、应用层 AppService |
| 9 | 模板方法 Template | 行为型 | `template/` 冲茶/冲咖啡，含钩子方法 | `template/` 回调 + Lambda 替代继承（JdbcTemplate 风格） | `AbstractList`、`JdbcTemplate`、AQS |
| 10 | 策略 Strategy | 行为型 | `strategy/` 出行方式运行时切换 | 与工厂联动：`factory/` 促销折扣全家桶 | `Comparator`、Spring 授权策略 |
| 11 | 观察者 Observer | 行为型 | `observer/` 天气站订阅/退订 | `observer/` 通用 EventBus：按类型订阅、异常隔离 | GUI 监听器、Guava/Spring 事件 |
| 12 | 责任链 Chain | 行为型 | `chain/` 请假审批链（传递型） | `chain/` OkHttp 风格拦截器（请求下行/响应上行） | `Filter`/`Interceptor`、Netty Pipeline |
| 13 | 状态 State | 行为型 | `state/` 订单状态类分发（行为型实现） | `state/` 状态机规则表：流转集中校验（规则型实现） | 订单/工单流转、线程状态 |

## 推荐学习路径

1. **先玩交互页**：双击打开 [`html/index.html`](html/index.html)，13 个初级模式每个一页交互（操作 → Java 代码行高亮 → 日志对照），课前演示或课后自学都合适；
2. 再跑 `basic` 下对应的 `*Demo`，对着输出看角色之间的调用关系；
3. 再跑 `advanced` 下的同款模式，对比"教学版"和"工程版"差在哪（一般是：线程安全、不可变、扩展点、异常处理）；
4. 最后看下表最后一列，去框架源码里找到同款结构。

> 交互页为单文件、零依赖、离线可用；放在 `html/basic/` 下，与源码包一一对应。

## 编译

```bash
mvn -pl design-patterns compile
```
