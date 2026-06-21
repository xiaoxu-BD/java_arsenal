# Flowable 审批工作流 — 详细技术文档

> **项目**：permission-system
> **目的**：帮助理解、调试、扩展审批流程

---

## 一、整体架构总览

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                          │
│  提交申请 → 查看待办 → 审批/驳回 → 查看流程图 + 审批历史       │
└───────────────────────┬─────────────────────────────────────┘
                        │ HTTP API
┌───────────────────────▼─────────────────────────────────────┐
│                     Controller 层                            │
│  FlowableController    FulfillmentOrderController            │
│  ApproveLeaveController  ProcessDefinitionController         │
└───────────────────────┬─────────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────────┐
│                      Service 层                              │
│  FlowableService (核心)   FulfillmentOrderService            │
│  ApproveLeaveService      ApproverService                    │
│  WorkflowIdentityService  AnnouncementService                │
└───────┬───────────────────┬──────────────────┬──────────────┘
        │                   │                  │
┌───────▼───────┐  ┌────────▼────────┐  ┌──────▼──────────────┐
│ Flowable 引擎 │  │ ApprovalHandler │  │  RBAC 系统          │
│ RepositorySvc │  │ Registry (策略)  │  │  UserMapper         │
│ RuntimeSvc    │  │ Leave Handler   │  │  RoleMapper         │
│ TaskSvc       │  │ Fulfill Handler │  │  UserRoleMapper     │
│ HistorySvc    │  └─────────────────┘  └─────────────────────┘
└───────┬───────┘
        │
┌───────▼─────────────────────────────────────────────────────┐
│                    数据库 (MySQL)                            │
│ ACT_RE_* (部署)  ACT_RU_* (运行时)  ACT_HI_* (历史)          │
│ fulfillment_order  approve_leave  system_users/role/...      │
└─────────────────────────────────────────────────────────────┘
```

---

## 二、核心概念速查

### 2.1 Flowable 引擎的四大 Service

| Service | 作用 | 典型方法 |
|---------|------|---------|
| `RepositoryService` | BPMN 文件部署、流程定义查询 | `createDeployment()`, `createProcessDefinitionQuery()` |
| `RuntimeService` | 启动流程、管理运行中的流程实例 | `startProcessInstanceByKey()`, `createProcessInstanceQuery()` |
| `TaskService` | 查询任务、认领、完成、评论 | `createTaskQuery()`, `claim()`, `complete()`, `addComment()` |
| `HistoryService` | 查询历史记录 | `createHistoricActivityInstanceQuery()` |

### 2.2 关键术语

| 术语 | 说明 |
|------|------|
| **ProcessDefinition** | 流程定义（BPMN XML 部署后生成），如 `leave-request:1:12345` |
| **ProcessInstance** | 流程实例（一次具体的审批），运行时存在，结束后从运行时表删除 |
| **Task** | 用户任务节点，当前等待某人处理的任务 |
| **candidateGroups** | 候选组（角色编码），如 `ROLE_MANAGER`，该角色下所有人可见此任务 |
| **assignee** | 任务处理人（认领后设置），如 `manager` |
| **businessKey** | 业务键，关联到具体业务单据，如 `ORD-20240101-001`（履约单号） |
| **variables** | 流程变量，控制网关走向，如 `{approved: true, days: 5}` |

---

## 三、两个审批流程详解

### 3.1 履约审批流程 (`fulfillment-approval`)

```
开始 → 经理审批 → 总监审批 → VP审批 → 审批通过(发邮件)
          │           │          │
        驳回         驳回       驳回
          │           │          │
          └───────────┴──────────┘
                     ↓
               审批驳回(发邮件)
```

**三级审批**：每一级都可以通过或驳回，驳回直接结束流程。

**businessKey**：履约单编号（orderNo），如 `ORD-20240620-001`

### 3.2 请假审批流程 (`leave-request`)

```
开始 → 主管审批 → [天数判断]
                     │
              ≤3天 ──┤── >3天
              │           │
         HR备案    总监审批 → HR备案
              │           │
              └─────┬─────┘
                    ↓
              审批通过(发邮件)

