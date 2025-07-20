package org.xiaoxu.web_boot.config.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.xiaoxu.web_boot.propertiesyml.DynamicDataSourceProperties;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @className: DynamicDataSourceConfig
 * @author: xiaoxu
 * @date: 2025/7/14 21:03
 * @Version: 1.0
 * @description: 动态数据源配置:
 */
@Configuration
public class DynamicDataSourceConfig {


    @Bean
    @ConfigurationProperties("spring.datasource.ds1")
    public DataSource ds1() {
        return  new DruidDataSource();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.ds2")
    public DataSource ds2() {
        return new DruidDataSource();
    }

    @Primary
    @Bean("dynamicDataSource")
    public DataSource dynamicDataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("ds1", ds1());
        targetDataSources.put("ds2", ds2());

        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
        dataSource.setTargetDataSources(targetDataSources);
        dataSource.setDefaultTargetDataSource(ds1());
        return dataSource;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dynamicDataSource) {
        return new DataSourceTransactionManager(dynamicDataSource);
    }
}
