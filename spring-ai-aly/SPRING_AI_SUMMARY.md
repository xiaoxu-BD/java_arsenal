# spring-ai-aly 中的 Spring AI / 大模型能力总结

本文档根据 `spring-ai-aly` 当前源码和配置整理，覆盖已经接入或演示的大模型相关能力。文档中的“当前生效”以 `pom.xml` 和 `application.yml` 未注释内容为准。

## 1. 模块定位

`spring-ai-aly` 是一个 Spring Boot Web 示例模块，用于演示 Spring AI 对聊天模型的统一调用方式，当前主要连接阿里云 DashScope 模型服务。模块提供了以下能力：

- 普通同步对话和流式对话
- `ChatClient` 与底层 `ChatModel` 两种调用层级
- 提示词模板和简单的提示词工程
- 结构化输出到 Java Bean
- 基于会话 ID 的上下文记忆，支持 JDBC 持久化
- Tool Calling / Function Calling，让模型调用 Java 方法获取实时数据
- 浏览器端 SSE 流式接收和 Markdown 渲染

当前模块没有看到 Embedding、向量数据库、RAG、重排、图像生成、语音、视频或 Agent 编排的实际业务调用。

## 2. 技术栈和依赖

| 依赖或组件 | 当前用途 |
| --- | --- |
| Spring Boot Web / WebFlux | 提供 HTTP 接口、`Flux` 流式返回和 Web 层能力 |
| Spring AI `1.1.0` | 提供 `ChatClient`、`ChatModel`、Prompt、Memory、结构化输出等抽象 |
| `spring-ai-alibaba-starter-dashscope` `1.1.0.0` | 自动装配 DashScope `ChatModel` |
| `spring-ai-autoconfigure-model-chat-memory` `1.1.0` | Spring AI 会话记忆相关自动配置 |
| `spring-ai-starter-model-chat-memory-repository-jdbc` `1.1.0` | JDBC 会话记忆仓库 |
| `spring-ai-alibaba-starter-memory-long` `1.1.0.0-M5` | 已声明依赖，但当前源码没有直接使用其 API |
| MySQL Connector/J `8.0.33` | 保存 JDBC 会话记忆 |
| Java | 模块编译目标为 21，父工程默认版本为 17 |

`spring-ai-starter-model-openai` 和 Ollama 相关依赖目前是注释状态，不会参与当前启动。OpenAI starter 可用于 DeepSeek 或 DashScope 兼容模式，但切换时需要和 DashScope starter 二选一，避免自动装配出多个 `ChatModel`。

## 3. 模型配置和自动装配

配置文件：`src/main/resources/application.yml`

当前生效配置的核心关系如下：

```text
spring.ai.dashscope.api-key
        |
        v
DashScope starter 自动装配 ChatModel
        |
        v
ChatModelConfiguration 构建 ChatClient
        |
        v
各 Controller 调用大模型
```

当前模型设置为 `qwen-plus`，默认端口为 `8000`。`ChatModelConfiguration` 注入自动装配的 `ChatModel`，并声明两个 `ChatClient`：

- `chatClientBySelf`：启用 `SimpleLoggerAdvisor` 和 `MessageChatMemoryAdvisor`，适合需要会话记忆的调用。
- `chatClient1`：只启用 `SimpleLoggerAdvisor`，适合无记忆调用和工具调用。

两个客户端都设置了 DashScope `temperature=0.7`。其他控制器还会自行通过 `ChatClient.builder(chatModel)` 创建客户端，因此这些客户端不会自动继承全局 Advisor 或默认配置。

## 4. 核心调用方式

### 4.1 `ChatModel`：底层模型接口

`ChatMemoryController` 展示了直接操作 Spring AI `ChatModel` 的方式：

- `chatModel.call(prompt)`：同步返回 `ChatResponse`，再从结果中提取文本。
- `chatModel.stream(prompt)`：返回 `Flux<ChatResponse>`，逐块读取模型输出文本。
- Prompt 由 `SystemMessage`、`UserMessage` 和 `AssistantMessage` 组成，可手动构造多轮上下文。