主管/总监 任意一级驳回 → 审批驳回(发邮件)
```

**条件分支**：请假 ≤3 天跳过总监，直接到 HR 备案。

**businessKey**：`用户名:identifier`，如 `employee:a1b2c3d4`

### 3.3 BPMN XML 关键属性

```xml
<!-- 用户任务：候选组（角色编码） -->
<userTask id="managerApproval" 
          flowable:candidateGroups="ROLE_MANAGER"/>

<!-- 排他网关条件：通过 -->
<sequenceFlow sourceRef="gateway" targetRef="nextTask">
  <conditionExpression>${approved == true}</conditionExpression>
</sequenceFlow>

<!-- 排他网关条件：驳回 -->
<sequenceFlow sourceRef="gateway" targetRef="rejectedEnd">
  <conditionExpression>${approved == false}</conditionExpression>
</sequenceFlow>

<!-- 结束事件：挂载邮件监听器 -->
<endEvent id="approvedEnd">
  <extensionElements>
    <flowable:executionListener 
      class="org.xiaoxu.workflow.listener.ApprovalEmailListener" 
      event="end"/>
  </extensionElements>
</endEvent>
```

---

## 四、完整审批流程时序（以履约单为例）

### 4.1 提交申请

```
1. employee 创建履约单（状态=草稿）
   POST /api/fulfillment-orders

2. employee 提交审批
   POST /api/tasks/submit/{orderId}
   → FulfillmentOrderService.submitApproval()
   → FlowableService.startProcess("fulfillment-approval", orderNo, "employee", null)
   → Flowable 引擎创建流程实例，第一个任务 = 经理审批
   → 履约单状态更新为 PROCESSING
```

### 4.2 经理查看待办

```
3. manager 登录，查看待办
   GET /api/tasks/my
   → FlowableService.queryMyTasks("manager")
   → ① 查已认领任务：taskAssignee("manager")
   → ② 查候选组任务：先查 manager 的角色 [ROLE_MANAGER]
                    再查 candidateGroupIn([ROLE_MANAGER]).taskUnassigned()
   → 合并返回
```

### 4.3 经理审批通过

```
4. manager 点击「通过」
   POST /api/tasks/{taskId}/approve  body: { comment: "同意" }
   → FlowableService.completeAndCallback(taskId, "manager", true, "同意")
   
   completeAndCallback 内部执行：
   ┌──────────────────────────────────────────────────┐
   │ ① 提取路由信息（complete 之前）                     │
   │    processDefinitionKey = "fulfillment-approval"  │
   │    businessKey = "ORD-20240620-001"               │
   │    processInstanceId = "xxx"                      │
   │                                                   │
   │ ② 自动认领（如果未认领）                            │
   │    claimIfUnassigned(taskId, "manager")           │
   │                                                   │
   │ ③ 写入审批意见到历史表                              │
   │    taskService.addComment(taskId, instanceId,     │
   │                            "同意")                 │
   │                                                   │
   │ ④ 记录任务名称（complete 前查）                     │
   │    taskName = "经理审批"                           │
   │                                                   │
   │ ⑤ 完成任务（传入流程变量）                           │
   │    variables = { approved: true, comment: "同意" } │
   │    completeTask(taskId, variables)                │
   │    → Flowable 引擎根据网关条件流转到总监审批          │
   │                                                   │
   │ ⑥ 记录审计日志                                      │
   │    auditLogService.recordWorkflowLog(...)          │
   │                                                   │
   │ ⑦ 注册事务提交后回调                                 │
   │    TransactionSynchronization.afterCommit()        │
   └──────────────────────────────────────────────────┘
   
   事务提交后：
   → approved=true，检查流程是否结束（isProcessFinished）
   → 如果还有下一级（总监），流程未结束，不触发 onApproved
   → 如果是最后一级（VP 通过），流程结束，触发：
     ApprovalHandlerRegistry.get("fulfillment-approval")
     → FulfillmentApprovalHandler.onApproved(ctx)
     → 更新履约单状态 = APPROVED
