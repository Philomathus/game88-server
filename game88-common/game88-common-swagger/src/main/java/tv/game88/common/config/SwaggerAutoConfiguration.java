package tv.game88.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;

/**
 * Swagger 自动配置
 *
 * @author MengJun
 * @version 1.0.0
 */
@AutoConfiguration
@ConditionalOnProperty( value = "springdoc.api-docs.enable", havingValue = "true" )
@EnableConfigurationProperties( SwaggerProperties.class )
public class SwaggerAutoConfiguration {

    /**
     * Swagger 扩展属性
     */
    @Resource
    private SwaggerProperties swaggerProperties;

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI().info( swaggerProperties.buildApiInfo() );
    }
}
