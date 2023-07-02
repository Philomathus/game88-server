package tv.game88.game.admin.task;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.game.api.service.GameDataService;

import javax.annotation.Resource;
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

    @Async
    @Scheduled( cron = "0/30 * * * * ?")
    public void runTask() {
        log.warn( 0 );
        if ( !redisUtils.lock( "GameDataTask", 10 ) ) {
            log.warn( 1 );
            return;
        }
        log.warn( 2 );
        LocalDateTime endDay  = LocalDateTime.now();
        LocalDateTime starDay = endDay.minusMinutes( 3 );
        String        begin   = LocalDateTimeUtils.format( starDay );
        String        end     = LocalDateTimeUtils.format( endDay );
        if ( LocalDateTimeUtils.isSameDay( starDay, endDay ) ) {
            try {
                gameDataService.beatGameCodeAgent( begin, begin, end, null, null );
            } catch ( Exception e ) {
                log.error( "1游戏拉取注单异常{}", e.getMessage(), e );
            }
            endDay  = LocalDateTime.now().minusMinutes( 7 );
            starDay = endDay.minusMinutes( 3 );
            begin   = LocalDateTimeUtils.format( starDay );
            end     = LocalDateTimeUtils.format( endDay );

            try {
                gameDataService.beatGameCodeAgent( begin, begin, end, null, null );
            } catch ( Exception e ) {
                log.error( "4游戏拉取注单异常{}", e.getMessage(), e );
            }
        } else {
            end = LocalDateTimeUtils.format( starDay.plusMinutes( 5 ).toLocalDate().atStartOfDay() );
            try {
                gameDataService.beatGameCodeAgent( begin, begin, end, null, null );
            } catch ( Exception e ) {
                log.error( "2游戏拉取注单异常{}", e.getMessage(), e );
            }
            begin = end;
            end   = LocalDateTimeUtils.format( endDay );
            try {
                gameDataService.beatGameCodeAgent( end, begin, end, null, null );
            } catch ( Exception e ) {
                log.error( "3游戏拉取注单异常{}", e.getMessage(), e );
            }
        }
    }
}
