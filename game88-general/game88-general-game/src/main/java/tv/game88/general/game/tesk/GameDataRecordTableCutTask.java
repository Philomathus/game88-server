package tv.game88.general.game.tesk;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.general.api.service.GameDataRecordService;

import javax.annotation.Resource;


@Log4j2
@Component
public class GameDataRecordTableCutTask {
    @Resource
    private GameDataRecordService gameDataRecordService;
    @Resource
    private RedisUtils            redisUtils;

    //每天凌晨一点执行一次
    @Scheduled( cron = "0 0 1 * * ?" )
    public void gameDataRecordTableCut() {
        if ( !redisUtils.lock( this.getClass().getSimpleName(), 30 ) ) {
            return;
        }
        log.info( "开始执行游戏注单数据表建表任务" );
        gameDataRecordService.cutTable( 5 );
    }
}
