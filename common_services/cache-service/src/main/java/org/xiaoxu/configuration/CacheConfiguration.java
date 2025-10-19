package org.xiaoxu.configuration;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.context.annotation.Configuration;

/**
 * @className: CacheConfiguration
 * @author: xiaoxu
 * @date: 2025/10/19 15:46
 * @Version: 1.0
 * @description:
 */
@Configuration
@EnableMethodCache(basePackages = "org.xiaoxu")
public class CacheConfiguration {
}
