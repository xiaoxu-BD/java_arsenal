package org.xiaoxu.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝配置
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {

    private String appId;
    private String privateKey;
    private String publicKey;
    private String notifyUrl;
    private String returnUrl;
    private String gatewayUrl;
    private String signType;
    private String charset;
    private String format;

    @PostConstruct
    public void check() {

        log.info("================ 支付宝配置 ================");
        log.info("appId        : {}", appId);
        log.info("gateway      : {}", gatewayUrl);
        log.info("signType     : {}", signType);
        log.info("charset      : {}", charset);
        log.info("format       : {}", format);
        log.info("notifyUrl    : {}", notifyUrl);
        log.info("returnUrl    : {}", returnUrl);

        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException("支付宝应用私钥为空");
        }

        if (publicKey == null || publicKey.isBlank()) {
            throw new IllegalStateException("支付宝公钥为空");
        }

        log.info("privateKey长度 : {}", privateKey.length());
        log.info("publicKey长度  : {}", publicKey.length());

        log.info("privateKey前30位 : {}", privateKey.substring(0, 30));
        log.info("publicKey前30位  : {}", publicKey.substring(0, 30));

        if (privateKey.contains("BEGIN")) {
            throw new IllegalStateException("privateKey 包含 BEGIN PRIVATE KEY，请删除头尾");
        }

        if (publicKey.contains("BEGIN")) {
            throw new IllegalStateException("publicKey 包含 BEGIN PUBLIC KEY，请删除头尾");
        }

        log.info("==========================================");
    }

    @Bean
    public AlipayClient alipayClient() {

        return new DefaultAlipayClient(
                gatewayUrl,
                appId,
                privateKey,
                format,
                charset,
                publicKey,
                signType
        );
    }
}
