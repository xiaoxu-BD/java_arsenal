# chunk-upload-demo

最小化的文件分片上传 Demo。**所有上传逻辑在后端**（分片大小、总片数、分片完整性校验、断点续传进度、合并与总大小校验），前端 TS 只负责按后端下发的参数切片、传输和展示进度。

后端：Spring Boot 3.2 / Java 17，仅依赖 `spring-boot-starter-web`。
前端：Vite + 原生 TypeScript，零 UI 框架。

## 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/upload/init` | body `{fileName, fileSize}`，返回 `{uploadId, chunkSize, totalChunks}`，分片策略由后端决定 |
| GET | `/api/upload/{uploadId}/status` | 返回已上传分片列表 + 分片策略，断点续传用 |
| POST | `/api/upload/{uploadId}/chunk/{index}` | multipart 字段 `file`，上传单个分片，后端校验分片大小 |
| POST | `/api/upload/{uploadId}/merge` | 校验分片齐全后按序合并，校验总大小，清理分片 |

## 运行

后端（默认端口 8090）：

```bash
mvn spring-boot:run -pl chunk-upload-demo
```

前端（默认端口 5173，`/api` 已代理到 8090）：

```bash
cd chunk-upload-demo/frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173 ，选文件点上传即可。

## 断点续传玩法

上传中途可以在后端控制台 Ctrl+C 或直接刷新页面，然后重新选择**同一个文件**点上传——
前端会用 localStorage 里记住的 uploadId 调 `status` 接口，跳过已传分片继续传。
（demo 的任务状态在内存里，后端重启后任务会失效，前端会自动重新初始化。）

## 目录

```
src/main/java/org/xiaoxu/chunkupload/
├── ChunkUploadApplication.java       # 启动类
├── controller/ChunkUploadController  # 4 个接口
├── controller/GlobalExceptionHandler # 参数错误 400 / 状态冲突 409
├── service/ChunkUploadService        # 核心逻辑全在这
└── dto/                              # record 定义的请求响应
frontend/                             # Vite + TS 前端
```

## 生产化要补的东西（demo 有意省略）

- 任务状态存内存 ConcurrentHashMap，多实例部署要换 Redis / DB
- 没有 MD5/SHA-256 整体校验与秒传（init 时带文件哈希查重）
- 没有登录鉴权，uploadId 谁拿到谁就能续传
- 没有过期任务的定时清理
- 分片落地本地磁盘，量大了要换对象存储（OSS/S3 的 multipart upload）
