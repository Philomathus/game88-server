package tv.game88.wallet.app.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.CorsFilter;
import tv.game88.common.security.config.PermitAllUrlProperties;
import tv.game88.wallet.app.security.filter.MemberAuthenticationTokenFilter;
import tv.game88.wallet.app.security.handle.MemberAuthenticationEntryPointHandle;
import tv.game88.wallet.app.security.handle.MemberLogoutSuccessHandle;

/**
 * spring security配置
 *
 * @author MengJun
 */
@Configuration
@EnableMethodSecurity( securedEnabled = true )
public class MemberSecurityConfig {
    /**
     * 自定义用户认证逻辑
     */
    @Resource
    private UserDetailsService                   userDetailsService;
    /**
     * 认证失败处理类
     */
    @Resource
    private MemberAuthenticationEntryPointHandle unauthorizedHandler;
    /**
     * 退出处理类
     */
    @Resource
    private MemberLogoutSuccessHandle            logoutSuccessHandler;
    /**
     * token认证过滤器
     */
    @Resource
    private MemberAuthenticationTokenFilter      authenticationTokenFilter;
    /**
     * 跨域过滤器
     */
    @Resource
    private CorsFilter                           corsFilter;
    /**
     * 允许匿名访问的地址
     */
    @Resource
    private PermitAllUrlProperties               permitAllUrl;

    /**
     * anyRequest          |   匹配所有请求路径
     * access              |   SpringEl表达式结果为true时可以访问
     * anonymous           |   匿名可以访问
     * denyAll             |   用户不能访问
     * fullyAuthenticated  |   用户完全认证可以访问（非remember-me下自动登录）
     * hasAnyAuthority     |   如果有参数，参数表示权限，则其中任何一个权限可以访问
     * hasAnyRole          |   如果有参数，参数表示角色，则其中任何一个角色可以访问
     * hasAuthority        |   如果有参数，参数表示权限，则其权限可以访问
     * hasIpAddress        |   如果有参数，参数表示IP地址，如果用户IP和参数匹配，则可以访问
     * hasRole             |   如果有参数，参数表示角色，则其角色可以访问
     * permitAll           |   用户可以任意访问
     * rememberMe          |   允许通过remember-me登录的用户访问
     * authenticated       |   用户登录后可访问
     */
    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity httpSecurity ) throws Exception {
        httpSecurity
                // CSRF禁用，因为不使用session
                .csrf( AbstractHttpConfigurer::disable )
                // 基于token，所以不需要session
                .sessionManagement( configurer -> configurer.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
                // 过滤请求
                .authorizeHttpRequests( customizer -> {
                    // 注解标记允许匿名访问的url
                    permitAllUrl.getUrls().forEach( url -> customizer.requestMatchers( url ).permitAll() );
                    customizer
                            .requestMatchers( "/swagger-ui/**", "/v3/api-docs/**", "/*.html", "/*.ico" ).anonymous()
                            // actuator 健康检查
                            .requestMatchers( "/actuator/**" ).anonymous()
                            // 除上面外的所有请求全部需要鉴权认证
                            .anyRequest().authenticated();
                } )
                .headers( customizer -> customizer.frameOptions( HeadersConfigurer.FrameOptionsConfig::disable ).disable() )
                .logout( customizer -> customizer.logoutUrl( "/logout" ).logoutSuccessHandler( logoutSuccessHandler ) )
                // 认证失败处理类
                .exceptionHandling( configurer -> configurer.authenticationEntryPoint( unauthorizedHandler ) )
                // 添加JWT filter
                .addFilterBefore( authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class )
                // 添加CORS filter
                .addFilterBefore( corsFilter, MemberAuthenticationTokenFilter.class )
                .addFilterBefore( corsFilter, LogoutFilter.class );
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService( userDetailsService );
        authProvider.setPasswordEncoder( getPasswordEncoder() );
        return authProvider;
    }

    @Bean
    public AuthenticationManager authManager( AuthenticationConfiguration config ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean( name = "myPasswordEncoder" )
    public PasswordEncoder getPasswordEncoder() {
        DelegatingPasswordEncoder delPasswordEncoder =
                ( DelegatingPasswordEncoder ) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
        delPasswordEncoder.setDefaultPasswordEncoderForMatches( bcryptPasswordEncoder );
        return delPasswordEncoder;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // 配置允许双斜杠
        return ( webSecurity ) -> webSecurity.httpFirewall( allowUrlEncodedSlashHttpFirewall() );
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash( true );
        return firewall;
    }

    // 解决RequestContextHolder.getRequestAttributes()可能为空的问题
    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
