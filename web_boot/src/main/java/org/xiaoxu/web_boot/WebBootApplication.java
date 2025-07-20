package org.xiaoxu.web_boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.xiaoxu.web_boot.propertiesyml.DynamicDataSourceProperties;

@SpringBootApplication
//@ConfigurationPropertiesScan
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@MapperScan("org.xiaoxu.web_boot.mapper")
public class WebBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebBootApplication.class, args);
	}

}
