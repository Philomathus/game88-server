package tv.game88.general.game.tesk;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RandomUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.game.type.EnumGameCategory;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.api.entity.GameRecordVersion;
import tv.game88.general.api.mapper.GamePlatformMapper;
import tv.game88.general.api.mapper.GameRecordVersionMapper;
import tv.game88.general.api.service.GameDataRecordService;
import tv.game88.general.game.base.BaseGamePull;
import tv.game88.general.game.base.GamePullDockFactoryUtil;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private GameRecordVersionMapper  gameRecordVersionMapper;
    @Resource
    private GameDataRecordService    gameDataRecordService;

    @Scheduled( cron = "0/20 * * * * ?" ) // 每20秒执行一次
    public void remoteGameDataRecord() {
        if ( !redisUtils.lock( this.getClass().getSimpleName(), 14 ) ) {
            return;
        }
        List<GamePlatform> gamePlatformList = new QueryChainWrapper<>( gamePlatformMapper ).eq( "effect", 1 ).list();
        for ( GamePlatform gamePlatform : gamePlatformList ) {
            GameRecordVersion gameRecordVersion = gameRecordVersionMapper.selectById( gamePlatform.getId() );
            if ( gameRecordVersion == null ) {
                continue;
            }
            scheduledExecutorService.schedule( () -> {
                log.info( "开始执行{}注单拉取程序, 开始时间:{}", gamePlatform.getName(), gameRecordVersion.getVersionValue() );

                gamePlatform.setVersionValue( gameRecordVersion.getVersionValue() );

                BaseGamePull baseGamePull = gamePullDockFactoryUtil.createGamePullProcessor( gamePlatform.getGameCategory() );
                try {
                    List<Object> remoteGameData = baseGamePull.requestRemoteGameData( gamePlatform );
                    if ( !CollectionUtils.isEmpty( remoteGameData ) ) {
                        log.info( "{}拉取数据成功, 条数:{}", gamePlatform.getName(), remoteGameData.size() );
                        List<GameDataRecord> gameDataRecords = new ArrayList<>();
                        for ( Object remoteGameDatum : remoteGameData ) {
                            GameDataRecord gameDataRecord = baseGamePull.handleResult( remoteGameDatum, gamePlatform );
                            if ( gameDataRecord != null ) {
                                gameDataRecords.add( gameDataRecord );
                            }
                        }
                        gameDataRecordService.batchInsert( gameDataRecords, gamePlatform );

                        if ( gamePlatform.getGameCategory() != EnumGameCategory.BBIN
                                && gamePlatform.getGameCategory() != EnumGameCategory.AG ) {
                            LocalDateTime localDateTime = null;
                            for ( GameDataRecord gameDataRecord : gameDataRecords ) {
                                LocalDateTime gameEndTime =
                                        LocalDateTimeUtils.parseLocalDateTime( gameDataRecord.getGameEndTime() );
                                if ( localDateTime == null || gameEndTime.isAfter( localDateTime ) ) {
                                    localDateTime = gameEndTime;
                                }
                            }
                            if ( localDateTime != null ) {
                                gameRecordVersion.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( localDateTime ) ) );
                            }
                        }
                    }
                    if ( !StringUtils.equals( gamePlatform.getVersionValue(), gameRecordVersion.getVersionValue() ) ) {
                        gameRecordVersion.setVersionValue( gamePlatform.getVersionValue() );
                        gameRecordVersionMapper.updateById( gameRecordVersion );
                    }
                } catch ( Exception e ) {
                    log.error( e.getMessage(), e );
                }

            }, RandomUtils.randomIntWithMax( 0, 5 ), TimeUnit.SECONDS );
        }
    }
}
