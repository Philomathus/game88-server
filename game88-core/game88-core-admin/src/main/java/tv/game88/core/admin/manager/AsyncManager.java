package tv.game88.core.admin.manager;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.SpringUtils;

import jakarta.annotation.PreDestroy;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务管理器
 *
 * @author MengJun
 */
@Log4j2
@Component
public class AsyncManager {
	private static final AsyncManager ME = new AsyncManager();
	/**
	 * 操作延迟10毫秒
	 */
	private final int OPERATE_DELAY_TIME = 10;
	/**
	 * 异步操作任务调度线程池
	 */
	private final ScheduledExecutorService executor = SpringUtils.getBean( "scheduledExecutorService" );

	/**
	 * 单例模式
	 */
	private AsyncManager() {
	}

	public static AsyncManager me() {
		return ME;
	}

	/**
	 * 执行任务
	 *
	 * @param task 任务
	 */
	public void execute( TimerTask task ) {
		executor.schedule( task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS );
	}

	/**
	 * 停止任务线程池
	 */
	@PreDestroy
	public void shutdown() {
		if ( !executor.isShutdown() ) {
			executor.shutdown();
			try {
				if ( !executor.awaitTermination( 120, TimeUnit.SECONDS ) ) {
					executor.shutdownNow();
					if ( !executor.awaitTermination( 120, TimeUnit.SECONDS ) ) {
						log.warn( "Pool did not terminate" );
					}
				}
			} catch ( InterruptedException ie ) {
				executor.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}
}
