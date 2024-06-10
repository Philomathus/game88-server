package tv.game88.general.game.tesk;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RandomUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.api.entity.GameRecordFixVersion;
import tv.game88.general.api.entity.GameRecordVersion;
import tv.game88.general.api.mapper.GamePlatformMapper;
import tv.game88.general.api.mapper.GameRecordFixVersionMapper;
import tv.game88.general.api.mapper.GameRecordVersionMapper;
import tv.game88.general.api.service.GameDataRecordService;
import tv.game88.general.game.base.BaseGamePull;
import tv.game88.general.game.base.GamePullDockFactoryUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class RemoteGameDataRecordTask {
    @Resource
    private RedisUtils                 redisUtils;
    @Resource
    private ScheduledExecutorService   scheduledExecutorService;
    @Resource
    private GamePullDockFactoryUtil    gamePullDockFactoryUtil;
    @Resource
    private GamePlatformMapper         gamePlatformMapper;
    @Resource
    private GameRecordVersionMapper    gameRecordVersionMapper;
    @Resource
    private GameRecordFixVersionMapper gameRecordFixVersionMapper;
    @Resource
    private GameDataRecordService      gameDataRecordService;

    @Scheduled( cron = "0 0 0,6,12,18 * * ?" ) // 每天0/6/12/18点执行一次
    public void fixRecordPPEveryDay() {
        GameRecordFixVersion gameRecordFixVersion = new GameRecordFixVersion();
        gameRecordFixVersion.setPlatformId( 49L );
        gameRecordFixVersion.setVersionValue(
                LocalDateTimeUtils.localDateToTimestamp( LocalDateTime.now().minusHours( 8 ) ) + "" );
        gameRecordFixVersionMapper.insert( gameRecordFixVersion );
    }

    @Scheduled( cron = "0/15 * * * * ?" ) // 每15秒执行一次
    public void remoteGameDataRecord() {
        List<GamePlatform> gamePlatformList = gamePlatformMapper.selectGamePlatformAndVersionList();
        for ( GamePlatform gamePlatform : gamePlatformList ) {
            scheduledExecutorService.schedule( () -> {
                if ( redisUtils.lock( "remoteGameDataRecord:" + gamePlatform.getId(), 12000 ) ) {
                    String name         = gamePlatform.getName() + "-" + gamePlatform.getId() + "注单拉取";
                    String versionValue = gamePlatform.getVersionValue();
                    if ( StringUtils.isNumeric( versionValue ) && versionValue.length() == 13 ) {
                        LocalDateTime versionTime = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( versionValue ) );
                        log.info( "开始执行{}程序, 开始时间:{}", name, LocalDateTimeUtils.format( versionTime ) );
                    } else {
                        log.info( "开始执行{}程序, 开始版本:{}", name, versionValue );
                    }
                    try {
                        this.gamePullAndInsert( gamePlatform, name );
                        if ( !StringUtils.equals( gamePlatform.getVersionValue(), versionValue ) ) {
                            GameRecordVersion update = new GameRecordVersion();
                            update.setPlatformId( gamePlatform.getId() );
                            update.setVersionValue( gamePlatform.getVersionValue() );
                            gameRecordVersionMapper.updateById( update );
                        }
                    } catch ( Exception e ) {
                        log.error( e.getMessage(), e );
                    } finally {
                        redisUtils.unLock( "remoteGameDataRecord:" + gamePlatform.getId() );
                    }
                }
            }, RandomUtils.randomIntWithMax( 0, 5 ), TimeUnit.SECONDS );
        }
    }

    @Scheduled( cron = "0/15 * * * * ?" ) // 每15秒执行一次
    public void remoteGameDataRecordFix() {
        List<GamePlatform> gamePlatformList = gamePlatformMapper.selectGamePlatformAndVersionFixList();
        for ( GamePlatform gamePlatform : gamePlatformList ) {
            scheduledExecutorService.schedule( () -> {
                if ( redisUtils.lock( "remoteGameDataRecordFix:" + gamePlatform.getId(), 12000 ) ) {
                    String name = gamePlatform.getName() + "-" + gamePlatform.getId() + "补单拉取";
                    gamePlatform.setFix( true );
                    String versionValue = gamePlatform.getVersionValue();
                    if ( StringUtils.isNumeric( versionValue ) && versionValue.length() == 13 ) {
                        LocalDateTime versionTime = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( versionValue ) );
                        log.info( "开始执行{}程序, 开始时间:{}", name, LocalDateTimeUtils.format( versionTime ) );
                    } else {
                        log.info( "开始执行{}程序, 开始版本:{}", name, versionValue );
                    }

                    try {
                        this.gamePullAndInsert( gamePlatform, name );
                        if ( !StringUtils.equals( gamePlatform.getVersionValue(), versionValue ) ) {
                            GameRecordFixVersion update = new GameRecordFixVersion();
                            update.setPlatformId( gamePlatform.getId() );
                            update.setVersionValue( gamePlatform.getVersionValue() );
                            gameRecordFixVersionMapper.updateById( update );
                        }
                        // 如果补单程序
                        GameRecordVersion gameRecordVersion = gameRecordVersionMapper.selectById( gamePlatform.getId() );
                        if ( gameRecordVersion != null && Long.parseLong( gameRecordVersion.getVersionValue() )
                                - Long.parseLong( gamePlatform.getVersionValue() ) <= 300000 ) {
                            log.warn( "{}, 补单结束 - 已执行到相近时间段:{}", name, versionValue );
                            gameRecordFixVersionMapper.deleteById( gamePlatform.getId() );
                        }
                    } catch ( Exception e ) {
                        log.error( e.getMessage(), e );
                    } finally {
                        redisUtils.unLock( "remoteGameDataRecordFix:" + gamePlatform.getId() );
                    }
                }
            }, RandomUtils.randomIntWithMax( 0, 5 ), TimeUnit.SECONDS );
        }
    }


    private void gamePullAndInsert( GamePlatform gamePlatform, String name ) {
        BaseGamePull baseGamePull   = gamePullDockFactoryUtil.createGamePullProcessor( gamePlatform.getGameCategory() );
        List<Object> remoteGameData = baseGamePull.requestRemoteGameData( gamePlatform );
        if ( !CollectionUtils.isEmpty( remoteGameData ) ) {
            log.info( "{}数据成功, 条数:{}", name, remoteGameData.size() );
            List<GameDataRecord> gameDataRecords = new ArrayList<>();
            for ( Object remoteGameDatum : remoteGameData ) {
                GameDataRecord gameDataRecord = baseGamePull.handleResult( remoteGameDatum, gamePlatform );
                if ( gameDataRecord != null ) {
                    gameDataRecords.add( gameDataRecord );
                }
            }
            gameDataRecordService.batchInsert( gameDataRecords, gamePlatform, name );
        }
    }
}
