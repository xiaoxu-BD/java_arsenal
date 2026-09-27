package org.xiaoxu.wecommail;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 企业微信群消息自动汇总发送邮件系统
 *
 * <p>模块:consumer(MQ消费) / scheduler(定时扫描) / mail(邮件发送) / admin(管理接口)。
 * 持久层:MyBatis-Plus(BaseMapper + 注解 SQL)。设计文档见 docs/backend-design.md。
 */
@EnableScheduling
@ConfigurationPropertiesScan
@MapperScan("org.xiaoxu.wecommail.mapper")
@SpringBootApplication
public class WeComMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeComMailApplication.class, args);
    }
}
