package org.xiaoxu.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xiaoxu.mapper.RoleMapper;
import org.xiaoxu.mapper.UserMapper;
import org.xiaoxu.mapper.UserRoleMapper;
import org.xiaoxu.pojo.SystemUsers;

import java.util.Collections;
import java.util.List;

/**
 * 审批人解析服务 — 运行时根据策略找到具体审批人。
 * <p>
 * BPMN 中通过表达式调用：${approverService.resolve('ROLE_MANAGER', initiator)}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApproverService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 按角色分配：查该角色下的用户，返回第一个。
     * 后续可扩展为负载均衡、轮询等策略。
     */
    public String resolve(String roleCode, String initiator) {
        List<String> users = userRoleMapper.getUsernamesByRoleCode(roleCode);
        if (users == null || users.isEmpty()) {
            // 找不到审批人，退回发起人
            return initiator;
        }
        return users.get(0);
    }

    /**
     * 发起人上级：通过 system_users.manager_id 查找直属上级。
     * <p>
     * 使用方式：BPMN 中配置 flowable:assignee="${approverService.resolveManager(initiator)}"
     */
    public String resolveManager(String initiator) {
        // 1. 查询当前用户
        SystemUsers user = userMapper.selectOne(new LambdaQueryWrapper<SystemUsers>()
                .select(SystemUsers::getId, SystemUsers::getManagerId)
                .eq(SystemUsers::getUsername, initiator)
                .eq(SystemUsers::getDeleted, "0"));
        if (user == null) {
            log.warn("用户 {} 不存在", initiator);
            return initiator;
        }
        if (user.getManagerId() == null) {
            log.warn("用户 {} 没有配置直属上级，退回发起人自己", initiator);
            return initiator;
        }
        // 2. 查询上级用户
        SystemUsers manager = userMapper.selectById(user.getManagerId());
        if (manager == null || !"0".equals(manager.getDeleted())) {
            log.warn("用户 {} 的上级(ID={})不存在或已删除", initiator, user.getManagerId());
            return initiator;
        }
        log.info("用户 {} 的直属上级为 {}", initiator, manager.getUsername());
        return manager.getUsername();
    }
}
