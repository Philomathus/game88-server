package tv.game88.common.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.annotation.Resource;

/**
 * Swagger 自动配置
 *
 * @author MengJun
 * @version 1.0.0
 */
@AutoConfiguration
@EnableOpenApi
@ConditionalOnProperty( value = "knife4j.enable" , havingValue = "true" )
@EnableConfigurationProperties( SwaggerProperties.class )
public class SwaggerAutoConfiguration {

	/**
	 * Swagger 扩展属性
	 */
	@Resource
	private SwaggerProperties swaggerProperties;

	/**
	 * 记事表 Bean
	 *
	 * @return 返回记事表
	 */
	@Bean( value = "defaultApi3" )
	public Docket docketBean() {
		// 获取扫描的基本包路径。
		String baseScanPackage = swaggerProperties.getBaseScanPackage();
		// 如果扩展指定扫描包路径存在，则配置包扫描选择。
		if ( StringUtils.isNotBlank( baseScanPackage ) ) {
			return new Docket( DocumentationType.OAS_30 )
					.apiInfo( swaggerProperties.buildApiInfo() )
					.select()
					.paths( PathSelectors.any() )
					.build();
		} else {
			return new Docket( DocumentationType.OAS_30 )
					.apiInfo( swaggerProperties.buildApiInfo() )
					.select()
					.apis( RequestHandlerSelectors.basePackage( baseScanPackage ) )
					.paths( PathSelectors.any() )
					.build();
		}
	}
}
