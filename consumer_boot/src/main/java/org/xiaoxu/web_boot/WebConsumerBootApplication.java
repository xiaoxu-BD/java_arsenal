package org.xiaoxu.web_boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@MapperScan("org.xiaoxu")
public class WebConsumerBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebConsumerBootApplication.class, args);
	}

}
