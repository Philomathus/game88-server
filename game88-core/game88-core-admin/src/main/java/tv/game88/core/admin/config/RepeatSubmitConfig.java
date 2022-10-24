package tv.game88.core.admin.config;

import tv.game88.core.admin.interceptor.RepeatSubmitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class RepeatSubmitConfig implements WebMvcConfigurer {
	@Resource
	private RepeatSubmitInterceptor repeatSubmitInterceptor;

	/**
	 * 自定义拦截规则
	 */
	@Override
	public void addInterceptors( InterceptorRegistry registry ) {
		registry.addInterceptor( repeatSubmitInterceptor ).addPathPatterns( "/**" );
	}
}
