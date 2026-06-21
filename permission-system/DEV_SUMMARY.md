# BPMN 权限管理系统 — 开发工作总结

> **日期**：2026-06-19 ~ 2026-06-20
> **项目**：permission-system
> **分支**：nft-study

---

## 一、EasyExcel 数据导出

### 新增功能
- 用户列表、角色列表、全部任务列表 一键导出 Excel

### 新建文件
| 文件 | 说明 |
|------|------|
| `file/UserExcelVO.java` | 用户导出数据模型 |
| `file/RoleExcelVO.java` | 角色导出数据模型 |
| `file/WorkflowItemExcelVO.java` | 任务导出数据模型 |
| `common/utils/ExcelExportUtil.java` | 通用导出工具类 |

### 修改文件
| 文件 | 改动 |
|------|------|
| `UserController.java` | 新增 `GET /api/user/export` |
| `RoleController.java` | 新增 `GET /api/role/export` |
| `WorkflowAdminController.java` | 新增 `GET /api/workflow/admin/export` |
| `api/user.ts` | 新增 `exportUserApi` / `exportRoleApi` |
| `api/workflow.ts` | 新增 `exportAdminTasksApi` |
| `views/user/index.vue` | 添加导出按钮、邮箱列、头像列 |
| `views/role/index.vue` | 添加导出按钮 |
| `views/workflow/admin/index.vue` | 添加导出按钮 |

---

## 二、审批邮件通知

### 新增功能
- 审批通过/驳回后自动发送 HTML 邮件通知申请人
- Flowable 监听器为主路径，XXL-Job 定时任务为兜底补发

### 新建文件
| 文件 | 说明 |
|------|------|
| `service/EmailService.java` | 邮件发送服务（支持 HTML 模板） |
| `spring/SpringContextHolder.java` | Spring Bean 静态获取工具 |
| `workflow/listener/ApprovalEmailListener.java` | 审批通过邮件监听器 |
| `workflow/listener/RejectionEmailListener.java` | 审批驳回邮件监听器 |
| `workflow/job/ApprovalNotifyJobHandler.java` | XXL-Job 兜底补发任务 |

### 修改文件
| 文件 | 改动 |
|------|------|
| `ApproveLeave.java` | 添加 `notified` 字段 |
| `FulfillmentOrder.java` | 添加 `notified` 字段 |
| `ApproveLeaveMapper.java` | 新增 `selectUnnotified()` / `markNotified()` |
| `FulfillmentOrderMapper.java` | 同上 |
| `leave-request.bpmn20.xml` | approvedEnd + rejectedEnd 挂载监听器 |
| `fulfillment-approval.bpmn20.xml` | 同上 |

---

## 三、用户信息详情页

### 新增功能
- 个人中心页：修改昵称、邮箱、手机号、性别
- 头像上传（阿里云 OSS）
- 修改密码（弹窗形式）

### 新建文件
| 文件 | 说明 |
|------|------|
| `service/OssService.java` | 阿里云 OSS 文件上传服务 |
| `views/profile/index.vue` | 个人中心页面 |

### 修改文件
| 文件 | 改动 |
|------|------|
| `SysUserService.java` | 新增 getUserById / updateProfile / changePassword / resetPassword / updateAvatar / findByEmail / createByEmail |
| `UserController.java` | 新增 5 个接口：profile / changePassword / resetPassword / avatar |
| `AuthController.java` | getUserInfo 返回完整用户信息 |
| `stores/user.ts` | 扩展 avatar / nickname / userId / firstLogin |
| `views/home/index.vue` | Header 头像改为真实图片 + 个人中心入口 |
| `router/index.ts` | 新增 /profile 路由 |

---

## 四、邮箱验证码登录

### 新增功能
- 邮箱 + 验证码登录（不存在自动注册，分配默认用户角色）
- 首次登录强制设置密码
- Spring Security `EmailAuthenticationProvider` 自定义认证

### 新建/重构文件
| 文件 | 说明 |
|------|------|
| `auth/emailauth/EmailAuthenticationToken.java` | 重构 - 邮箱验证码认证 Token |
| `auth/emailauth/EmailAuthenticationProvider.java` | 重构 - Provider 含验证码校验 + 自动注册 |

### 修改文件
| 文件 | 改动 |
|------|------|
| `config/SecurityConfig.java` | 显式注册 Provider 顺序（Email 优先于 Dao） |
| `AuthController.java` | 新增 sendCode / emailLogin / resetPasswordByEmail |
| `UserMapper.java` | 新增 findByEmail |
| `UserRoleMapper.java` | 新增 getUsernamesByRoleCode |
| `UserRoleMapper.xml` | 新增按角色查用户 SQL |
| `SystemUsers.java` | 新增 firstLogin 字段 |
| `views/login/index.vue` | Tab 切换（密码/邮箱）+ 首次登录强制改密码 + 邮箱格式校验 |
| `api/user.ts` | 新增 sendCodeApi / emailLoginApi / resetPasswordByEmailApi |

---

## 五、审计日志

### 新增功能
- 三张审计日志表：登录日志、业务操作日志、工作流审批日志
- AOP 注解 `@OperationLog` 自动记录业务操作
- 登录/注销/审批 手动埋点

