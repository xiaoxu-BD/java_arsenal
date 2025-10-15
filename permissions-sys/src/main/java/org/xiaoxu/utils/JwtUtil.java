package org.xiaoxu.utils;

import cn.hutool.jwt.Claims;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    public final static String secret = "change_this_secret"; // 配置化
    private final long expireMillis = 3600_000 * 8;



    public static String generateToken(String secretKey, Map<String, Object> claims) {
        // 使用 HS256 算法创建签名器
        JWTSigner signer = JWTSignerUtil.hs256(secretKey.getBytes());

        // 生成 JWT
        return JWTUtil.createToken(claims, signer);
    }



    public static Map<String, Object> parseAndVerifyToken(String token, String secretKey) {
        try {
            // 使用相同的密钥构建签名器
            JWTSigner signer = JWTSignerUtil.hs256(secretKey.getBytes());

            // 解析并验证 token
            JWT jwt = JWTUtil.parseToken(token);

            // 验证签名有效（包括过期等校验，如果设置了 exp 的话）
            if (jwt.verify(signer)) {
                // 返回所有的 payload 数据
                return jwt.getPayloads();
            } else {
                System.err.println("JWT 校验失败（可能是签名错误或已过期）");
                return null;
            }
        } catch (Exception e) {
            System.err.println("JWT 解析或校验异常: " + e.getMessage());
            return null;
        }
    }


    public static void main(String[] args) {
        String secret = JwtUtil.secret;;

        // 构造用户信息
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1001);
        claims.put("username", "Alice");
        claims.put("role", "admin");

        // 生成 Token
        String token = generateToken(secret, claims);
        System.out.println("生成的 JWT Token: " + token);

        // 解析 Token
        Map<String, Object> parsed = parseAndVerifyToken(token, secret);
        if (parsed != null) {
            System.out.println("解析成功:");
            System.out.println("userId: " + parsed.get("userId"));
            System.out.println("username: " + parsed.get("username"));
            System.out.println("role: " + parsed.get("role"));
        } else {
            System.out.println("解析失败或校验不通过");
        }
    }

}
