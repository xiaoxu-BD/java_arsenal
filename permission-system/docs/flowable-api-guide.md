# Flowable 四大 API 用法梳理

## 一、RepositoryService（仓库服务）

> 管理流程定义的部署、查询、删除

### 1.1 涉及的表

| 表名 | 说明 |
|------|------|
| `ACT_RE_DEPLOYMENT` | 部署信息表 |
| `ACT_RE_PROCDEF` | 流程定义表 |
| `ACT_GE_BYTEARRAY` | 二进制资源表（BPMN XML） |

### 1.2 项目中的用法

#### 部署流程定义
```java
// FlowableServiceImpl.deployProcess()
Deployment deployment = repositoryService.createDeployment()
        .addClasspathResource(bpmnResourcePath)
        .deploy();
```

**SQL 等价：**
```sql
-- 插入部署记录
INSERT INTO ACT_RE_DEPLOYMENT (ID_, NAME_, DEPLOY_TIME_) VALUES (?, ?, NOW());

-- 插入流程定义
INSERT INTO ACT_RE_PROCDEF (ID_, KEY_, NAME_, VERSION_, DEPLOYMENT_ID_) VALUES (?, ?, ?, ?, ?);

-- 插入 BPMN XML 资源
INSERT INTO ACT_GE_BYTEARRAY (ID_, NAME_, BYTES_, DEPLOYMENT_ID_) VALUES (?, ?, ?, ?);
```

#### 查询所有流程定义
```java
// FlowableServiceImpl.getProcessDefinitions()
List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionKey().asc()
        .orderByProcessDefinitionVersion().desc()
        .list();
```

**SQL 等价：**
```sql
SELECT * FROM ACT_RE_PROCDEF 
ORDER BY KEY_ ASC, VERSION_ DESC;
```

#### 获取 BPMN XML
```java
// FlowableServiceImpl.getBpmnXml()
InputStream stream = repositoryService.getResourceAsStream(deploymentId, resourceName);
```

**SQL 等价：**
```sql
SELECT BYTES_ FROM ACT_GE_BYTEARRAY 
WHERE DEPLOYMENT_ID_ = ? AND NAME_ LIKE '%.bpmn20.xml';
```

#### 挂起/激活流程定义
```java
repositoryService.suspendProcessDefinitionById(processDefinitionId);
repositoryService.activateProcessDefinitionById(processDefinitionId);
```

**SQL 等价：**
```sql
UPDATE ACT_RE_PROCDEF SET SUSPENSION_STATE_ = 2 WHERE ID_ = ?;  -- 挂起
UPDATE ACT_RE_PROCDEF SET SUSPENSION_STATE_ = 1 WHERE ID_ = ?;  -- 激活
```

---

## 二、RuntimeService（运行时服务）

> 管理流程实例的启动、查询、删除

### 2.1 涉及的表

| 表名 | 说明 |
|------|------|
| `ACT_RU_EXECUTION` | 流程实例/执行实例表 |
| `ACT_RU_VARIABLE` | 运行时变量表 |
| `ACT_RU_TASK` | 运行时任务表 |

### 2.2 项目中的用法

#### 启动流程实例
```java
// FlowableServiceImpl.startProcess()
ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
        processDefinitionKey, businessKey, variables);
```

**SQL 等价：**
```sql
-- 插入流程实例
INSERT INTO ACT_RU_EXECUTION (ID_, PROC_DEF_ID_, BUSINESS_KEY_, PROC_INST_ID_) VALUES (?, ?, ?, ?);

-- 插入流程变量
INSERT INTO ACT_RU_VARIABLE (ID_, NAME_, TYPE_, DOUBLE_, TEXT_, PROC_INST_ID_) VALUES (?, ?, ?, ?, ?, ?);
```

#### 查询流程实例
```java
// FlowableServiceImpl.isProcessFinished()
ProcessInstance instance = runtimeService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();
```

**SQL 等价：**
```sql
SELECT * FROM ACT_RU_EXECUTION 
WHERE ID_ = ? AND END_TIME_ IS NULL;
```

