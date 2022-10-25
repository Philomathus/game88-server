package tv.game88.core.admin.config;

import tv.game88.core.admin.security.filter.JwtAuthenticationTokenFilter;
import tv.game88.core.admin.security.handle.AuthenticationEntryPointImpl;
import tv.game88.core.admin.security.handle.LogoutSuccessHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.filter.CorsFilter;

import tv.game88.common.security.config.PermitAllUrlProperties;

import javax.annotation.Resource;

/**
 * spring security配置
 *
 * @author MengJun
 */
@EnableGlobalMethodSecurity( prePostEnabled = true, securedEnabled = true )
public class SecurityConfig implements WebSecurityCustomizer {
    /**
     * 自定义用户认证逻辑
     */
    @Resource
    private UserDetailsService           userDetailsService;
    /**
     * 认证失败处理类
     */
    @Resource
    private AuthenticationEntryPointImpl unauthorizedHandler;
    /**
     * 退出处理类
     */
    @Resource
    private LogoutSuccessHandlerImpl     logoutSuccessHandler;
    /**
     * token认证过滤器
     */
    @Resource
    private JwtAuthenticationTokenFilter authenticationTokenFilter;
    /**
     * 跨域过滤器
     */
    @Resource
    private CorsFilter                   corsFilter;
    /**
     * 允许匿名访问的地址
     */
    @Resource
    private PermitAllUrlProperties       permitAllUrl;

    @Override
    public void customize( WebSecurity webSecurity ) {
        // 配置允许双斜杠
        webSecurity.httpFirewall( allowUrlEncodedSlashHttpFirewall() );
    }

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
        // 注解标记允许匿名访问的url
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry =
                httpSecurity.authorizeRequests();
        permitAllUrl.getUrls().forEach( url -> registry.antMatchers( url ).permitAll() );

        httpSecurity
                // CSRF禁用，因为不使用session
                .csrf().disable()
                // 认证失败处理类
                .exceptionHandling().authenticationEntryPoint( unauthorizedHandler ).and()
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy( SessionCreationPolicy.STATELESS ).and()
                // 过滤请求
                .authorizeRequests()
                // 对于登录login 允许匿名访问
                .antMatchers( "/login" ).anonymous()
                .antMatchers( HttpMethod.GET, "/*.html", "/**/*.html", "/**/*.css", "/**/*.js" ).permitAll()
                .antMatchers( "/doc.html" ).anonymous().antMatchers( "/swagger-resources/**" ).anonymous()
                .antMatchers( "/webjars/**" ).anonymous().antMatchers( "/*/api-docs" ).anonymous().antMatchers( "/actuator/**" )
                .anonymous()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated().and().headers().frameOptions().disable();
        httpSecurity.logout().logoutUrl( "/logout" ).logoutSuccessHandler( logoutSuccessHandler );
        // 添加JWT filter
        httpSecurity.addFilterBefore( authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class );
        // 添加CORS filter
        httpSecurity.addFilterBefore( corsFilter, JwtAuthenticationTokenFilter.class );
        httpSecurity.addFilterBefore( corsFilter, LogoutFilter.class );
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authManager( HttpSecurity httpSecurity ) throws Exception {
        return httpSecurity.getSharedObject( AuthenticationManagerBuilder.class ).userDetailsService( userDetailsService )
                           .passwordEncoder( bCryptPasswordEncoder() ).and().build();
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedDoubleSlash( true );
        return firewall;
    }

    /**
     * 强散列哈希加密实现
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
