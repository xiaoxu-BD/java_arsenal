# excel-demo

Excel 导入导出演示，基于 **Apache Fesod 2.0.2**（`org.apache.fesod:fesod-sheet`）——就是 EasyExcel → fastexcel 一脉的原班 API，fastexcel 捐给 Apache 孵化后的新名字，旧坐标 `cn.idev.excel:fastexcel` 停更于 1.3.0。三块内容：

| 场景 | 接口前缀 | 特点 |
|---|---|---|
| 同步小表 | `/api/excel` | 模板下载 / 导入 / 导出，一次请求跑完 |
| 异步大表 | `/api/excel/async` | 提交返回 taskId，轮询进度，下载产物 |
| 造数 | `/api/data` | 批量插入随机用户，给大表练习喂料 |

后端：Spring Boot 3.2 / Java 17 / MyBatis-Plus / MySQL。前端：Vite + Vue 3 `<script setup>` + TS，零 UI 框架。数据模型用**两级表头**（基本信息 / 工作信息）；导出比导入多 ID、入库时间两列，读取按表头名匹配自动忽略多出的列，所以**导出的文件可以直接拿去再导入**，错误回执改完也能直接传。

## 接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/excel/template` | 导入模板，部门列带下拉（第 3~10002 行） |
| POST | `/api/excel/import` | 同步导入，multipart 字段 `file`，返回 `{total, successCount, failCount, errors[]}` |
| GET | `/api/excel/export` | 同步导出全量，仅小表用 |
| POST | `/api/excel/async/import` | 异步导入，立即返回任务 |
| POST | `/api/excel/async/export` | 异步导出，游标翻页每页 5000，满 50 万行换 sheet |
| GET | `/api/excel/async/{taskId}` | 轮询进度（导入没有 total，只有 processed 一直涨） |
| GET | `/api/excel/async/{taskId}/file` | 下载产物：导入有错 = 错误回执，导出 = 结果文件 |
| POST | `/api/data/generate?count=100000` | 造数，count ≤ 100 万，返回插入行数 |

## 三层校验（生产导入的标准分层）

| 层 | 检什么 | 在哪 |
|---|---|---|
| 格式层 | 类型转换失败（日期/数字），行数据已丢只能报行号列号 | `UserImportListener.onException` |
| 字段层 | 必填、长度、手机/邮箱正则、部门枚举、薪资范围 | `UserImportListener.checkRow` |
| 业务层 | 文件内手机号去重 + 库内已存在（按批 `IN` 查，不逐行查库） | `invoke` + `flushBuffer` |

策略是**全量收集错误**而不是遇错即停：合法行正常入库，非法行带行号进清单；异步任务结束后非法行写成"错误回执.xlsx"（原数据 + 错误原因列），用户改完可直接回传——多出的错误原因列导入时同样被忽略。

## 运行

MySQL 默认连 `192.168.200.128:3306`（root），可用 `MYSQL_HOST/MYSQL_PORT/MYSQL_DB/MYSQL_USER/MYSQL_PASSWORD` 环境变量覆盖；库和表首次启动自动创建。本机默认 JDK 是 8，先指到 17：

```bash
export JAVA_HOME="D:/dev/jdks/jdk17/jdk-17.0.12/jdk-17.0.12"
mvn spring-boot:run -pl excel-demo
```

一轮闭环玩法：

```bash
# 1. 拿模板手填几行（故意填错几格）传同步接口, 看 errors 清单
curl -O -J http://localhost:8091/api/excel/template
curl -F "file=@用户导入模板.xlsx" http://localhost:8091/api/excel/import

# 2. 造 20 万数据, 走异步导出再轮询
curl -X POST "http://localhost:8091/api/data/generate?count=200000"
curl -X POST http://localhost:8091/api/excel/async/export          # 返回 {id: ...}
curl http://localhost:8091/api/excel/async/{id}                    # 轮询到 DONE
curl -O -J http://localhost:8091/api/excel/async/{id}/file         # 下载产物

# 3. 把导出的 20 万行再异步导入 → 全部"手机号已存在于数据库" → 下载错误回执
curl -F "file=@xxxx-用户导出.xlsx" http://localhost:8091/api/excel/async/import
```

前端（默认端口 5173，`/api` 已代理到 8091）：

```bash
cd excel-demo/frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173 ，单页覆盖全部接口：造数、模板/同步导出下载、同步导入（结果+错误清单表格）、异步导入/导出（任务卡片 + 每秒轮询进度条 + 产物下载）。文件上传走原生 `<input type="file">`。

## 目录

```
src/main/java/org/xiaoxu/exceldemo/
├── ExcelDemoApplication.java            # 启动类
├── controller/ExcelController           # M1 同步: 模板/导入/导出
├── controller/AsyncExcelController      # M2 异步: 提交/轮询/下载
├── controller/DataGenController         # 造数
├── controller/GlobalExceptionHandler    # 参数错误 400 / 状态冲突 409
├── service/ExcelService                 # 同步读写
├── service/ExcelTaskService             # 异步任务(内存任务表 + 2 线程池)
├── service/DataGenService               # 批量造数
├── listener/UserImportListener          # 核心: 三层校验 + 1000 行攒批入库
├── handler/DeptDropdownSheetHandler     # 模板部门列下拉(POI 数据验证)
├── entity/DemoUser + mapper/            # MyBatis-Plus
└── dto/                                 # 导入行/导出行/错误回执行/任务/结果
frontend/                                # Vite + Vue3 + TS 前端
└── src/{App.vue, api.ts, style.css}     # 单页: api.ts 是接口封装, App.vue 全部交互
```

## 已踩/已避的坑

- `ReadListener` 回调叫 `doAfterAllAnalysed`（英式拼写，不是 Invoked）
- POI 的 `CellRangeAddressList` 在 `org.apache.poi.ss.util`，不在 `ss.usermodel`
- fesod-sheet 直接 compile 依赖原生 POI，没有被 shade 重定位，WriteHandler 里可以放心用 `org.apache.poi.*`
- 显式下拉列表有 **255 字符总长上限**，选项多要换隐藏字典 sheet + 公式引用
- 手机号列 11 位数字在常规格式下不会变科学计数法；更长数字列要在模板里预设文本格式
- xlsx 单 sheet 硬上限 1048576 行，导出满 50 万主动换 sheet
- 同步导出直接写 `HttpServletResponse`，若项目有统一响应包装（`ResponseBodyAdvice`）要记得放行，否则二进制流被包成 JSON
- 深分页用 `WHERE id > lastId LIMIT n` 游标，别 `LIMIT offset, n`

## 生产化要补的东西（demo 有意省略）

- 任务表在内存 ConcurrentHashMap，重启即失，多实例要换 DB / Redis
- 没有登录鉴权、并发导出限流、任务取消
- 产物文件没有过期清理（会堆在 `E:/excel-demo`）
- 错误回执行数无上限，全错的百万行导入会整个载入内存
- `fill` 模板填充和 CSV 读写未演示（同一套 API，`writer.fill()` / `.csv().doRead()`）
- Fesod 还在孵化期（-incubating），上生产前确认公司私服策略与版本冻结情况