#### 删除流程实例
```java
// FlowableServiceImpl.deleteProcessInstance()
runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
```

**SQL 等价：**
```sql
UPDATE ACT_RU_EXECUTION SET END_TIME_ = NOW() WHERE PROC_INST_ID_ = ?;
DELETE FROM ACT_RU_VARIABLE WHERE PROC_INST_ID_ = ?;
DELETE FROM ACT_RU_TASK WHERE PROC_INST_ID_ = ?;
```

---

## 三、TaskService（任务服务）

> 管理用户任务的认领、完成、评论

### 3.1 涉及的表

| 表名 | 说明 |
|------|------|
| `ACT_RU_TASK` | 运行时任务表 |
| `ACT_RU_IDENTITYLINK` | 任务候选人/组表 |
| `ACT_HI_COMMENT` | 审批意见表 |

### 3.2 项目中的用法

#### 查询我的待办任务
```java
// FlowableServiceImpl.queryMyTasks()
List<Task> claimed = taskService.createTaskQuery()
        .taskAssignee(assignee)  // 已认领的任务
        .orderByTaskCreateTime().desc()
        .list();

List<Task> candidate = taskService.createTaskQuery()
        .taskCandidateGroupIn(myGroups)  // 候选组任务
        .taskUnassigned()
        .orderByTaskCreateTime().desc()
        .list();
```

**SQL 等价：**
```sql
-- 已认领任务
SELECT * FROM ACT_RU_TASK 
WHERE ASSIGNEE_ = ? 
ORDER BY CREATE_TIME_ DESC;

-- 候选组任务
SELECT * FROM ACT_RU_TASK t
JOIN ACT_RU_IDENTITYLINK i ON t.ID_ = i.TASK_ID_
WHERE i.GROUP_ID_ IN (?, ?, ...) 
AND t.ASSIGNEE_ IS NULL
ORDER BY t.CREATE_TIME_ DESC;
```

#### 认领任务
```java
// FlowableServiceImpl.claimTask()
taskService.claim(taskId, username);
```

**SQL 等价：**
```sql
UPDATE ACT_RU_TASK SET ASSIGNEE_ = ? WHERE ID_ = ?;
```

#### 添加审批意见
```java
// FlowableServiceImpl.completeAndCallback()
taskService.addComment(taskId, processInstanceId, comment);
```

**SQL 等价：**
```sql
INSERT INTO ACT_HI_COMMENT (ID_, TASK_ID_, PROC_INST_ID_, MESSAGE_, TIME_) VALUES (?, ?, ?, ?, NOW());
```

#### 完成任务
```java
taskService.complete(taskId, variables);
```

**SQL 等价：**
```sql
-- 更新任务完成时间
UPDATE ACT_RU_TASK SET END_TIME_ = NOW() WHERE ID_ = ?;

-- 更新流程变量
UPDATE ACT_RU_VARIABLE SET DOUBLE_ = ?, TEXT_ = ? WHERE NAME_ = 'approved' AND TASK_ID_ = ?;
```

---

## 四、HistoryService（历史服务）

> 查询已完成的流程实例、任务、活动

### 4.1 涉及的表

| 表名 | 说明 |
|------|------|
| `ACT_HI_PROCINST` | 历史流程实例表 |
| `ACT_HI_TASKINST` | 历史任务实例表 |
| `ACT_HI_ACTINST` | 历史活动实例表 |
| `ACT_HI_VARINST` | 历史变量表 |
| `ACT_HI_COMMENT` | 审批意见表 |

### 4.2 项目中的用法

#### 查询流程实例历史
```java
// FlowableServiceImpl.isProcessFinished()
HistoricProcessInstance processInstance = historyService
        .createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId)
        .singleResult();
```

**SQL 等价：**
```sql
SELECT * FROM ACT_HI_PROCINST 
WHERE PROC_INST_ID_ = ?;
```

