package org.xiaoxu.config;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xiaoxu.facade.FacadeAspect;

@EnableDubbo
@Configuration
public class RpcConfiguration {

    @Bean
    public FacadeAspect facadeAspect() {
        return new FacadeAspect();
    }
}