```

### 4.4 审批通过 → 邮件通知

```
流程走到 approvedEnd 结束事件时：
   → BPMN 上的 executionListener 触发
   → ApprovalEmailListener.notify()
   → 从 execution 解析 processDefinitionKey + businessKey
   → 查履约单 + 查申请人邮箱
   → 发送 HTML 邮件
   → 标记 notified = '1'
   
   如果邮件发送失败：
   → 仅记录日志，不阻塞流程结束
   → XXL-Job 定时扫描 notified='0' 的记录，补发邮件
```

### 4.5 审批驳回

```
任意一级驳回（approved=false）：
   → completeAndCallback(taskId, username, false, "驳回理由")
   → 网关条件 ${approved == false} 命中
   → 流程直接走到 rejectedEnd
   → ① RejectionEmailListener 触发，发驳回邮件
   → ② afterCommit 回调：handler.onRejected()
   → 更新履约单状态 = REJECTED
```

---

## 五、核心组件详解

### 5.1 FlowableServiceImpl — 工作流核心

文件：`workflow/service/impl/FlowableServiceImpl.java`

**最关键的方法：`completeAndCallback()`**

```java
@Transactional
public void completeAndCallback(String taskId, String username, boolean approved, String comment) {
    // 1. 提取路由信息（必须在 complete 之前，否则流程实例被删除）
    String processDefinitionKey = getProcessDefinitionKeyByTaskId(taskId);
    String businessKey = getBusinessKeyByTaskId(taskId);
    String processInstanceId = getProcessInstanceIdByTaskId(taskId);

    // 2. 候选组任务 → 自动认领
    claimIfUnassigned(taskId, username);

    // 3. 写审批意见到历史表
    taskService.addComment(taskId, processInstanceId, comment);

    // 4. 完成任务（传入 approved 变量，控制网关走向）
    completeTask(taskId, { approved, comment });

    // 5. 记录审计日志
    auditLogService.recordWorkflowLog(...);

    // 6. 注册事务提交后回调
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
            // 事务提交后才触发业务回调（避免事务回滚后状态不一致）
            ApprovalHandler handler = registry.get(processDefinitionKey);
            if (approved && isProcessFinished(processInstanceId)) {
                handler.onApproved(ctx);  // 仅最后一级通过
            } else if (!approved) {
                handler.onRejected(ctx);  // 驳回立即触发
            }
        }
    });
}
```

**为什么用 `afterCommit`？**
> 如果在事务内直接回调，业务状态更新可能被回滚（比如审批通过更新了履约单状态，但事务还没提交，回调又去查这条记录查不到）。`afterCommit` 保证数据库写入已持久化。

**为什么 `isProcessFinished()` 检查？**
> 三级审批中，第一级经理通过后流程不会结束（还有总监、VP）。只有最后一级 VP 通过后，流程实例才从运行时表删除。`isProcessFinished()` 检查运行时表是否还有这条实例。

---

### 5.2 ApprovalHandler 策略模式

文件：`workflow/approval/`

```
ApprovalHandler (接口)
├── FulfillmentApprovalHandler  → key="fulfillment-approval"
└── LeaveApprovalHandler        → key="leave-request"
```

**ApprovalHandlerRegistry** — 启动时自动收集所有 Handler，按 key 建路由表：

```java
// 启动时
handlers = {
    "fulfillment-approval" → FulfillmentApprovalHandler,
    "leave-request"        → LeaveApprovalHandler
}