#### 查询审批历史活动
```java
// FlowableServiceImpl.getHistoricActivities()
List<HistoricActivityInstance> activities = historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processInstanceId)
        .orderByHistoricActivityInstanceStartTime().asc()
        .list();
```

**SQL 等价：**
```sql
SELECT * FROM ACT_HI_ACTINST 
WHERE PROC_INST_ID_ = ? 
ORDER BY START_TIME_ ASC;
```

#### 查询审批意见
```java
List<Comment> comments = taskService.getProcessInstanceComments(processInstanceId);
```

**SQL 等价：**
```sql
SELECT * FROM ACT_HI_COMMENT 
WHERE PROC_INST_ID_ = ? 
ORDER BY TIME_ ASC;
```

#### 查询流程变量
```java
historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(processInstanceId)
        .variableName("approved")
        .list();
```

**SQL 等价：**
```sql
SELECT * FROM ACT_HI_VARINST 
WHERE PROC_INST_ID_ = ? AND NAME_ = 'approved';
```

---

## 五、完整流程示例

### 请假审批流程

```
1. 用户提交请假
   ↓
   RuntimeService.startProcessInstanceByKey("leave-request-v2", businessKey, variables)
   → ACT_RU_EXECUTION, ACT_RU_VARIABLE

2. 经理审批
   ↓
   TaskService.claim(taskId, "manager_01")
   → ACT_RU_TASK.ASSIGNEE_ = "manager_01"
   
   TaskService.addComment(taskId, processInstanceId, "同意")
   → ACT_HI_COMMENT
   
   TaskService.complete(taskId, {approved: true})
   → ACT_RU_TASK.END_TIME_ = NOW()
   → ACT_RU_VARIABLE(approved = true)

3. 查询待办
   ↓
   TaskService.createTaskQuery().taskAssignee("manager_01").list()
   → SELECT * FROM ACT_RU_TASK WHERE ASSIGNEE_ = 'manager_01'

4. 查询审批历史
   ↓
   HistoryService.createHistoricActivityInstanceQuery().processInstanceId(pid).list()
   → SELECT * FROM ACT_HI_ACTINST WHERE PROC_INST_ID_ = ?

5. 流程结束
   ↓
   HistoryService.createHistoricProcessInstanceQuery().processInstanceId(pid).singleResult()
   → SELECT * FROM ACT_HI_PROCINST WHERE PROC_INST_ID_ = ?
   → END_TIME_ != NULL 表示流程已结束
```

---

## 六、常用表关系图

```
ACT_RE_PROCDEF (流程定义)
    ↓ 1:N
ACT_RE_DEPLOYMENT (部署)
    ↓ 1:N
ACT_GE_BYTEARRAY (资源)

ACT_RU_EXECUTION (流程实例)
    ↓ 1:N
ACT_RU_TASK (任务)
    ↓ 1:N
ACT_RU_IDENTITYLINK (候选人)
    
ACT_RU_VARIABLE (变量)
    ↓ 关联
ACT_RU_EXECUTION

ACT_HI_PROCINST (历史流程)
ACT_HI_TASKINST (历史任务)
ACT_HI_ACTINST (历史活动)
ACT_HI_VARINST (历史变量)
ACT_HI_COMMENT (审批意见)
```

---

## 七、项目文件对照

| 功能 | 项目文件 | 使用的 API |
|------|---------|-----------|
| 部署流程 | `FlowableServiceImpl.java` | `RepositoryService` |
| 启动流程 | `FlowableServiceImpl.java` | `RuntimeService` |
| 查询待办 | `FlowableServiceImpl.java` | `TaskService` |
| 认领任务 | `FlowableServiceImpl.java` | `TaskService` |
| 审批通过/驳回 | `FlowableServiceImpl.java` | `TaskService` |
| 查询审批历史 | `FlowableServiceImpl.java` | `HistoryService` |
| 查询流程图 | `FlowableServiceImpl.java` | `RepositoryService` + `HistoryService` |
| 删除流程 | `FlowableServiceImpl.java` | `RuntimeService` |
