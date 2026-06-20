package org.xiaoxu.config;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.xiaoxu.auth.emailauth.EmailAuthenticationProvider;
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

    @Autowired
    @Lazy
    private EmailAuthenticationProvider emailAuthenticationProvider;

    /**
     * 流程: filter(构造哪种authentication) -> Manager -> provider -> userDetailsService & passwordEncoder
     * 注册你要使用的那个策略 daoAuthenticationProvider 决定是否认证成功
     *       ▼
     *   AuthController.login()
     *       │
     *       │  new UsernamePasswordAuthenticationToken(username, password)
     *       │  ↑ 未认证的凭证（authenticated=false, principal=字符串）
     *       │
     *       ▼
     *   AuthenticationManager.authenticate(token)
     *       │
     *       │  遍历 providers，找到 DaoAuthenticationProvider
     *       │
     *       ▼
     *   DaoAuthenticationProvider.authenticate()
     *       │
     *       ├─→ UserDetailsService.loadUserByUsername(username)
     *       │       │
     *       │       ├─ 查数据库 userMapper.getOne(username)
     *       │       ├─ 查权限 menuMapper.getPermissionCodeByUserId()
     *       │       └─ 返回 LoginUser（实现了 UserDetails）
     *       │
     *       ├─→ PasswordEncoder.matches(明文密码, 加密密码)
     *       │       │
     *       │       ├─ 匹配 → 继续
     *       │       └─ 不匹配 → 抛 BadCredentialsException
     *       │
     *       └─→ 创建新的 UsernamePasswordAuthenticationToken（三参）
     *               principal=LoginUser, credentials=null, authenticated=true
     *       │
     *       ▼
     *   返回到 AuthController（authenticated=true 的 Authentication）
     *       │
     *       ├─ SecurityContextHolder 存入 authentication
     *       ├─ 取出 LoginUser 和 permissions
     *       └─ 生成 JWT token 返回给前端
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


    /**
     * 用 AuthenticationManagerBuilder 显式注册 Provider，保证顺序：
     * 1. EmailAuthenticationProvider（优先匹配 EmailAuthenticationToken）
     * 2. DaoAuthenticationProvider（兜底匹配 UsernamePasswordAuthenticationToken）
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(emailAuthenticationProvider);  // 先
        builder.authenticationProvider(authenticationProvider());      // 后
        return builder.build();
    }
}