这种方式更接近模型层，适合需要直接处理 `ChatResponse`、消息角色或流式响应元数据的场景。

### 4.2 `ChatClient`：面向业务的调用接口

项目中的主要调用链是：

```java
chatClient.prompt()
        .user(message)
        .call()
        .content();
```

常用操作包括：

- `prompt(String)`：直接传入用户提示词。
- `user(String)`：设置用户消息。
- `system(String)`：设置系统角色指令。
- `call().content()`：同步获取文本。
- `stream().content()`：以 `Flux<String>` 获取文本增量。
- `call().entity(Book.class)`：把模型结果转换为 Java Bean。
- `tools(...)`：给本次请求注册工具。
- `advisors(...)`：设置会话 ID 等 Advisor 参数。

## 5. 提示词模板和提示词工程

### 5.1 PromptTemplate

`PromptTemplateController` 使用模板变量生成提示词：

```text
请给我推荐几个关于{topic}的开源项目
```

接口：

| 接口 | 说明 |
| --- | --- |
| `GET /template/network?topic=...` | 模板替换后同步调用，返回完整文本 |
| `GET /template/network2?topic=...` | 模板替换后流式调用，返回 `Flux<String>` |

该控制器自行构建 `ChatClient`，仅设置 `temperature=0.7`，没有配置日志 Advisor 或会话记忆 Advisor。

### 5.2 简单提示词工程

`PromptEngineerController` 的 `GET /prompt/engineer/call?message=...` 会在用户输入前加入“请你以 json 格式输出内容”的指令，然后通过流式方式返回结果。

这里属于提示词约束示例，并没有使用 JSON Schema、Bean 输出转换器或服务端 JSON 校验，因此模型返回内容仍可能不是严格可解析 JSON。

## 6. 流式输出和 SSE

`StreamController` 中与大模型直接相关的是 `GET /stream/client?message=...`：

1. 使用 `ChatClient.prompt().user(message).stream().content()` 调用模型。
2. 每个文本增量先进行 percent 编码，避免 SSE 的 `data:` 解析规则丢失开头空格，特别是英文代码中的空格。
3. 结束时追加 `[DONE]` 哨兵。
4. 前端收到 `[DONE]` 后主动关闭 `EventSource`，避免浏览器自动重连造成重复请求。
5. 前端累积完整 Markdown，再使用 `marked` 转 HTML，并用 `DOMPurify` 清理后渲染。

静态页面位于 `src/main/resources/static/index.html`，打开模块根地址即可进入示例页面。

`/stream/sse`、`/stream/entity` 和 `/stream/flux` 只是通用流式传输或模拟数据示例，不会调用大模型。

## 7. 结构化输出

实体类 `Book` 包含 `name`、`desc`、`publisher` 三个字段。

`StructOutPutController` 提供两种方式：

| 接口 | 实现方式 | 返回结果 |
| --- | --- | --- |
| `GET /struct/stream` | 使用 `BeanOutputConverter<Book>` 生成格式要求，再通过 `PromptTemplate` 注入提示词 | 返回模型原始文本；方法名含 stream，但实现是同步 `call()` |
| `GET /struct/note` | `call().entity(Book.class)` | Spring AI 将模型结果转换为 `Book` 后返回 `toString()` |

`BeanOutputConverter` 负责把 Java Bean 的格式要求放入 Prompt；它并不等同于服务端强校验。生产场景仍建议对转换失败、字段缺失和模型输出不符合格式的情况做异常处理。

## 8. 会话记忆

### 8.1 手动构造历史消息

`GET /memory/call` 和 `GET /memory/stream` 直接把系统消息、用户消息、助手消息放入 `List<Message>`，再构造 `Prompt` 调用模型。这是“把历史对话手动放回 Prompt”的示例，不会自动从数据库读取历史。

### 8.2 ChatClient Advisor 记忆

`chatClientBySelf` 配置了：

```java
MessageChatMemoryAdvisor.builder(chatMemory).build()
```

