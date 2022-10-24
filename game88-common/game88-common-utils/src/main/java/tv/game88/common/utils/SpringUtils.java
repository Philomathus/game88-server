package tv.game88.common.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

/**
 * spring工具类 方便在非spring管理环境中获取bean
 *
 * @author MengJun
 */
@Log4j2
@Component
public final class SpringUtils implements BeanFactoryPostProcessor, ApplicationContextAware, EmbeddedValueResolverAware {
	/**
	 * Spring应用上下文环境
	 */
	private static ConfigurableListableBeanFactory beanFactory;

	private static ApplicationContext applicationContext;

	private static StringValueResolver stringValueResolver;

	@Override
	public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException {
		SpringUtils.beanFactory = beanFactory;
	}

	@Override
	public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
		SpringUtils.applicationContext = applicationContext;
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver stringValueResolver) {
		SpringUtils.stringValueResolver = stringValueResolver;
	}

	/**
	 * 获取对象
	 *
	 * @return Object 一个以所给名字注册的bean的实例
	 * @throws BeansException
	 */
	@SuppressWarnings( "unchecked" )
	public static <T> T getBean( String name ) throws BeansException {
		return ( T ) beanFactory.getBean( name );
	}

	/**
	 * 获取类型为requiredType的对象
	 *
	 * @return
	 * @throws BeansException
	 */
	public static <T> T getBean( Class<T> clz ) throws BeansException {
		return beanFactory.getBean( clz );
	}

	/**
	 * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
	 *
	 * @return boolean
	 */
	public static boolean containsBean( String name ) {
		return beanFactory.containsBean( name );
	}

	/**
	 * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
	 *
	 * @return boolean
	 * @throws NoSuchBeanDefinitionException
	 */
	public static boolean isSingleton( String name ) throws NoSuchBeanDefinitionException {
		return beanFactory.isSingleton( name );
	}

	/**
	 * @return Class 注册对象的类型
	 * @throws NoSuchBeanDefinitionException
	 */
	public static Class<?> getType( String name ) throws NoSuchBeanDefinitionException {
		return beanFactory.getType( name );
	}

	/**
	 * 如果给定的bean名字在bean定义中有别名，则返回这些别名
	 *
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 */
	public static String[] getAliases( String name ) throws NoSuchBeanDefinitionException {
		return beanFactory.getAliases( name );
	}

	/**
	 * 获取aop代理对象
	 *
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public static <T> T getAopProxy( T invoker ) {
		return ( T ) AopContext.currentProxy();
	}

	/**
	 * 获取当前的环境配置，无配置返回null
	 *
	 * @return 当前的环境配置
	 */
	public static String[] getActiveProfiles() {
		return applicationContext.getEnvironment().getActiveProfiles();
	}

	/**
	 * 获取当前的环境配置，当有多个环境配置时，只获取第一个
	 *
	 * @return 当前的环境配置
	 */
	public static String getActiveProfile() {
		return getActiveProfiles()[ 0 ];
	}

	/**
	 * 动态获取配置文件中的值
	 *
	 * @param name
	 * @return
	 */
	public static String getPropertiesValue(String name) {
		try {
			return stringValueResolver.resolveStringValue("${" + name + "}");
		} catch (Exception e) {
			log.error(String.format("当前环境变量中没有{%s}的配置", name));
			// 获取失败则返回null
			return null;
		}
	}

}
