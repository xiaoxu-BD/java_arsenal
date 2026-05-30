package org.xiaoxu.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.xiaoxu.auth.LoginUser;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class TokenProvider {

    private static final long TOKEN_EXPIRE_HOURS = 1;
    private static final String TOKEN_PREFIX = "login:token:";
    private static final String USER_TOKEN_PREFIX = "login:user:";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 生成 UUID token 并将 LoginUser 存入 Redis，同时建立 userId → token 反向索引
     */
    public String createToken(LoginUser loginUser) {
        String token = UUID.randomUUID().toString();
        String cacheKey = TOKEN_PREFIX + token;
        String userKey = USER_TOKEN_PREFIX + loginUser.getUserId();

        // 先清除该用户旧的 token（防止同一用户多个有效 token）
        Object oldToken = redisTemplate.opsForValue().get(userKey);
        if (oldToken instanceof String oldTokenStr) {
            redisTemplate.delete(TOKEN_PREFIX + oldTokenStr);
        }

        redisTemplate.opsForValue().set(cacheKey, loginUser, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(userKey, token, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
        return token;
    }

    /**
     * 从 Redis 获取 LoginUser
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
     * 删除 token
     */
    public void removeToken(String token) {
        LoginUser loginUser = getLoginUser(token);
        redisTemplate.delete(TOKEN_PREFIX + token);
        if (loginUser != null) {
            redisTemplate.delete(USER_TOKEN_PREFIX + loginUser.getUserId());
        }
    }

    /**
     * 根据 userId 强制清除该用户的 token（权限变更时调用）
     */
    public void removeTokenByUserId(Long userId) {
        String userKey = USER_TOKEN_PREFIX + userId;
        Object token = redisTemplate.opsForValue().get(userKey);
        if (token instanceof String tokenStr) {
            redisTemplate.delete(TOKEN_PREFIX + tokenStr);
        }
        redisTemplate.delete(userKey);
    }
}