`GET /memory/client` 和 `GET /jdbc/memory/streamDb` 通过：

```java
.advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
```

把请求关联到指定会话。Advisor 会在调用前读取历史消息，并在调用后保存本轮消息。

### 8.3 JDBC 持久化

`application.yml` 配置了：

```yaml
spring:
  ai:
    chat:
      memory:
        repository:
          jdbc:
            platform: mysql
            initialize-schema: always
```

因此当前目标是使用 MySQL 保存 Spring AI 的会话记忆。数据库连接指向 `llm_spring`，启动时允许自动初始化记忆表。

## 9. Tool Calling / Function Calling

模块演示了两种工具注册形式。

### 9.1 `@Tool` 工具

`TimeTools.getTimeByZoneId` 使用：

```java
@Tool(name = "getTimeByZoneId", description = "Get time by zone id")
```

`GET /tools/chat?query=...` 通过：

```java
chatClient1.prompt()
        .tools(new TimeTools())
        .user(query)
        .call()
        .content();
```

模型可根据用户问题决定是否调用该方法。方法接收时区 ID，例如 `Asia/Shanghai`，使用 Java `ZoneId` 获取当前时间并返回格式化字符串。

### 9.2 Function Bean

`FunctionCallConfiguration` 声明了一个名为 `getTimeFunction` 的 `Function<TimeService.Request, TimeService.Response>` Bean，并通过 `@Description`、`@JsonPropertyDescription` 描述工具和参数。

当前控制器实际启用的是 `new TimeTools()` 方式；按函数名调用的代码 `toolNames("getTimeFunction")` 仍是注释状态。因此 Function Bean 是已定义的演示能力，但不是当前 `/tools/chat` 的实际注册路径。

## 10. 接口总览

| 路径 | 大模型能力 | 调用形式 |
| --- | --- | --- |
| `/template/network` | PromptTemplate + 同步回答 | `ChatClient.call()` |
| `/template/network2` | PromptTemplate + 流式回答 | `ChatClient.stream()` |
| `/prompt/engineer/call` | 提示词约束 + 流式回答 | `ChatClient.stream()` |
| `/struct/stream` | BeanOutputConverter 格式提示 | `ChatClient.call()` |
| `/struct/note` | Java Bean 结构化转换 | `call().entity(Book.class)` |
| `/memory/call` | 手动历史消息 + 同步回答 | `ChatModel.call()` |
| `/memory/stream` | 手动历史消息 + 流式回答 | `ChatModel.stream()` |
| `/memory/client` | ChatMemory Advisor + 会话 ID | `ChatClient.stream()` |
| `/jdbc/memory/streamDb` | JDBC 会话记忆 + 会话 ID | `ChatClient.stream()` |
| `/tools/chat` | Tool Calling 获取时区时间 | `ChatClient.tools()` |
| `/stream/client` | SSE 大模型流式回答 | `ChatClient.stream()` |

## 11. 当前实现的注意事项

1. `application.yml` 中直接写入了 DashScope API Key、数据库地址、用户名和密码。应立即轮换已经暴露的密钥，并改为环境变量或外部配置注入，例如 `${DASHSCOPE_API_KEY}`、`${DB_PASSWORD}`。
2. `initialize-schema: always` 适合示例环境，不建议直接用于生产部署；生产环境应使用版本化数据库迁移。
3. 多处接口使用 GET 传递完整用户问题，正式应用应评估 URL 长度、访问日志泄露和敏感内容留存问题。
4. 流式接口没有展示统一的异常事件格式；客户端需要同时处理模型调用异常、网络中断和 `[DONE]`。
5. `ChatModelConfiguration` 中的 `chatClientBySelf` 带记忆，`chatClient1` 不带记忆；使用者需要根据业务场景选择正确 Bean，避免把本应隔离的会话混用。
6. 当前提示词工程只要求模型输出 JSON，没有严格结构校验；对外接口不应直接把模型原文当作可信 JSON 或业务指令执行。
7. 工具方法中的 `ZoneId.of` 对非法时区会抛异常，生产代码应增加参数校验和错误返回。