// 运行时
handler = registry.get("fulfillment-approval");
// → FulfillmentApprovalHandler
```

**每个 Handler 做什么？**

| 方法 | FulfillmentApprovalHandler | LeaveApprovalHandler |
|------|--------------------------|---------------------|
| `supportProcessDefinitionKey()` | 返回 `"fulfillment-approval"` | 返回 `"leave-request"` |
| `onApproved()` | 查履约单 → 更新状态为 APPROVED | 查请假单 → 更新状态为 APPROVED |
| `onRejected()` | 查履约单 → 更新状态为 REJECTED | 查请假单 → 更新状态为 REJECTED |
| `getBusinessId()` | orderNo 查库 → 返回主键 id | 从 businessKey 解析 identifier → 查库 → 返回 id |

**新增业务流程只需：**
1. 画 BPMN XML，设好 process id 和 candidateGroups
2. 实现 `ApprovalHandler`，标注 `@Component`
3. 不需要改 Controller 或 Service

---

### 5.3 WorkflowIdentityService — RBAC 角色桥接

文件：`workflow/identity/WorkflowIdentityService.java`

```java
// 把系统用户名翻译成 Flowable 候选组
public List<String> getGroupsOf(String username) {
    return roleMapper.getRoleCodesByUsername(username);
    // SELECT r.code FROM system_role r
    //   JOIN system_user_role ur ON ur.role_id = r.id
    //   JOIN system_users u ON u.id = ur.user_id
    //   WHERE u.username = 'manager'
    // → ["ROLE_MANAGER"]
}
```

**BPMN 里写 `candidateGroups="ROLE_MANAGER"`**，Flowable 引擎记录"这个任务的候选组是 ROLE_MANAGER"。

**用户查待办时**：先从 RBAC 查用户有哪些角色，再拿这些角色去问 Flowable "有没有候选组匹配的未认领任务"。

---

### 5.4 ApproverService — 动态审批人解析

文件：`workflow/service/ApproverService.java`

```java
// BPMN 中用表达式调用：
// flowable:assignee="${approverService.resolve('ROLE_MANAGER', initiator)}"

public String resolve(String roleCode, String initiator) {
    List<String> users = userRoleMapper.getUsernamesByRoleCode(roleCode);
    // SELECT u.username FROM system_users u
    //   JOIN system_user_role ur ON ur.user_id = u.id
    //   JOIN system_role r ON r.id = ur.role_id
    //   WHERE r.code = 'ROLE_MANAGER'
    // → ["manager"]
    
    if (users.isEmpty()) return initiator;  // 找不到退回发起人
    return users.get(0);                     // 返回第一个
}
```

**对比 candidateGroups 的区别：**

| 方式 | BPMN | 用户需要认领？ | 多人可见？ |
|------|------|--------------|-----------|
| candidateGroups | `flowable:candidateGroups="ROLE_MANAGER"` | 需要 | 所有该角色用户可见 |
| assignee + 表达式 | `flowable:assignee="${approverService.resolve('ROLE_MANAGER', initiator)}"` | 不需要 | 只有分配到的人可见 |

---

### 5.5 邮件监听器

文件：`workflow/listener/`

| 监听器 | 挂在哪个节点 | 触发时机 | 做什么 |
|--------|------------|---------|--------|
| `ApprovalEmailListener` | approvedEnd | 流程走到通过结束事件 | 查申请人邮箱 → 发通过邮件 → 标记 notified |
| `RejectionEmailListener` | rejectedEnd | 流程走到驳回结束事件 | 查申请人邮箱 → 发驳回邮件 → 标记 notified |

**监听器是 Flowable 引擎实例化的，不是 Spring Bean**，所以用 `SpringContextHolder.getBean()` 获取依赖。

**XXL-Job 兜底**：
```
ApprovalNotifyJobHandler（每5分钟）
  → 查 status IN ('APPROVED','REJECTED') AND notified='0'
  → 逐条补发邮件
  → 标记 notified='1'
```

---

## 六、数据库表说明

### 6.1 Flowable 引擎表

| 前缀 | 说明 | 关键表 |
|------|------|--------|
| `ACT_RE_*` | Repository（部署/定义） | `ACT_RE_DEPLOYMENT`, `ACT_RE_PROCDEF` |
| `ACT_RU_*` | Runtime（运行时） | `ACT_RU_EXECUTION`, `ACT_RU_TASK`, `ACT_RU_VARIABLES` |
| `ACT_HI_*` | History（历史） | `ACT_HI_PROCINST`, `ACT_HI_TASKINST`, `ACT_HI_COMMENT`, `ACT_HI_ACTINST` |
| `ACT_ID_*` | Identity（身份） | 未使用（走系统 RBAC） |
| `ACT_GE_*` | General（通用） | `ACT_GE_BYTEARRAY`（BPMN XML 存储） |

### 6.2 业务表

| 表 | 说明 | 关键字段 |
|----|------|---------|
| `fulfillment_order` | 履约单 | `order_no`(businessKey), `status`, `applicant`, `notified`, `process_inst_id` |
| `approve_leave` | 请假单 | `identifier`(businessKey的一部分), `user_id`, `status`, `notified`, `process_instance_id` |

### 6.3 状态流转

```
ApprovalStatus 枚举：
  DRAFT      → 草稿（刚创建）
  PROCESSING → 审批中（已提交）
  APPROVED   → 已通过（流程结束）
  REJECTED   → 已驳回（流程结束）
  CANCELLED  → 已撤回
