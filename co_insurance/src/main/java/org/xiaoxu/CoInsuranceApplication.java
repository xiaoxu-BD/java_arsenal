package org.xiaoxu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@MapperScan
public class CoInsuranceApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(CoInsuranceApplication.class,args);
    }
}
