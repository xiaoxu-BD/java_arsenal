# chunk-upload-demo

最小化的文件分片上传 Demo。**所有上传逻辑在后端**（分片大小、总片数、分片完整性校验、断点续传进度、合并与总大小校验），前端 TS 只负责按后端下发的参数切片、传输和展示进度。

后端：Spring Boot 3.2 / Java 17，仅依赖 `spring-boot-starter-web` + `aliyun-sdk-oss`。
前端：Vite + 原生 TypeScript，零 UI 框架。

包含两套同构实现，页面上勾选切换：

| 模式 | 接口前缀 | 存储 |
|---|---|---|
| 本地磁盘 | `/api/upload` | `E:/chunk-upload-demo` |
| 阿里云 OSS | `/api/oss-upload` | OSS multipart upload |

## 接口（两套一一对应，OSS 版多一个 DELETE）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `{base}/init` | body `{fileName, fileSize}`，返回 `{uploadId, chunkSize, totalChunks}`，分片策略由后端决定 |
| GET | `{base}/{uploadId}/status` | 返回已上传分片列表 + 分片策略，断点续传用 |
| POST | `{base}/{uploadId}/chunk/{index}` | multipart 字段 `file`，上传单个分片，后端校验分片大小 |
| POST | `{base}/{uploadId}/merge` | 校验分片齐全后合并，返回最终文件地址 |
| DELETE | `/api/oss-upload/{uploadId}` | 仅 OSS 版：abort，未 complete 的分片会持续占存储计费，取消必须调 |

OSS 版的关键规则：partNumber 从 1 开始（对外仍是 0 基下标，服务端 +1）；每片除最后一片外不小于 100KB（demo 固定 5MB）；complete 必须携带全部 PartETag 且按 partNumber 升序。

## 运行

后端（默认端口 8090），**推荐用脚本**，敏感配置全走 `.env`：

```bash
# 第一次：复制模板并填入真实值（.env 已被 git 忽略，不会提交）
cp chunk-upload-demo/.env.example chunk-upload-demo/.env

# 以后启动只要一句（Git Bash）
bash chunk-upload-demo/run-backend.sh
# 或 CMD / 资源管理器双击
chunk-upload-demo\run-backend.cmd
```

脚本会自动做三件事：把 `.env` 里的 KEY=VALUE 注入环境变量、把 JAVA_HOME 覆盖到 JDK 17（本机默认指向 jdk8，换机器改脚本里那一行路径）、然后 `mvn spring-boot:run`。换凭据改 `.env` 重启即可。

不想用脚本也可以手动：

```bash
export OSS_ENDPOINT=... OSS_ACCESS_KEY_ID=... ...
mvn spring-boot:run -pl chunk-upload-demo
```

前端（默认端口 5173，`/api` 已代理到 8090）：

```bash
cd chunk-upload-demo/frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173 ，选文件点上传即可（要传 OSS 就勾上勾选框）。

## 断点续传玩法

上传中途可以在后端控制台 Ctrl+C 或直接刷新页面，然后重新选择**同一个文件**点上传——
前端会用 localStorage 里记住的 uploadId 调 `status` 接口，跳过已传分片继续传。
（demo 的任务状态在内存里，后端重启后任务会失效，前端会自动重新初始化。）

## 目录

```
src/main/java/org/xiaoxu/chunkupload/
├── ChunkUploadApplication.java        # 启动类
├── controller/ChunkUploadController   # 本地磁盘版 4 个接口
├── controller/OssChunkUploadController # OSS 版接口（多一个 abort）
├── controller/GlobalExceptionHandler  # 参数错误 400 / 状态冲突 409
├── service/ChunkUploadService         # 本地磁盘版核心逻辑
├── service/OssChunkUploadService      # OSS 分片上传（initiate/uploadPart/complete）
└── dto/                               # record 定义的请求响应
frontend/                              # Vite + TS 前端
```

## 生产化要补的东西（demo 有意省略）

- 任务状态存内存 ConcurrentHashMap，多实例部署要换 Redis / DB
- 没有 MD5/SHA-256 整体校验与秒传（init 时带文件哈希查重）
- 没有登录鉴权，uploadId 谁拿到谁就能续传
- 没有过期任务的定时清理
- 分片落地本地磁盘，量大了要换对象存储（OSS/S3 的 multipart upload）
