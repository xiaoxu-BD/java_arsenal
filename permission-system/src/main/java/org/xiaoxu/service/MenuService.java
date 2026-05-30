package org.xiaoxu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xiaoxu.mapper.MenuMapper;
import org.xiaoxu.mapper.RoleMenuMapper;
import org.xiaoxu.mapper.UserRoleMapper;
import org.xiaoxu.pojo.SystemMenu;

import java.util.*;

@Slf4j
@Service
public class MenuService {

    @Resource
    private MenuMapper menuMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    /**
     * 获取当前用户可见的菜单树（按角色权限过滤）
     */
    public List<Map<String, Object>> getUserMenuTree(Long userId) {
        // 1. 查用户角色
        Set<Long> roleIds = userRoleMapper.getRoleIdsByUserId(userId);
        log.info("用户[{}]的角色IDs: {}", userId, roleIds);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 查角色关联的菜单ID
        Set<Long> menuIds = roleMenuMapper.getMenuIdsByRoleId(roleIds);
        log.info("用户[{}]的菜单IDs: {}", userId, menuIds);
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 补充父级菜单ID（保证树结构完整）
        Set<Long> allIds = new HashSet<>(menuIds);
        List<SystemMenu> directMenus = menuMapper.selectBatchIds(menuIds);
        for (SystemMenu m : directMenus) {
            // 递归往上找所有父级
            Long pid = m.getParentId();
            while (pid != null && pid > 0 && !allIds.contains(pid)) {
                allIds.add(pid);
                SystemMenu parent = menuMapper.selectById(pid);
                if (parent != null) {
                    pid = parent.getParentId();
                } else {
                    break;
                }
            }
        }

        // 4. 查出所有需要的菜单，构建树
        List<SystemMenu> allMenus = menuMapper.selectBatchIds(allIds);
        allMenus.removeIf(Objects::isNull);
        allMenus.sort(Comparator.comparing(
                m -> Optional.ofNullable(m.getSort()).orElse(0L)
        ));
        return buildTree(allMenus, 0L);
    }

    /**
     * 获取全部菜单树（管理用）
     */
    public List<Map<String, Object>> getMenuTree() {
        List<SystemMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SystemMenu>()
                        .eq(SystemMenu::getDeleted, "0")
                        .orderByAsc(SystemMenu::getSort)
        );
        return buildTree(allMenus, 0L);
    }

    public List<SystemMenu> getMenuList() {
        return menuMapper.selectList(
                new LambdaQueryWrapper<SystemMenu>()
                        .eq(SystemMenu::getDeleted, "0")
                        .orderByAsc(SystemMenu::getSort)
        );
    }

    public SystemMenu getMenuById(Long id) {
        return menuMapper.selectById(id);
    }

    public void createMenu(SystemMenu menu) {
        menuMapper.insert(menu);
    }

    public void updateMenu(SystemMenu menu) {
        menuMapper.updateById(menu);
    }

    public void deleteMenu(Long id) {
        SystemMenu menu = new SystemMenu();
        menu.setId(id);
        menu.setDeleted("1");
        menuMapper.updateById(menu);
    }

    /**
     * 递归构建菜单树
     */
    private List<Map<String, Object>> buildTree(List<SystemMenu> menus, Long parentId) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (SystemMenu menu : menus) {
            if (!Objects.equals(menu.getParentId(), parentId)) {
                continue;
            }
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", menu.getId());
            node.put("name", menu.getName());
            node.put("path", menu.getPath());
            node.put("icon", menu.getIcon());
            node.put("component", menu.getComponent());
            node.put("permission", menu.getPermission());
            node.put("type", menu.getType());
            node.put("sort", menu.getSort());
            node.put("visible", menu.getVisible());
            node.put("parentId", menu.getParentId());

            List<Map<String, Object>> children = buildTree(menus, menu.getId());
            if (!children.isEmpty()) {
                node.put("children", children);
            }
            tree.add(node);
        }
        return tree;
    }
}
