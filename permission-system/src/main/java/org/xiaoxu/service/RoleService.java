package org.xiaoxu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xiaoxu.common.utils.TokenProvider;
import org.xiaoxu.mapper.RoleMapper;
import org.xiaoxu.mapper.RoleMenuMapper;
import org.xiaoxu.mapper.UserRoleMapper;
import org.xiaoxu.pojo.SystemRole;
import org.xiaoxu.pojo.SystemRoleMenu;
import org.xiaoxu.pojo.SystemUserRole;

import java.util.*;

@Service
public class RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private TokenProvider tokenProvider;

    public List<SystemRole> getRoleList() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<SystemRole>()
                        .eq(SystemRole::getDeleted, "0")
                        .orderByAsc(SystemRole::getSort)
        );
    }

    /**
     * 获取角色详情 + 已分配的菜单ID列表
     */
    public Map<String, Object> getRoleDetail(Long roleId) {
        SystemRole role = roleMapper.selectById(roleId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("role", role);
        Set<Long> menuIds = roleMenuMapper.getMenuIdsByRoleId(Collections.singleton(roleId));
        data.put("menuIds", menuIds);
        return data;
    }

    public void createRole(SystemRole role) {
        roleMapper.insert(role);
    }

    public void updateRole(SystemRole role) {
        roleMapper.updateById(role);
    }

    public void deleteRole(Long id) {
        SystemRole role = new SystemRole();
        role.setId(id);
        role.setDeleted("1");
        roleMapper.updateById(role);
        // 同时软删除角色-菜单关联
        SystemRoleMenu update = new SystemRoleMenu();
        update.setDeleted("1");
        roleMenuMapper.update(update,
                new LambdaQueryWrapper<SystemRoleMenu>()
                        .eq(SystemRoleMenu::getRoleId, id)
        );
    }

    /**
     * 给角色分配菜单：先删旧关联，再插入新关联，同时清除该角色下所有用户的 token
     */
    @Transactional
    public void updateRoleMenu(Long roleId, List<Long> menuIds) {
        // 删除旧关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<SystemRoleMenu>()
                        .eq(SystemRoleMenu::getRoleId, roleId)
        );
        // 插入新关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SystemRoleMenu rm = new SystemRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                rm.setDeleted("0");
                roleMenuMapper.insert(rm);
            }
        }
        // 清除该角色下所有用户的 token
        List<SystemUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SystemUserRole>()
                        .eq(SystemUserRole::getRoleId, roleId)
                        .eq(SystemUserRole::getDeleted, "0")
        );
        for (SystemUserRole ur : userRoles) {
            tokenProvider.removeTokenByUserId(ur.getUserId());
        }
    }
}
