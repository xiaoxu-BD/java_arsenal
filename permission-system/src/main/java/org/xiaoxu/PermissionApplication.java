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
@MapperScan("org.xiaoxu.mapper")
public class PermissionApplication {



    public static void main( String[] args ){
        SpringApplication.run(PermissionApplication.class, args);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后: " + encodedPassword);

        // 验证密码
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("验证结果: " + matches);

    }
}
