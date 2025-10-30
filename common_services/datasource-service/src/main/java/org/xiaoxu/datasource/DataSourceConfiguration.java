package org.xiaoxu.datasource;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xiaoxu.handler.DataObjectHandler;

/**
 * @className: DataSourceConfiguration
 * @author: xiaoxu
 * @date: 2025/10/19 15:49
 * @Version: 1.0
 * @description:
 */
@Configuration
public class DataSourceConfiguration {

        @Bean
        public DataObjectHandler myMetaObjectHandler() {
            return new DataObjectHandler();
        }

        @Bean
        public MybatisPlusInterceptor mybatisPlusInterceptor() {
            MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
            //乐观锁插件
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
            //防全表更新与删除插件
            interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
            //分页插件
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
            return interceptor;
        }
    }


