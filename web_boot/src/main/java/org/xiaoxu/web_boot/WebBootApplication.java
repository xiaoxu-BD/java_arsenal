package org.xiaoxu.web_boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.xiaoxu.web_boot.propertiesyml.DynamicDataSourceProperties;

@SpringBootApplication
//@ConfigurationPropertiesScan
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class WebBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebBootApplication.class, args);
	}

}