```

---

## 七、调试指南

### 7.1 常用 SQL 查询

```sql
-- 查所有流程定义
SELECT * FROM ACT_RE_PROCDEF ORDER BY VERSION_ DESC;

-- 查运行中的流程实例
SELECT * FROM ACT_RU_EXECUTION;

-- 查当前待办任务
SELECT t.ID_, t.NAME_, t.ASSIGNEE_, t.PROC_INST_ID_
FROM ACT_RU_TASK t;

-- 查任务变量（approved 等）
SELECT * FROM ACT_RU_VARIABLE WHERE TASK_ID_ = 'xxx';

-- 查审批历史（含意见）
SELECT t.NAME_, t.ASSIGNEE_, t.END_TIME_, c.MESSAGE_
FROM ACT_HI_TASKINST t
LEFT JOIN ACT_HI_COMMENT c ON c.TASK_ID_ = t.ID_
WHERE t.PROC_INST_ID_ = 'xxx'
ORDER BY t.START_TIME_;

-- 查部署的 BPMN XML
SELECT * FROM ACT_GE_BYTEARRAY WHERE NAME_ LIKE '%.bpmn20.xml';
```

### 7.2 调试断点位置

| 场景 | 断点位置 | 文件 |
|------|---------|------|
| 查看任务如何匹配到用户 | `queryMyTasks()` | FlowableServiceImpl.java |
| 审批通过流程 | `completeAndCallback()` | FlowableServiceImpl.java |
| 业务状态更新 | `onApproved()` / `onRejected()` | FulfillmentApprovalHandler.java |
| 邮件发送 | `notify()` | ApprovalEmailListener.java |
| 角色解析 | `getGroupsOf()` | WorkflowIdentityService.java |
| 审批人解析 | `resolve()` | ApproverService.java |

### 7.3 常见问题排查

| 问题 | 原因 | 解决 |
|------|------|------|
| 用户看不到待办 | candidateGroups 和 role.code 不一致 | 检查 BPMN XML 的角色编码和 system_role.code |
| 审批后状态没更新 | afterCommit 回调异常 | 看日志 `审批回调异常` |
| 邮件没发 | 邮箱没配置 / SMTP 连不上 | 检查 application.yml 的 mail 配置 |
| 流程不走下一步 | approved 变量没传对 | 检查 completeTask 的 variables |
| 流程实例查不到 | 流程已结束，从运行时表删除了 | 查 ACT_HI_PROCINST 历史表 |

---

## 八、扩展指南

### 8.1 新增一个审批业务（如报销审批）

**1. 画 BPMN XML**
```xml
<process id="expense-approval" name="报销审批">
  <startEvent id="start"/>
  <userTask id="managerApproval" flowable:candidateGroups="ROLE_MANAGER"/>
  <!-- ... -->
  <endEvent id="approvedEnd">
    <extensionElements>
      <flowable:executionListener 
        class="org.xiaoxu.workflow.listener.ApprovalEmailListener" event="end"/>
    </extensionElements>
  </endEvent>
</process>
```

**2. 实现 ApprovalHandler**
```java
@Component
public class ExpenseApprovalHandler implements ApprovalHandler {
    @Override
    public String supportProcessDefinitionKey() { return "expense-approval"; }
    
    @Override
    public void onApproved(ApprovalContext ctx) {
        // 更新报销单状态为已通过
    }
    
    @Override
    public void onRejected(ApprovalContext ctx) {
        // 更新报销单状态为已驳回
    }
    
