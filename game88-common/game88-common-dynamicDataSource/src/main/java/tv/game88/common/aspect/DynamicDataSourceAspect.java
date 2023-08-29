package tv.game88.common.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tv.game88.common.annotation.DataSource;
import tv.game88.common.config.dds.DynamicDataSourceContextHolder;

/**
 * 动态数据源切换处理器
 */
@Log4j2
@Aspect
@Order( -1 )  // 该切面应当先于 @Transactional 执行
@Component
public class DynamicDataSourceAspect {

    /**
     * 切换数据源
     */
    @Before( "@annotation(dataSource))" )
    public void switchDataSource( JoinPoint point, DataSource dataSource ) {
        if ( !DynamicDataSourceContextHolder.containDataSourceKey( dataSource.value() ) ) {
            log.warn( "DataSource [{}] doesn't exist, use default DataSource [{}] " + dataSource.value() );
        } else {
            // 切换数据源
            DynamicDataSourceContextHolder.setDataSourceKey( dataSource.value() );
            log.debug( "Switch DataSource to [" + DynamicDataSourceContextHolder.getDataSourceKey() + "] in Method ["
                    + point.getSignature() + "]" );
        }
    }

    /**
     * 重置数据源
     */
    @After( "@annotation(dataSource))" )
    public void restoreDataSource( JoinPoint point, DataSource dataSource ) {
        // 将数据源置为默认数据源
        DynamicDataSourceContextHolder.clearDataSourceKey();
        log.debug( "Restore DataSource to [" + DynamicDataSourceContextHolder.getDataSourceKey() + "] in Method ["
                + point.getSignature() + "]" );
    }
}