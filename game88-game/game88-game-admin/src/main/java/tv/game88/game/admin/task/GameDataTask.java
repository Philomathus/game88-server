package tv.game88.game.admin.task;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.game.api.service.GameDataService;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 游戏数据打码
 */
@Log4j2
@Component
public class GameDataTask {
    @Resource
    private RedisUtils      redisUtils;
    @Resource
    private GameDataService gameDataService;

    @Scheduled( cron = "0/30 * * * * ?" )
    public void runTask1() {
        if ( !redisUtils.lock( "GameDataTaskRunTask1", 120 ) ) {
            return;
        }
        LocalDateTime now = LocalDateTimeUtils.convertToUTC8( LocalDateTime.now() );
        Thread.ofVirtual().start( () -> {
            try {
                LocalDateTime starDay = now.minusMinutes( 3 );
                gameDataService.beatGameCodeAgent( starDay, now, null, null );
            } catch ( Exception e ) {
                log.error( "1游戏拉取注单异常{}", e.getMessage(), e );
            }
        } );
        Thread.ofVirtual().start( () -> {
            try {
                LocalDateTime starDay = now.minusMinutes( 5 );
                LocalDateTime endDay  = now.minusMinutes( 3 );
                gameDataService.beatGameCodeAgent( starDay, endDay, null, null );
            } catch ( Exception e ) {
                log.error( "2游戏拉取注单异常{}", e.getMessage(), e );
            }
        } );
        Thread.ofVirtual().start( () -> {
            try {
                LocalDateTime starDay = now.minusMinutes( 8 );
                LocalDateTime endDay  = now.minusMinutes( 5 );
                gameDataService.beatGameCodeAgent( starDay, endDay, null, null );
            } catch ( Exception e ) {
                log.error( "3游戏拉取注单异常{}", e.getMessage(), e );
            }
        } );
    }

    @Scheduled( cron = "0 * 0-23 * * ?" ) // 每小时拉一次
    public void runTask2() {
        if ( !redisUtils.lock( "GameDataTaskRunTask2", Duration.ofMinutes( 50 ) ) ) {
            return;
        }
        LocalDateTime now = LocalDateTimeUtils.convertToUTC8( LocalDateTime.now() );
        Thread.ofVirtual().start( () -> {
            try {
                LocalDateTime startTime = now.minusHours( 2 );
                LocalDateTime endTime   = now.minusHours( 1 );
                gameDataService.beatGameCodeAgent( startTime, endTime, null, null );
            } catch ( Exception e ) {
                log.error( "1游戏拉取注单异常{}", e.getMessage(), e );
            }
        } );
    }
}
