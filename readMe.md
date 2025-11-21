# Java Arsenal

> 多模块 Java/Spring Boot 技术练兵场，涵盖 Web、微服务、权限、安全、Starter、并发、设计模式等多个主题。

---

## 总览

- **语言/框架**：Java 17、Spring Boot 3.2.x、Spring Cloud 2023.0.x、Spring Cloud Alibaba 2023.0.1.2、Dubbo 3.2.x
- **基础设施**：MySQL、Redis、Nacos、RabbitMQ、Elasticsearch 等
- **多模块结构**：通过父级 `pom.xml` 统一版本和依赖管理，每个子模块可独立运行或组合使用。

---

## 目录速览

| 模块 | 说明 |
| --- | --- |
| `web_boot/` | 大型 Web 应用，整合多数据源、MyBatis、Redis、Dubbo、定时任务等，既是 Dubbo Provider 也是综合练习项目 |
| `business-service/` | 业务服务集合，`demo-service` 等作为 Dubbo Consumer，演示如何接入自定义 starter |
| `common_services/` | 自研 Starter 仓库（cache、datasource、rpc、job、base 等），通过自动装配向业务提供通用能力 |
| `permission-system/` | 独立权限系统示例：Spring Security + JWT + Redis，自定义登录、鉴权、Token 过滤链 |
| `gateway_service/` | 网关示例工程 |
| `core_se/` | Java SE 核心技术与语法练习 |
| `juc_demo/` | 并发/JUC 相关示例 |
| `design-mode/` | 设计模式演练 |
| `docs/` | 使用指南与技术文档（如 Starter + Nacos 配置指南） |

---

## 快速开始

```bash
# 1. 拉取代码
git clone https://github.com/xxx/java_arsenal.git

# 2. 进入工程
cd java_arsenal

# 3. 构建所有模块
mvn clean install -DskipTests

# 4. 按需运行模块（示例）
cd web_boot && mvn spring-boot:run
cd permission-system && mvn spring-boot:run
```

> 建议事先准备好 MySQL / Redis / Nacos 等环境（可本地或 Docker），并根据模块配置调整连接信息。

---

## 自定义 Starter 使用方式

`common_services` 下的 starter 默认不依赖配置中心，只需：

1. 在 starter 中定义 `@ConfigurationProperties` + `@Configuration`
2. 在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 暴露自动装配
3. 业务服务侧（如 `web_boot`、`demo-service`）引入 starter，并通过本地 `application.yml` 或 Nacos 下发配置

详见 `docs/starter-nacos-config.md`。

---

## 权限系统示例（permission-system）

- 登录：`POST /login`（JWT 签发 + Redis 缓存 token）
- 访问资源：请求头携带 `Authorization: Bearer <token>`
- 核心组件：`SecurityConfig`、`UserDetailsServiceImpl`、`TokenAuthenticationFilter`、`JwtTokenProvider`

---

## Dubbo 远程调用示例

- Provider：`web_boot`（Dubbo Service + Nacos 注册）
- Consumer：`business-service/demo-service`（`@DubboReference` 调用）
- 自定义 starter：`rpc-service` 提供 `@EnableDubbo` 自动装配

---

## 贡献/扩展建议

- 补充单元/集成测试，确保自定义 starter 与业务模块的稳定性
- 优化配置管理（本地 yml → Nacos/Consul）
- 完善文档（模块 README、API 示例、运维脚本等）
- 引入 CI/CD 以自动校验多模块构建

---

## 参考文档

- `docs/starter-nacos-config.md`：Starter + Nacos 配置指南
- 各模块内 README / 注释

---

如有问题或改进建议，欢迎提 Issue 或 PR，一起丰富这个“Java Arsenal”。💪