## 12. 一次请求的典型链路

以 `/jdbc/memory/streamDb` 为例：

```text
HTTP 请求携带 message、chatId
        |
        v
ChatClient.prompt().user(message)
        |
        v
MessageChatMemoryAdvisor 根据 conversation_id 读取历史
        |
        v
DashScope ChatModel 调用 qwen-plus
        |
        v
stream().content() 持续产生文本增量
        |
        v
Advisor 保存本轮用户消息和助手消息到 JDBC
```

这个模块的核心价值是展示 Spring AI 在模型供应商之上提供的统一编程模型：业务代码主要依赖 `ChatClient`、`Prompt`、`ChatMemory` 和 Tool/Converter 抽象，模型供应商由 starter 和配置负责接入。

## 13. 原理深入：Spring AI 的分层模型

### 13.1 Spring AI 解决的不是“模型推理”，而是调用协议统一

大模型真正的推理仍然发生在 DashScope 服务端。Spring AI 主要负责把 Java 对象转换为模型服务需要的请求，把供应商响应转换为统一的 Java 对象，并补充记忆、工具、日志、重试等调用周边能力。

可以把一次调用拆成四层：

```text
业务层：Controller / Service
    使用 ChatClient.prompt()、call()、stream()
        |
Spring AI 客户端层：Prompt、Message、Advisor、OutputConverter、Tool
    组装一次 ChatModel.Request
        |
模型抽象层：ChatModel
    提供 call(Prompt) 和 stream(Prompt)
        |
供应商适配层：DashScope ChatModel
    序列化为 DashScope 请求，调用 HTTP API，解析响应
```

因此业务代码依赖 `ChatModel` 接口时，可以在 DashScope、OpenAI 兼容服务或其他实现之间切换；真正需要调整的是 starter、配置和供应商专用的 `ChatOptions`。本模块仍在多个控制器中直接使用 `DashScopeChatOptions`，所以“模型接口可替换”与“选项完全可移植”是两件事。

### 13.2 `ChatModel`、`ChatClient`、`Prompt` 的职责边界

`ChatModel` 是最接近供应商适配器的一层：

- 输入是 `Prompt`，其中包含有序的 `Message` 列表和模型选项。
- `call()` 返回完整的 `ChatResponse`，通常包含一个或多个生成结果、文本、finish reason、token usage 等元数据。
- `stream()` 返回 `Flux<ChatResponse>`，每个元素通常只携带一段增量文本或一段响应事件。

`ChatClient` 是面向业务的门面。它把链式调用暂存在一个请求规范对象中，直到执行 `call()` 或 `stream()` 才真正触发模型请求。因此：

```java
chatClient.prompt().user(message)
```

只是构造请求，不会访问网络；

```java
chatClient.prompt().user(message).call()
```

才会触发一次同步调用；

```java
chatClient.prompt().user(message).stream()
```

才会创建响应流。`content()` 只是从统一响应中提取文本，调用方因此不需要重复处理 `ChatResponse -> result -> output -> text` 的对象层级。

`Prompt` 和 `Message` 描述“发送给模型的上下文”，不是数据库中的会话，也不是模型内部记忆。模型每次只看到本次请求中最终组装出来的消息；所谓多轮对话，本质是下一次请求再次携带了之前的消息。

### 13.3 Spring Boot 自动装配发生了什么

启动过程可以按下面的顺序理解：

1. `spring-ai-alibaba-starter-dashscope` 引入 DashScope 的自动配置类和实现类。
2. 自动配置读取 `spring.ai.dashscope.api-key`、模型名等属性。
3. 在条件满足且容器中没有冲突实现时，创建 DashScope `ChatModel` Bean。
4. Spring AI 的 ChatClient 自动配置提供 `ChatClient.Builder`。
5. `ChatModelConfiguration` 使用注入的 `ChatModel` 手动创建两个业务级 `ChatClient`。
6. Controller 再注入 `ChatClient` 或 `ChatModel`，请求到达时才执行模型调用。

