package org.xiaoxu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@MapperScan("org.xiaoxu.mapper")
public class PermissionApplication
{
    public static void main( String[] args ){
        SpringApplication.run(PermissionApplication.class, args);
    }
}
