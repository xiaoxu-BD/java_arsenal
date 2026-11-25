package org.xiaoxu.wecommail.payment.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

/**
 * 模拟支付宝的 RSA2 签名/验签。本地渠道使用临时密钥；
 * 真实接入时商户只持有支付宝公钥，不持有渠道私钥。
 */
@Component
public class MockAlipaySignature {

    private final KeyPair channelKeyPair;

    public MockAlipaySignature() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        channelKeyPair = generator.generateKeyPair();
    }

    public String sign(Map<String, String> parameters) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(channelKeyPair.getPrivate());
            signature.update(content(parameters));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("模拟支付宝签名失败", e);
        }
    }

    public boolean verify(Map<String, String> parameters) {
        if (!"RSA2".equals(parameters.get("sign_type")) || parameters.get("sign") == null) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(channelKeyPair.getPublic());
            signature.update(content(parameters));
            return signature.verify(Base64.getDecoder().decode(parameters.get("sign")));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] content(Map<String, String> parameters) {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, String> entry : new TreeMap<>(parameters).entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if ("sign".equals(key) || "sign_type".equals(key) || value == null || value.isEmpty()) {
                continue;
            }
            if (!content.isEmpty()) {
                content.append('&');
            }
            content.append(key).append('=').append(value);
        }
        return content.toString().getBytes(StandardCharsets.UTF_8);
    }
}
