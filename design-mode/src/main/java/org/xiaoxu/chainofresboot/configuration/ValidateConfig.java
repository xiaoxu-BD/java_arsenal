package org.xiaoxu.chainofresboot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.xiaoxu.chainofresboot.OrderCreateValidator;
import org.xiaoxu.chainofresboot.validators.GoodsValidator;
import org.xiaoxu.chainofresboot.validators.StockValidator;
import org.xiaoxu.chainofresboot.validators.UserValidator;
import org.xiaoxu.entity.User;

@Configuration
public class ValidateConfig {




    @Bean
    public UserValidator userValidator(){
        return  new UserValidator();
    }

    @Bean
    public StockValidator stockValidator(){
        return new StockValidator();
    }

    @Bean
    public GoodsValidator goodsValidator(){
        return new GoodsValidator();
    }



    @Bean
    @Primary
    public OrderCreateValidator validatorChain(UserValidator userValidator,StockValidator stockValidator,GoodsValidator goodsValidator){
        userValidator.setNext(goodsValidator);
        goodsValidator.setNext(stockValidator);
        return  userValidator;
    }
}
