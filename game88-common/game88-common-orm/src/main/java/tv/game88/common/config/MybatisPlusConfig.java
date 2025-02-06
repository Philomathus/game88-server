package tv.game88.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tv.game88.common.config.injector.EasySqlInjector;
import tv.game88.common.helper.RequestDataHelper;
import tv.game88.common.utils.LocalDateTimeUtils;

import java.time.LocalDate;
import java.util.Map;

import static tv.game88.common.utils.LocalDateTimeUtils.YYYYMMDD_FORMATTER;

@EnableTransactionManagement
@Configuration
@MapperScan( "tv.game88.**.mapper" )
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor( new PaginationInnerInterceptor( DbType.MYSQL ) );

        // 动态表名
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler( ( sql, tableName ) -> {
            // 获取参数方法
            Map<String, Object> paramMap = RequestDataHelper.getRequestData();
            if ( paramMap == null ) {
                return tableName;
            }
            // log_money
            if ( tableName.equalsIgnoreCase( "log_money" ) && paramMap.containsKey( "userId" ) ) {
                String userId = paramMap.get( "userId" ).toString();
                return tableName + userId.substring( userId.length() - 1 );
            }
            // member_game_data
            if ( tableName.equalsIgnoreCase( "member_game_data" ) && paramMap.containsKey( "userId" ) ) {
                String userId = paramMap.get( "userId" ).toString();
                return tableName + userId.substring( userId.length() - 1 );
            }
            // member_game_data
            if ( tableName.equalsIgnoreCase( "game_data_record" ) && paramMap.containsKey( "time" ) ) {
                LocalDate time = ( LocalDate ) paramMap.get( "time" );
                return tableName + "_" + LocalDateTimeUtils.format( time, YYYYMMDD_FORMATTER );
            }
            return tableName;
        } );
        interceptor.addInnerInterceptor( dynamicTableNameInnerInterceptor );

        return interceptor;
    }

    @Bean
    public EasySqlInjector easySqlInjector() {
        return new EasySqlInjector();
    }
}