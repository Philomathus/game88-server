package tv.game88.common.config;

import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tv.game88.common.config.dds.DynamicDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@MapperScan( basePackages = { "tv.game88.**.mapper" } ) // 扫描Mapper
@EnableTransactionManagement
public class MybatisConfig {

	@Bean( "PrimaryDataSource" )
	@Primary
	@ConfigurationProperties( prefix = "spring.datasource.primary" )
	public DataSource primaryDataSource() {
		return DataSourceBuilder.create().type( HikariDataSource.class ).build();
	}

	@Bean( "SecondaryDataSource" )
	@ConfigurationProperties( prefix = "spring.datasource.secondary" )
	public DataSource secondaryDataSource() {
		return DataSourceBuilder.create().type( HikariDataSource.class ).build();
	}

	@Bean( "dynamicDataSource" )
	public DataSource dynamicDataSource( @Qualifier( "PrimaryDataSource" ) DataSource primaryDataSource,
										 @Qualifier( "SecondaryDataSource" ) DataSource secondaryDataSource ) {
		Map<Object, Object> dataSourceMap     = new HashMap<>( 2 );
		dataSourceMap.put( "primaryDataSource", primaryDataSource );
		dataSourceMap.put( "secondaryDataSource", secondaryDataSource );

		DynamicDataSource dynamicDataSource = new DynamicDataSource();
		// 将 master 数据源作为默认指定的数据源
		dynamicDataSource.setDefaultDataSource( primaryDataSource );
		// 将 master 和 slave 数据源作为指定的数据源
		dynamicDataSource.setDataSources( dataSourceMap );
		return dynamicDataSource;
	}

	@Bean
	public SqlSessionFactoryBean sqlSessionFactoryBean( @Qualifier( "dynamicDataSource" ) DataSource dynamicDataSource,
														MybatisProperties mybatisProperties ) throws Exception {
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		// 配置数据源，此处配置为关键配置，如果没有将 dynamicDataSource作为数据源则不能实现切换
		sessionFactory.setDataSource( dynamicDataSource );
		// 扫描Model
		sessionFactory.setTypeAliasesPackage( "com.qiqilm.server.admin.domain" );
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		// 扫描映射文件
		sessionFactory.setMapperLocations( resolver.getResources( "classpath*:mapper/*.xml" ) );
		sessionFactory.setConfiguration( mybatisProperties.getConfiguration() );
		return sessionFactory;
	}

	@Bean
	public DataSourceTransactionManager transactionManager( @Qualifier( "dynamicDataSource" ) DataSource dynamicDataSource ) {
		// 配置事务管理, 使用事务时在方法头部添加@Transactional注解即可
		return new DataSourceTransactionManager( dynamicDataSource );
	}
}