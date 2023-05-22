package tv.game88.general.game.tesk;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.RandomUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.api.mapper.GamePlatformMapper;
import tv.game88.general.api.service.GameDataRecordService;
import tv.game88.general.game.base.BaseGamePull;
import tv.game88.general.game.base.GamePullDockFactoryUtil;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class RemoteGameDataRecordTask {
    @Resource
    private RedisUtils               redisUtils;
    @Resource
    private ScheduledExecutorService scheduledExecutorService;
    @Resource
    private GamePullDockFactoryUtil  gamePullDockFactoryUtil;
    @Resource
    private GamePlatformMapper       gamePlatformMapper;
    @Resource
    private GameDataRecordService    gameDataRecordService;

    @Scheduled( cron = "0/15 * * * * ?" ) // 每15秒执行一次
    public void remoteGameDataRecord() {
        if ( !redisUtils.lock( this.getClass().getSimpleName(), 14 ) ) {
            return;
        }
        log.info( "开始执行游戏注单数据拉取任务" );

        List<GamePlatform> gamePlatformList = new QueryChainWrapper<>( gamePlatformMapper ).eq( "effect", 1 ).list();
        for ( GamePlatform gamePlatform : gamePlatformList ) {
            BaseGamePull baseGamePull = gamePullDockFactoryUtil.createGamePullProcessor( gamePlatform.getGameCategory() );
            scheduledExecutorService.schedule( () -> {
                try {
                    List<Map<String, Object>> remoteGameData = baseGamePull.requestRemoteGameData( gamePlatform );
                    if ( !CollectionUtils.isEmpty( remoteGameData ) ) {
                        List<GameDataRecord> gameDataRecords = baseGamePull.handleResult( remoteGameData, gamePlatform );
                        gameDataRecordService.batchInsert( gameDataRecords );
                    }
                } catch ( Exception e ) {
                    log.error( e.getMessage(), e );
                }

            }, RandomUtils.randomIntWithMax( 0, 5 ), TimeUnit.SECONDS );
        }
    }
}
