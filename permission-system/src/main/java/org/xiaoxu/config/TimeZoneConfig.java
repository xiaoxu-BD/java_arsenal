package org.xiaoxu.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * 统一 JVM 默认时区，避免 LocalDateTime.now() / MySQL 时间戳与前端展示偏差。
 */
@Configuration
public class TimeZoneConfig {

    private static final String DEFAULT_ZONE = "Asia/Shanghai";

    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(DEFAULT_ZONE));
    }
}
