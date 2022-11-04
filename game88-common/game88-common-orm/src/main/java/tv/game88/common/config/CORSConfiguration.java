package tv.game88.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@Configuration
public class CORSConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers( ResourceHandlerRegistry registry ) {
        /** swagger配置 */
        registry.addResourceHandler( "/swagger-ui/**" ).addResourceLocations( "classpath:/META-INF/resources/" );
        registry.addResourceHandler( "/webjars/**" ).addResourceLocations( "classpath:/META-INF/resources/webjars/" );
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration               config = new CorsConfiguration();
        //config.setAllowCredentials( true );

        // 设置访问源地址
        config.addAllowedOrigin( "*" );
        // 设置访问源请求头
        config.addAllowedHeader( "*" );
        // 设置访问源请求方法
        config.addAllowedMethod( "*" );
        // 对接口配置跨域设置
        source.registerCorsConfiguration( "/**", config );
        return new CorsFilter( source );
    }

    /**
     * Json提交去空格
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.deserializerByType( String.class,
                new StdScalarDeserializer<String>( String.class ) {
            @Override
            public String deserialize( JsonParser jsonParser, DeserializationContext ctx ) throws IOException {
                return StringUtils.trimWhitespace( jsonParser.getValueAsString() );
            }
        } );
    }
}