    @Override
    public Long getBusinessId(String businessKey) {
        // 返回报销单主键
    }
}
```

**3. 不需要改 Controller / Service / 前端审批页** — 自动接入。

### 8.2 用设计器创建流程

1. 进入「流程设计」→ 新建流程
2. 拖拽节点 → 配置审批人（选角色）→ 配置邮件通知
3. 点击部署 → 流程立即生效
4. 业务代码调用 `startProcess("新流程key", businessKey, username, null)` 启动

---

## 九、文件清单速查

| 层 | 文件 | 说明 |
|----|------|------|
| **BPMN** | `processes/leave-request.bpmn20.xml` | 请假流程定义 |
| **BPMN** | `processes/fulfillment-approval.bpmn20.xml` | 履约流程定义 |
| **接口** | `workflow/service/FlowableService.java` | 核心接口 |
| **实现** | `workflow/service/impl/FlowableServiceImpl.java` | 核心实现（最重要） |
| **控制器** | `workflow/controller/FlowableController.java` | 审批任务 API |
| **策略** | `workflow/approval/ApprovalHandler.java` | 回调接口 |
| **注册** | `workflow/approval/ApprovalHandlerRegistry.java` | 路由注册 |
| **履约回调** | `workflow/approval/FulfillmentApprovalHandler.java` | 履约状态更新 |
| **请假回调** | `workflow/approval/LeaveApprovalHandler.java` | 请假状态更新 |
| **身份桥接** | `workflow/identity/WorkflowIdentityService.java` | RBAC→Flowable |
| **审批人解析** | `workflow/service/ApproverService.java` | 动态分配 |
| **通过邮件** | `workflow/listener/ApprovalEmailListener.java` | 通过通知 |
| **驳回邮件** | `workflow/listener/RejectionEmailListener.java` | 驳回通知 |
| **兜底补发** | `workflow/job/ApprovalNotifyJobHandler.java` | XXL-Job |
| **Bean 工具** | `spring/SpringContextHolder.java` | 监听器获取 Bean |
| **设计器** | `workflow/controller/ProcessDefinitionController.java` | 流程定义管理 |

# 十：各个id的详情说明

processDefineKey  流程模板的名字    ； 写在bpmn.xml的属性id     ； 不会变化，所有的版本都叫这个

processDefinitionId ： 流程模板的版本号标识 ； 部署自动创建：key：版本号：部署id； 同一版本不变，重新部署会加版本号

processInstanceId ： 一次流程的唯一标识； 调用startProcess（）；







# Flowable 常见 ID 对照表

| ID                       | 是什么                 | 谁创建的                                   | 会变吗                             |
| ------------------------ | ---------------------- | ------------------------------------------ | ---------------------------------- |
| **processDefinitionKey** | 流程模板的名字         | 你写在 BPMN 里的 `id` 属性                 | 不变，所有版本都叫这个             |
| **processDefinitionId**  | 流程模板的版本号标识   | 部署时自动生成，格式为 `key:版本号:部署ID` | 同一版本不变，重新部署会增加版本号 |
| **processInstanceId**    | 一次流程运行的唯一标识 | 调用 `startProcess()` 时创建               | 整次流程不变，从开始到结束都是这个 |
| **executionId**          | 一条执行路径的标识     | 流程引擎自动创建                           | 会变，但不一定等于 `taskId`        |
| **taskId**               | 当前审批节点的标识     | 流程推进到审批节点时创建                   | 每经过一个节点都会变化             |
| **businessKey**          | 业务数据标识           | 由业务系统传入                             | 不变，传入什么就一直是什么         |

## 举例

假设有一个请假流程：

- BPMN 中定义：

```xml
<process id="leaveProcess" name="请假流程">
```

则：

| 字段                 | 示例值                 |
| -------------------- | ---------------------- |
| processDefinitionKey | `leaveProcess`         |
| processDefinitionId  | `leaveProcess:3:25004` |
| processInstanceId    | `50001`                |
| executionId          | `50005`                |
| taskId               | `50012`（经理审批）    |
| businessKey          | `LEAVE202606210001`    |

当经理审批完成进入总监审批：

- `processInstanceId`：仍然是 `50001`
- `businessKey`：仍然是 `LEAVE202606210001`
- `taskId`：变成新的任务 ID，例如 `50018`
- `executionId`：可能保持不变，也可能因为并行网关等原因发生变化
