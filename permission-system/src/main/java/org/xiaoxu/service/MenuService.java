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
import java.util.stream.Collectors;

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

        // 3. 一次性查出所有菜单，建 id → menu 的索引（只查一次数据库）
        List<SystemMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SystemMenu>().eq(SystemMenu::getDeleted, "0")
        );
        Map<Long, SystemMenu> menuMap = allMenus.stream()
                .collect(Collectors.toMap(SystemMenu::getId, m -> m));

        // 4. 收集直接菜单 + 补齐所有父级菜单ID（纯内存操作，不再查数据库）
        Set<Long> allIds = new HashSet<>(menuIds);
        for (Long menuId : menuIds) {
            Long pid = menuMap.get(menuId) != null ? menuMap.get(menuId).getParentId() : null;
            while (pid != null && pid > 0 && !allIds.contains(pid)) {
                allIds.add(pid);
                SystemMenu parent = menuMap.get(pid);
                pid = parent != null ? parent.getParentId() : null;
            }
        }

        // 5. 过滤出需要的菜单，构建树
        List<SystemMenu> visibleMenus = allMenus.stream()
                .filter(m -> allIds.contains(m.getId()))
                .sorted(Comparator.comparing(m -> Optional.ofNullable(m.getSort()).orElse(0L)))
                .collect(Collectors.toList());
        return buildTree(visibleMenus);
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
        return buildTree(allMenus);
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
     * 构建菜单树（一次遍历，无递归）
     */
    private List<Map<String, Object>> buildTree(List<SystemMenu> menus) {
        // 1. 把每个菜单转成 Map 节点，并用 id 做索引
        Map<Long, Map<String, Object>> nodeMap = new LinkedHashMap<>();
        for (SystemMenu menu : menus) {
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
            node.put("children", new ArrayList<Map<String, Object>>());
            nodeMap.put(menu.getId(), node);
        }

        // 2. 一次遍历：找父节点，挂上去
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Map<String, Object> node : nodeMap.values()) {
            Long parentId = (Long) node.get("parentId");
            if (parentId == null || parentId == 0) {
                // 根节点
                roots.add(node);
            } else {
                // 找到父节点，把自己挂到父节点的 children 里
                Map<String, Object> parent = nodeMap.get(parentId);
                if (parent != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> children =
                            (List<Map<String, Object>>) parent.get("children");
                    children.add(node);
                }
            }
        }

        // 3. 清理空的 children（叶子节点不需要 children: []）
        cleanEmptyChildren(roots);
        return roots;
    }

    private void cleanEmptyChildren(List<Map<String, Object>> nodes) {
        for (Map<String, Object> node : nodes) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children =
                    (List<Map<String, Object>>) node.get("children");
            if (children != null && children.isEmpty()) {
                node.remove("children");
            } else if (children != null) {
                cleanEmptyChildren(children);
            }
        }
    }
}
