package org.xiaoxu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@MapperScan({"org.xiaoxu.mapper", "org.xiaoxu.workflow.mapper"})
public class PermissionApplication {



    public static void main( String[] args ){
        SpringApplication.run(PermissionApplication.class, args);
    }
}
