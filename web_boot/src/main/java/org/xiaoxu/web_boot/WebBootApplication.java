package org.xiaoxu.web_boot;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.xiaoxu.web_boot.propertiesyml.DynamicDataSourceProperties;

import java.io.IOException;

@SpringBootApplication
//@ConfigurationPropertiesScan
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@MapperScan("org.xiaoxu.web_boot.mapper")
@EnableScheduling
public class WebBootApplication {
	private static final Logger log = LoggerFactory.getLogger(WebBootApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(WebBootApplication.class, args);
	}

}
