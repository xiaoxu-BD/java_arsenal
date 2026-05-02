package org.xiaoxu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.xiaoxu.datasource.DataSourceConfiguration;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@Import(DataSourceConfiguration.class)
public class BusinessWorkApplication
{
    public static void main( String[] args ) {
        SpringApplication.run(BusinessWorkApplication.class,args);
    }
}
