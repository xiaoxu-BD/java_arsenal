package org.xiaoxu.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xiaoxu.enums.UserStatusEnum;
import org.xiaoxu.handler.UserTypeHandler;

//@Configuration
public class MyBatisConfig {

    @Bean
    public UserTypeHandler userTypeHandler() {
        return new UserTypeHandler();
    }
}