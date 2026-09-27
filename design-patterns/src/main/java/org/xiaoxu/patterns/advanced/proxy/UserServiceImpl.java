package org.xiaoxu.patterns.advanced.proxy;

import java.util.List;

/** 目标实现（Real Subject）：只写业务，不掺日志/耗时等杂事 */
public class UserServiceImpl implements UserService {

    @Override
    public String getUserName(long userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId 必须为正数: " + userId);
        }
        // 实际项目里这里是查库。mock 一条
        return "用户-" + userId;
    }

    @Override
    public List<String> listPermissions(long userId) {
        return List.of("order:query", "order:create");
    }
}