`ChatClient.Builder` 不是模型连接池，也不是模型实例的第二份副本。它保存默认模型、默认选项和默认 Advisor，`build()` 后得到一个轻量的调用门面。多个 `ChatClient` 实际上可以共享同一个 `ChatModel`，但它们的默认 Advisor 和选项可以不同。

当前项目同时声明了 `chatClientBySelf` 和 `chatClient1`。部分类使用 `@Resource` 按名称注入，部分类使用 `@Autowired`；Spring 在按类型找到多个 Bean 时会使用字段名作为候选名称回退。为了避免后续新增客户端后启动失败，生产代码应改成构造器注入并显式标注 `@Qualifier`。

需要特别注意，`StreamController` 源码注释中仍写着“现在是 OpenAI starter -> DeepSeek”，但当前 `pom.xml` 实际启用的是 DashScope starter，`application.yml` 实际配置的是 `qwen-plus`。阅读运行行为时应以依赖和配置为准，不能以这段过期注释为准。

## 14. 原理深入：一次同步请求如何完成

以 `/template/network` 为例，执行过程可以展开为：

```text
topic = "Java"
    |
PromptTemplate.create(Map)
    |  把 {topic} 替换为 Java，生成 UserMessage
    v
ChatClient 请求规范
    |  合并默认 temperature、system message、Advisor
    v
MessageChatMemoryAdvisor（仅 chatClientBySelf 生效）
    |  读取会话历史，拼接到本次 Prompt
    v
ChatModel.call(Prompt)
    |  DashScope 适配器序列化消息与 options
    v
DashScope HTTP API
    |
ChatResponse
    |  ChatClient 提取 content 或转换 entity
    v
Controller 返回 HTTP 响应
```

这里有两个容易混淆的点：

- Prompt 模板替换发生在本地，不会让模型理解 `{topic}` 这种占位符；模型收到的是替换后的普通文本。
- `temperature=0.7` 是随请求发送给模型服务的生成参数，不是 Spring AI 的业务逻辑。它通常影响随机性，但不保证结构化输出一定正确。

同步调用会一直占用当前请求的处理流程，直到供应商返回完整结果或抛出异常。模型超时、网络重试、供应商限流等行为由底层 HTTP 客户端和 Spring AI 的重试配置共同决定；当前项目没有显式配置统一的超时、重试和错误响应协议。

## 15. 原理深入：流式响应、Reactive Streams 与 SSE

### 15.1 `Flux` 不是“把完整字符串切片”

供应商的流式 API 通常使用 HTTP 长连接，把模型生成的增量事件持续写回。DashScope 适配器将这些事件解析成 Spring AI 的 `Flux<ChatResponse>`，`ChatClient.stream().content()` 再把每个响应事件映射为文本增量。

```text
供应商长连接事件
    -> DashScope ChatModel
    -> Flux<ChatResponse>
    -> Flux<String>
    -> Spring MVC 写出响应块
    -> 浏览器 EventSource 收到 message 事件
```

`Flux` 遵循 Reactive Streams 的订阅模型：Controller 返回的是一个尚未完全消费的发布者，Web 层订阅它后才开始产生数据。客户端断开、下游写出失败或取消订阅时，理论上可以沿链路取消上游请求；当前代码没有专门实现取消与资源清理策略，因此生产环境需要验证供应商连接是否会及时释放。

### 15.2 为什么代码要做 percent 编码

SSE 每个事件的字段形如 `data: <value>`。协议解析器会去掉 `data:` 后紧邻的一个空格，而大模型 token 可能本身以空格开头，例如 ` static`。如果原样发送，前端收到的内容可能变成 `publicstaticvoid`。

当前实现的处理是：

```text
模型增量 " static"
    -> URL percent 编码 "%20static"
    -> SSE data 字段传输
    -> 浏览器收到 "%20static"
    -> decodeURIComponent
    -> 还原 " static"
```

