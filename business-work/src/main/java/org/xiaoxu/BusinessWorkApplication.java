package org.xiaoxu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.xiaoxu.datasource.DataSourceConfiguration;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@MapperScan({"org.xiaoxu.mapper", "org.xiaoxu.channel.mapper"})
@Import(DataSourceConfiguration.class)
public class BusinessWorkApplication
{
    public static void main( String[] args ) {
        SpringApplication.run(BusinessWorkApplication.class,args);
    }
}
