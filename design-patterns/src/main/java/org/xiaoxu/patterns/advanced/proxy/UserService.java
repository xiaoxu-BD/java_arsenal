package org.xiaoxu.patterns.advanced.proxy;

import java.util.List;

/** 用户服务接口（Subject）：动态代理要求目标类有接口，代理在运行期动态生成 */
public interface UserService {

    String getUserName(long userId);

    List<String> listPermissions(long userId);
}
