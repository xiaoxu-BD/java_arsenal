# 项目长期记忆：permission-system

## 已知缺陷 / TODO
- **FulfillmentOrder 分页线上报错 `Unknown column 'user_id'`（已止血）**：根因=实体 `FulfillmentOrder` 有 `userId`(Long) 字段，但 `fulfillment_order` 表（sql/workflow_init.sql:30-50）无 `user_id` 列；MP `selectPage` 默认 SELECT 全字段 → Unknown column。create/update 不报错因 MP 忽略 null 且 userId 从未被 set。2026-07-25 采用临时止血：给 `FulfillmentOrder.userId` 标 `@TableField(exist = false)`（FulfillmentOrder.java:37-44），报错消除。后续仍需根治：① 给表加 `user_id` 列（并同步 workflow_init.sql）；② create/submit 时 `order.setUserId(currentUserId)`（可取 `request.getAttribute("userId")`，TokenAuthenticationFilter.java:65 已 set）；③ pageQuery 改用 userId 隔离，getDetailById/deleteById/changeStatus 加归属校验。userId 当前仍是死字段。
