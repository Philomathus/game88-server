package tv.game88.common.config;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 线程池配置
 *
 * @author MengJun
 **/
@Log4j2
@Configuration
public class ThreadPoolConfig {
    // 核心线程池大小
    private static final int corePoolSize = 20;

    /**
     * 打印线程异常信息
     */
    private static void printException( Runnable r, Throwable t ) {
        if ( t == null && r instanceof Future<?> ) {
            try {
                Future<?> future = ( Future<?> ) r;
                if ( future.isDone() ) {
                    future.get();
                }
            } catch ( CancellationException ce ) {
                t = ce;
            } catch ( ExecutionException ee ) {
                t = ee.getCause();
            } catch ( InterruptedException ie ) {
                Thread.currentThread().interrupt();
            }
        }
        if ( t != null ) {
            log.error( t.getMessage(), t );
        }
    }

    /**
     * 执行周期性或定时任务
     */
    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor( corePoolSize, new BasicThreadFactory.Builder()
                .wrappedFactory( Thread.ofVirtual().factory() )
                .namingPattern( "schedule-pool-%d" )
                .daemon( true )
                .build() ) {
            @Override
            protected void afterExecute( Runnable r, Throwable t ) {
                super.afterExecute( r, t );
                ThreadPoolConfig.printException( r, t );
            }
        };
    }

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor( Executors.newVirtualThreadPerTaskExecutor() );
        };
    }
}
