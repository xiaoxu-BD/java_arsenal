package org.xiaoxu.web_boot.config.datasource;

import com.alibaba.druid.DbType;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @className: DataSourceConfig
 * @author: xiaoxu
 * @date: 2025/5/22 7:51
 * @Version: 1.0
 * @description:
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource customDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setInitialSize(5);
        dataSource.setMinIdle(5);
        dataSource.setMaxActive(20);
        dataSource.setMaxWait(60000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(false);
        dataSource.setTestWhileIdle(true);
        //     # ❗连接失败时不重试、不创建线程，而是直接抛出异常，默认值为false
        dataSource.setBreakAfterAcquireFailure(Boolean.TRUE);


        try {
//            dataSource.setFilters("stat,wall,slf4j");
            dataSource.setDbType(DbType.mysql); // mysql 取决于你用的数据库
        } catch (Exception e) {
            log.error("druid configuration initialization filter", e);
        }

        return dataSource;
    }
}
