# 自定义 Starter + Nacos 配置指南

> 目标：Starter **不** 引入 Nacos 依赖，只负责提供可配置的 `@Configuration`。真正使用 Starter 的业务应用去接 Nacos，从远程拉取配置，然后注入到 Starter 中。

---

## 1. Starter 端如何写

### 1.1 定义可配置的属性类

```java
@ConfigurationProperties(prefix = "xiaoxu.cache")
public class CacheProperties {
    /**
     * redis://ip:port
     */
    private String address;
    private String password;
    private int database = 0;
    private Integer timeout;
    // getter / setter
}
```

### 1.2 提供自动装配
```java
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfiguration {

    @Bean
    public RedissonClient redissonClient(CacheProperties properties) {
        Config config = new Config();
        config.useSingleServer()
              .setAddress(properties.getAddress())
              .setPassword(properties.getPassword())
              .setDatabase(properties.getDatabase())
              .setTimeout(properties.getTimeout());
        return Redisson.create(config);
    }
}
```

### 1.3 暴露 AutoConfiguration
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
```
org.xiaoxu.configuration.CacheConfiguration
```

> 至此，Starter 不含任何 Nacos 依赖，只要应用侧给出 `xiaoxu.cache.*` 配置即可。

---

## 2. 应用端如何使用 Starter + Nacos

### 2.1 引入依赖
```xml
<dependency>
    <groupId>org.xiaoxu</groupId>
    <artifactId>cache-service</artifactId>
    <version>1.0</version>
</dependency>

<!-- Spring Cloud Alibaba Nacos Config -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

### 2.2 配置 bootstrap.yml
> `bootstrap.yml` 会在应用启动最早阶段加载，用来告诉 Spring 去 Nacos 拉配置。

```yaml
spring:
  application:
    name: permission-service

  cloud:
    nacos:
      config:
        server-addr: ${NACOS_ADDR:192.168.200.128:8848}
        namespace: ${NACOS_NAMESPACE:public}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yaml
        username: ${NACOS_USER:nacos}
        password: ${NACOS_PASS:nacos}

        # 根据需要加载额外 dataId（可选）
        extension-configs:
          - data-id: cache-service.yaml
            group: STUDY_RPC
            refresh: true
          - data-id: datasource-service.yaml
            group: STUDY_RPC
            refresh: true
```

### 2.3 在 Nacos 创建配置

| dataId                | group      | 内容示例                                  |
|----------------------|------------|-------------------------------------------|
| `permission-service.yaml` | `DEFAULT_GROUP` | 应用专属配置（端口、日志等）          |
| `cache-service.yaml` | `STUDY_RPC`| Starter 所需的参数 (参见下方示例)         |
| `datasource-service.yaml` | `STUDY_RPC`| 数据源 Starter 参数                      |

**cache-service.yaml**
```yaml
xiaoxu:
  cache:
    address: redis://192.168.200.128:6379
    password: NFTurbo666
    database: 0
    timeout: 3000
```

**datasource-service.yaml**
```yaml
xiaoxu:
  datasource:
    master:
      url: jdbc:mysql://192.168.200.128:3306/permission_system
      username: root
      password: NFTurbo666
      driver-class-name: com.mysql.cj.jdbc.Driver
    slave:
      url: jdbc:mysql://192.168.200.128:3307/permission_system
      username: reader
      password: reader123
```

### 2.4 应用层 `application.yml`
> 不需要再写死具体 IP，只保留与环境无关的配置。

```yaml
server:
  port: 13690

spring:
  profiles:
    active: nacos

logging:
  level:
    root: info
```

---

## 3. 常见问题 & 建议

1. **多个 Starter 共享配置**  
   - 可以使用 `shared-configs` / `extension-configs`，将共用的 redis / datasource 参数拆成独立 dataId。

2. **刷新机制**  
   - 在 starter 的属性类上增加 `@RefreshScope` 或者在使用 Bean 的地方标记 `@RefreshScope`，即可实时更新配置。

3. **本地调试**  
   - `bootstrap.yml` 中的 `${NACOS_ADDR:…}` 保证本地没配置环境变量时走默认值，部署在不同环境只需注入对应变量。

4. **敏感信息**  
   - 建议配合 Nacos 的加密工具（`nacos-console` 的加密功能）或 KMS，避免明文密码。

5. **配置优先级**  
   - `bootstrap.yml` < 远程配置 < `application.yml`。若想本地覆盖 Nacos，可在 `application-local.yml` 中重新赋值。

---

## 4. 总结

| 角色        | 是否依赖 Nacos | 作用                                                |
|-------------|----------------|-----------------------------------------------------|
| Starter 模块 | ❌              | 只暴露 `@Configuration` + `@ConfigurationProperties` |
| 业务应用     | ✅              | 引入 Nacos Config，指定 dataId，负责拉取远程配置       |

这样既保持 starter 的纯净，又能让业务在不同环境中通过 Nacos 动态调整配置。

--- 

如需给具体 starter（如 cache、datasource、rpc）补充示例 dataId，可以在 Nacos 中按模块拆分，方便共享与维护。

