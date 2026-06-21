package org.xiaoxu.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.xiaoxu.auth.LoginUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Token 管理 — 支持同一用户多地/多设备同时登录。
 * <p>
 * Redis 结构：
 * - login:token:{token}     → LoginUser（单个 token 的会话数据）
 * - login:user:{userId}     → List<String>（该用户所有有效 token 列表）
 */
@Component
public class TokenProvider {

    private static final long TOKEN_EXPIRE_HOURS = 1;
    private static final String TOKEN_PREFIX = "login:token:";
    private static final String USER_TOKEN_PREFIX = "login:user:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成新 token，不清除旧 token，支持同一用户多地同时在线。
     */
    public String createToken(LoginUser loginUser) {
        String token = UUID.randomUUID().toString();
        String cacheKey = TOKEN_PREFIX + token;
        String userKey = USER_TOKEN_PREFIX + loginUser.getUserId();

        // 存储 token → LoginUser
        redisTemplate.opsForValue().set(cacheKey, loginUser, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

        // 将新 token 追加到用户的 token 列表
        Object existing = redisTemplate.opsForValue().get(userKey);
        List<String> tokenList;
        if (existing instanceof List<?> list) {
            tokenList = new ArrayList<>(list.stream().map(Object::toString).toList());
        } else {
            tokenList = new ArrayList<>();
        }
        // 清理已过期的 token（只保留 Redis 中还存在的）
        tokenList.removeIf(t -> !redisTemplate.hasKey(TOKEN_PREFIX + t));
        tokenList.add(token);
        redisTemplate.opsForValue().set(userKey, tokenList, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

        return token;
    }

    /**
     * 从 Redis 获取 LoginUser（纯读取）
     */
    public LoginUser getLoginUser(String token) {
        String cacheKey = TOKEN_PREFIX + token;
        Object obj = redisTemplate.opsForValue().get(cacheKey);
        if (obj instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    /**
     * 续签 token（延长过期时间）
     */
    public void renewToken(String token, Long userId) {
        String cacheKey = TOKEN_PREFIX + token;
        redisTemplate.expire(cacheKey, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        // 用户 token 列表也续签
        String userKey = USER_TOKEN_PREFIX + userId;
        redisTemplate.expire(userKey, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    /**
     * 注销当前设备的 token（只影响当前会话，不影响其他设备）
     */
    public void removeToken(String token) {
        LoginUser loginUser = getLoginUser(token);
        redisTemplate.delete(TOKEN_PREFIX + token);
        if (loginUser != null) {
            // 从用户的 token 列表中移除当前 token
            removeFromTokenList(loginUser.getUserId(), token);
        }
    }

    /**
     * 强制踢掉该用户的所有设备（权限变更时调用）
     */
    public void removeTokenByUserId(Long userId) {
        String userKey = USER_TOKEN_PREFIX + userId;
        Object existing = redisTemplate.opsForValue().get(userKey);
        if (existing instanceof List<?> tokenList) {
            for (Object t : tokenList) {
                redisTemplate.delete(TOKEN_PREFIX + t.toString());
            }
        }
        redisTemplate.delete(userKey);
    }

    /**
     * 从用户的 token 列表中移除指定 token
     */
    private void removeFromTokenList(Long userId, String token) {
        String userKey = USER_TOKEN_PREFIX + userId;
        Object existing = redisTemplate.opsForValue().get(userKey);
        if (existing instanceof List<?> list) {
            List<String> tokenList = new ArrayList<>(list.stream().map(Object::toString).toList());
            tokenList.remove(token);
            if (tokenList.isEmpty()) {
                redisTemplate.delete(userKey);
            } else {
                redisTemplate.opsForValue().set(userKey, tokenList, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
            }
        }
    }
}