### 新建文件
| 文件 | 说明 |
|------|------|
| `sql/audit_log_init.sql` | 三张日志表建表 SQL |
| `pojo/SysLoginLog.java` | 登录日志实体 |
| `pojo/SysOperationLog.java` | 业务操作日志实体 |
| `pojo/SysWorkflowLog.java` | 工作流审批日志实体 |
| `mapper/SysLoginLogMapper.java` | 登录日志 Mapper |
| `mapper/SysOperationLogMapper.java` | 操作日志 Mapper |
| `mapper/SysWorkflowLogMapper.java` | 审批日志 Mapper |
| `service/AuditLogService.java` | 审计日志统一服务 |
| `annotation/OperationLog.java` | 自定义 AOP 注解 |
| `aspect/OperationLogAspect.java` | 操作日志切面 |

### 修改文件
| 文件 | 改动 |
|------|------|
| `AuthController.java` | 登录成功/失败/注销埋点 |
| `FlowableServiceImpl.java` | 审批通过/驳回埋点 |
| `UserController.java` | 关键方法加 @OperationLog |
| `RoleController.java` | 关键方法加 @OperationLog |

---

## 六、可视化流程设计器

### 新增功能
- 基于 bpmn-js Modeler 的拖拽式 BPMN 流程设计器
- 自定义属性面板：配置审批人（角色/指定人/上级）、邮件通知
- XML 导入/导出、流程部署/挂起/激活/删除
- 右侧代码编辑器：粘贴 XML 实时渲染

### 新建文件
| 文件 | 说明 |
|------|------|
| `workflow/controller/ProcessDefinitionController.java` | 流程定义管理 API |
| `workflow/service/ApproverService.java` | 审批人动态解析服务 |
| `views/workflow/designer/index.vue` | 流程管理列表页 |
| `views/workflow/designer/BpmnModeler.vue` | 拖拽设计器 + 属性面板 + XML 编辑器 |

### 修改文件
| 文件 | 改动 |
|------|------|
| `api/workflow.ts` | 新增 6 个流程定义 API |
| `router/index.ts` | 新增设计器路由 |

---

## 七、系统公告

### 新增功能
- 管理员发布/撤回/删除公告
- 用户登录后弹窗展示未读公告（类似版本更新通知）
- 已读记录追踪，关闭后不再弹出

### 新建文件
| 文件 | 说明 |
|------|------|
| `sql/announcement_init.sql` | 公告表 + 已读记录表 |
| `pojo/SysAnnouncement.java` | 公告实体 |
| `pojo/SysAnnouncementRead.java` | 已读记录实体 |
| `mapper/SysAnnouncementMapper.java` | 公告 Mapper |
| `mapper/SysAnnouncementReadMapper.java` | 已读记录 Mapper |
| `service/AnnouncementService.java` | 公告服务 |
| `controller/AnnouncementController.java` | 公告 API（8 个接口） |
| `api/announcement.ts` | 公告 API 函数 |
| `views/announcement/index.vue` | 公告管理页 |

### 修改文件
| 文件 | 改动 |
|------|------|
| `views/home/index.vue` | 登录后未读公告弹窗 |
| `router/index.ts` | 新增 /announcement 路由 |

---

## 八、部署配置 & Bug 修复

### 部署配置
| 文件 | 改动 |
|------|------|
| `pom.xml` | 新增 spring-boot-starter-mail / spring-boot-starter-aop / aliyun-sdk-oss |
| `application.yml` | 所有敏感配置改为环境变量占位 `${ENV_VAR:default}` |
| `application-pro.yml` | 生产配置（DB/Redis/Mail/OSS/XXL-Job 全环境变量） |
| `application-local.yml` | 新建本地开发配置 |
| `deploy.sh` | 新增 Mail/OSS/XXL-Job 环境变量 + 9998 端口映射 |

### Bug 修复
| 问题 | 修复 |
|------|------|
| 履约单列表越权 | `FulfillmentOrderController` 加 applicant 过滤 |
| 邮件连接超时 | SMTP 端口改为 465 + SSL |
| JavaMailSender 找不到 | mail 配置移到主 application.yml |
| EmailAuthenticationProvider 不生效 | SecurityConfig 显式注册 Provider 顺序 |
| 权限空值报错 | Provider 过滤空权限字符串 |
| 用户列表头像不显示 | 原生 img + 固定尺寸样式 |

---

## 九、其他改动

| 文件 | 改动 |
|------|------|
| `index.html` | 标题改为「BPMN 权限管理系统」+ favicon.svg |
| `public/favicon.svg` | 新建蓝紫渐变 "P" 图标 |
| `views/home/index.vue` | 移动端适配（侧边栏响应式 + 遮罩层） |
| 全部任务菜单图标 | iconMap 添加 IconList 映射 |

---

## 统计总览

| 维度 | 数量 |
|------|------|
| 新建 Java 文件 | ~25 个 |
| 新建 Vue 文件 | ~6 个 |
| 新建 SQL 文件 | 3 个 |
| 修改文件 | ~30 个 |
| 新增后端 API 接口 | ~25 个 |
| 新增前端页面 | 6 个 |

### 功能清单

1. ✅ EasyExcel 数据导出（用户/角色/任务）
2. ✅ 审批邮件通知（通过+驳回，Flowable 监听器 + XXL-Job 兜底）
3. ✅ 用户个人中心（资料修改/头像上传/密码修改）
4. ✅ 邮箱验证码登录（自动注册 + 首次强制改密码）
5. ✅ 审计日志（登录/操作/审批三维度）
6. ✅ 可视化 BPMN 流程设计器（拖拽 + 属性面板 + XML 编辑）
7. ✅ 系统公告（发布/撤回/登录弹窗/已读追踪）
8. ✅ 生产部署配置 + Bug 修复
