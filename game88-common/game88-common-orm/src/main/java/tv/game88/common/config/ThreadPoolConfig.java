package tv.game88.common.config;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ForkJoinPoolFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
    private final int corePoolSize = 50;

    // 最大可创建的线程数
    private final int maxPoolSize = 200;

    // 队列最大长度
    private final int queueCapacity = 1000;

    // 线程池维护线程所允许的空闲时间
    private final int keepAliveSeconds = 300;

    /**
     * 打印线程异常信息
     */
    private static void printException( Runnable r, Throwable t ) {
        if ( t == null && r instanceof Future<?> ) {
            try {
                Future<?> future = (Future<?>) r;
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

    @Bean( name = "threadPoolTaskExecutor" )
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize( maxPoolSize );
        executor.setCorePoolSize( corePoolSize );
        executor.setQueueCapacity( queueCapacity );
        executor.setKeepAliveSeconds( keepAliveSeconds );
        // 线程池对拒绝任务(无线程可用)的处理策略
        executor.setRejectedExecutionHandler( new ThreadPoolExecutor.CallerRunsPolicy() );
        return executor;
    }

    /**
     * 执行周期性或定时任务
     */
    @Bean( name = "scheduledExecutorService" )
    protected ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor( corePoolSize, new BasicThreadFactory.Builder()
                .namingPattern( "schedule-pool-%d" ).daemon( true ).build() ) {
            @Override
            protected void afterExecute( Runnable r, Throwable t ) {
                super.afterExecute( r, t );
                ThreadPoolConfig.printException( r, t );
            }
        };
    }

    @Bean
    public ForkJoinPoolFactoryBean forkJoinPoolFactoryBean() {
        final ForkJoinPoolFactoryBean poolFactory = new ForkJoinPoolFactoryBean();
        poolFactory.setCommonPool( true );
        poolFactory.setParallelism( 2 * Runtime.getRuntime().availableProcessors() );
        return poolFactory;
    }
}
