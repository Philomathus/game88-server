package tv.game88.core.admin.manager;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

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
	 * 异步操作任务调度线程池
	 */
	@Resource
	private ScheduledExecutorService scheduledExecutorService;

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
		/**
		 * 操作延迟10毫秒
		 */
		scheduledExecutorService.schedule( task, 10, TimeUnit.MILLISECONDS );
	}

	/**
	 * 停止任务线程池
	 */
	@PreDestroy
	public void shutdown() {
		if ( !scheduledExecutorService.isShutdown() ) {
			scheduledExecutorService.shutdown();
			try {
				if ( !scheduledExecutorService.awaitTermination( 120, TimeUnit.SECONDS ) ) {
					scheduledExecutorService.shutdownNow();
					if ( !scheduledExecutorService.awaitTermination( 120, TimeUnit.SECONDS ) ) {
						log.warn( "Pool did not terminate" );
					}
				}
			} catch ( InterruptedException ie ) {
				scheduledExecutorService.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
	}
}
