package org.xiaoxu.workflow.identity;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xiaoxu.mapper.RoleMapper;

import java.util.Collections;
import java.util.List;

/**
 * 工作流身份解析：把 RBAC 中的角色 code 直接当作 Flowable 候选组。
 * <p>
 * 设计约定：BPMN 中 {@code flowable:candidateGroups} 的字符串值
 * 与 {@code system_role.code} 严格一致（区分大小写）。
 * <p>
 * 不依赖 Flowable 自带的 ACT_ID_USER / ACT_ID_GROUP / ACT_ID_MEMBERSHIP，
 * 待办过滤完全走系统 RBAC。
 */
@Service
@RequiredArgsConstructor
public class WorkflowIdentityService {

    private final RoleMapper roleMapper;

    /**
     * 查询某用户在 RBAC 中的角色 code 列表，作为 Flowable 候选组使用。
     *
     * @param username 系统用户名
     * @return 角色 code 列表；用户名空或没有任何角色时返回空列表
     */
    public List<String> getGroupsOf(String username) {
        if (StringUtils.isBlank(username)) {
            return Collections.emptyList();
        }
        List<String> codes = roleMapper.getRoleCodesByUsername(username);
        return codes == null ? Collections.emptyList() : codes;
    }
}
