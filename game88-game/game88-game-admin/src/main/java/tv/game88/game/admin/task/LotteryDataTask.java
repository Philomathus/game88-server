package tv.game88.game.admin.task;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.game.api.service.GameDataService;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 彩票数据打码
 */
@Log4j2
@Component
public class LotteryDataTask {
    @Resource
    private RedisUtils      redisUtils;
    @Resource
    private GameDataService gameDataService;

    @Scheduled( fixedDelay = 60000, initialDelay = 5000 )
    public void runTask() {
        if ( !redisUtils.lock( "LotteryDataTask", 20 ) ) {
            return;
        }
        LocalDateTime endDay  = LocalDateTime.now();
        LocalDateTime starDay = endDay.minusMinutes( 2 );
        String        begin   = LocalDateTimeUtils.format( starDay );
        String        end     = LocalDateTimeUtils.format( endDay );
        try {
            gameDataService.beatLotteryCode( begin, end );
        } catch ( Exception e ) {
            log.error( "彩票拉取注单异常{}", e.getMessage(), e );
        }
    }
}