`[DONE]` 是应用层自定义哨兵，不是 Spring AI 或 SSE 标准事件。它解决的是浏览器 `EventSource` 在连接关闭后自动重连的问题，但不能替代错误事件、超时事件和客户端取消处理。

### 15.3 “流式”与“结构化输出”的冲突

文本增量可以边到边展示，但 JSON、XML 或 Java Bean 需要等到结构完整后才能解析。`StructOutPutController` 因此使用同步 `call()`，先得到完整文本再执行 `entity(Book.class)`。如果强行对结构化结果流式解析，就必须额外处理半截字符串、未闭合引号、代码围栏和模型重复输出等情况。

## 16. 原理深入：Advisor 为什么可以插入记忆和日志

`Advisor` 可以理解为 ChatClient 请求管线上的拦截器。它不直接替代 `ChatModel`，而是有机会在模型调用前读取或修改请求，在调用后观察或保存响应。

以 `MessageChatMemoryAdvisor` 为例，逻辑可以抽象成：

```text
输入 message + conversationId
    |
读取 ChatMemory 中该 conversationId 的历史
    |
历史消息 + 当前用户消息 -> 最终 Prompt
    |
调用 ChatModel
    |
把当前用户消息和模型回答写回 ChatMemory
```

`ChatMemory.CONVERSATION_ID` 只是 Advisor 使用的请求参数名。它不等于数据库主键，也不会自动从 HTTP Session、登录用户或 Cookie 获取；当前接口必须显式传入 `chatId`。如果两个用户传入相同的 ID，就会共享同一段上下文。

`SimpleLoggerAdvisor` 位于同一条管线中，用来记录请求和响应。它可能记录用户 Prompt、工具参数或模型输出，所以日志级别、脱敏和留存周期都属于数据安全设计的一部分。

`chatClientBySelf` 的默认 Advisor 只会作用于通过这个 Bean 发起的请求。`PromptTemplateController`、`PromptEngineerController` 和 `StreamController` 自行通过 `ChatClient.builder(chatModel)` 创建客户端，因此不会自动获得 `chatClientBySelf` 的会话记忆和日志配置；这也是“注入了同一个 ChatModel”但行为仍可能不同的原因。

## 17. 原理深入：会话记忆与上下文窗口

### 17.1 记忆不是模型永久记忆

模型本身通常是无状态的。Spring AI 的 `ChatMemory` 做的是“在下一次请求中重新拼接历史消息”：

```text
第 1 次：user(A) -> assistant(B)
第 2 次：读取 A、B，再拼接 user(C) -> 发送 A、B、C
```

随着历史增加，请求 token 数会增加，成本和延迟上升，并最终受到模型上下文窗口限制。`MessageWindowChatMemory` 的窗口策略就是在本地裁剪历史；当前代码中它只是注释示例，实际使用的 `ChatMemory` 由自动配置提供。

### 17.2 JDBC 仓库保存什么

JDBC 记忆仓库保存的是会话标识、消息角色、消息内容及必要的消息元数据，而不是模型权重或模型内部状态。应用重启后可以恢复历史，是因为下一次请求又把这些消息读回并放进 Prompt。

`initialize-schema: always` 允许启动时创建 Spring AI 所需表，适合学习和演示。正式环境应使用版本化迁移，并为 `conversation_id` 建立合适索引。还要考虑历史消息的敏感数据、保留期限和并发写入顺序。

### 17.3 并发与一致性

同一个 `chatId` 同时收到两个请求时，如果两个请求都先读历史、再分别调用模型、最后写回结果，可能出现回答交错或顺序颠倒。当前代码没有按会话加锁或版本控制。需要严格顺序的聊天业务应按会话串行化请求，或在存储层增加版本/时间序列校验。

## 18. 原理深入：Tool Calling 的真实交互协议

工具调用不是“模型直接执行 Java 方法”，而是模型输出结构化的工具意图，由应用程序执行后再把结果交回模型。典型流程如下：

