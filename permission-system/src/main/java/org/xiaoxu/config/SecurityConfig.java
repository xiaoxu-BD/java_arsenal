package org.xiaoxu.config;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.xiaoxu.auth.TokenAuthenticationFilter;
import org.xiaoxu.auth.handler.AccessDeniedHandlerImpl;
import org.xiaoxu.auth.handler.AuthenticationEntryPointImpl;

/**
 * @className: SecurityConfig
 * @author: xiaoxu
 * @date: 2025/11/17 13:58
 * @Version: 1.0
 * @description:
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    //放行 login 拦截其他的接口:

    @Resource
    private TokenAuthenticationFilter tokenAuthenticationFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
         http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 放行所有登录相关接口（不管你是 /login 还是 /api/auth/login）
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/hello").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/error", "/favicon.ico", "/css/**", "/js/**", "/images/**")
                        .permitAll()
                        // 跨域预检也放行（必须！）
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
//                 增加处理器机制判断 登录失败的场景
                 .exceptionHandling(exception -> exception
                         .authenticationEntryPoint(authenticationEntryPoint())
                         .accessDeniedHandler(accessDeniedHandler())
                 )
                // 只在非公开路径上添加 token 认证过滤器
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


         return http.build();
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandlerImpl();
    }


    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPointImpl();
    }


    @Autowired
    private UserDetailsService userDetailsService;


    /**
     * 流程: filter(构造哪种authentication) -> Manager -> provider -> userDetailsService & passwordEncoder
     * 注册你要使用的那个策略 daoAuthenticationProvider 决定是否认证成功
     * <p>
     * setHideUserNotFoundExceptions(false)：让 {@link UserDetailsService#loadUserByUsername}
     * 抛出的 {@link org.springframework.security.core.userdetails.UsernameNotFoundException}
     * 原样透传，方便 GlobalExceptionHandler 区分"用户不存在"和"密码错误"。
     * 注意：开放后存在用户名枚举风险（攻击者能区分两种错误），生产环境建议改回 true。
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
