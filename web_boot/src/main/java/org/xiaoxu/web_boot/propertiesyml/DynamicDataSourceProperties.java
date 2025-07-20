package org.xiaoxu.web_boot.propertiesyml;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @className: DynamicDataSourceProperties
 * @author: xiaoxu
 * @date: 2025/7/14 21:08
 * @Version: 1.0
 * @description:
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource")
public class DynamicDataSourceProperties {
    private String defaultDataSource;
    private Map<String, DataSourceProperty> datasources;


    @Data
    public static class DataSourceProperty {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
    }
}