```text
第 1 次模型请求：用户问题 + 工具名称/描述/参数 Schema
        |
第 1 次模型响应：tool_call(name=getTimeByZoneId, args={zoneId: ...})
        |
Spring AI 根据工具注册表找到 Java 方法并执行
        |
工具结果：2026-10-14 20:00:00 CST
        |
第 2 次模型请求：原消息 + tool_call + tool_result
        |
第 2 次模型响应：面向用户的自然语言答案
```

因此 `ToolsCallController` 的一次 `call()` 可能产生两次或更多次模型请求，延迟和费用不能只按一次 HTTP 接口调用估算。模型只负责选择工具和生成参数，权限、参数校验、超时、幂等、审计和异常隔离必须由应用程序负责。

`@Tool` 方式通过方法和注解生成工具定义；Function Bean 方式通过函数类型、参数记录和描述生成工具定义。两者最终都要转换成模型供应商理解的名称、描述和 JSON Schema。当前 `TimeTools` 直接用 `ZoneId.of()`，非法参数会在工具执行阶段抛异常，当前代码没有把它转换成统一的工具错误结果。

工具结果不会自动成为最终答案。它只是新的上下文，模型仍需要决定如何解释结果并生成最终文本；如果工具结果是高风险操作的执行结果，不能只依赖模型的自然语言复述作为审计依据。

## 19. 原理深入：结构化输出到底保证了什么

`BeanOutputConverter<Book>` 的核心作用是根据 Java 类型生成一段格式说明，并把说明放进 Prompt。`call().entity(Book.class)` 则在完整响应返回后，尝试把文本解析成 `Book`。

```text
Java Class Book
    -> 格式说明/字段信息
    -> 拼接到 Prompt
    -> 模型按提示生成文本
    -> 输出转换器解析文本
    -> Book 实例
```

这个过程默认是“提示约束 + 客户端解析”，不是数学意义上的类型安全。以下情况仍可能失败：

- 模型输出 Markdown 代码围栏或额外解释文字。
- 字段名称拼写错误、字段缺失或类型不匹配。
- 模型在流式中途输出了不完整 JSON。
- 内容虽然能解析，但不满足业务规则，例如书名为空。

因此结构化输出至少要分三层校验：语法解析、字段约束、业务规则。若下游要执行敏感操作，还应增加人工确认或服务端白名单校验。

## 20. 当前模块没有使用的能力边界

依赖或注释中出现 `embedding`、`rerank`、`image`、`audio`、`video` 等自动配置名称，并不代表模块已经实现这些业务能力。当前源码没有发现：

- 文档切分、Embedding 向量化和向量库写入
- 相似度检索、RAG 上下文注入和 Rerank
- 图片、语音、视频模型调用
- Agent 状态机或多 Agent 编排

当前模块的“记忆”是按会话保存聊天消息，和 RAG 的知识库检索不是同一件事：前者解决“记住本次对话”，后者解决“从外部资料检索事实”。不要因为引入了 Memory 依赖就把它描述成向量记忆或知识库。

## 21. 从原理到生产实现的检查项

1. **模型接入**：将 API Key、数据库密码改为环境变量或密钥管理服务；为模型调用设置连接、读取和整体超时。
2. **Bean 管理**：为不同 `ChatClient` 使用明确的 Bean 名称和 `@Qualifier`，避免增加模型或客户端后出现注入歧义。
3. **Prompt 管理**：模板变量做长度和内容校验，避免把未经处理的外部文本直接拼进系统指令；对固定任务记录 Prompt 版本。
4. **流式协议**：统一正常结束、业务错误、模型错误、客户端取消和超时事件，不要只依赖 `[DONE]`。
5. **记忆一致性**：限制历史窗口，控制 token 成本；对同一会话的并发请求做串行化或版本校验。
6. **工具安全**：工具白名单、参数校验、执行超时、权限检查和审计日志必须在 Java 侧完成，不能交给模型决定。
7. **结构化结果**：解析失败要可重试或返回明确错误；解析成功后仍要执行字段和业务规则校验。
8. **可观测性**：记录模型、耗时、token usage、finish reason、错误类型和 trace id，同时对 Prompt、记忆和工具参数脱敏。
