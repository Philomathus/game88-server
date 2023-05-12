package tv.game88.game.admin.task;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.game.api.entity.GamePlatform;
import tv.game88.game.api.entity.MemberGameDataFix;
import tv.game88.game.api.mapper.GamePlatformMapper;
import tv.game88.game.api.mapper.MemberGameDataFixMapper;
import tv.game88.game.api.service.GameDataService;
import tv.game88.core.game.type.EnumGameCategory;

import javax.annotation.Resource;
import java.util.List;

/**
 * 数据打码补单
 */
@Log4j2
@Component
public class FixDataTask {
    @Resource
    private RedisUtils              redisUtils;
    @Resource
    private GamePlatformMapper      gamePlatformMapper;
    @Resource
    private GameDataService         gameDataService;
    @Resource
    private MemberGameDataFixMapper memberGameDataFixMapper;

    @Scheduled( fixedDelay = 180000, initialDelay = 1 )
    public void runTask() {
        if ( !redisUtils.lock( "FixDataTask", 150 ) ) {
            return;
        }
        List<GamePlatform> gamePlatforms = new QueryChainWrapper<>( gamePlatformMapper ).list();

        List<MemberGameDataFix> memberGameDataFixes = new QueryChainWrapper<>( memberGameDataFixMapper ).eq( "status", 0 )
                                                                                                        .isNotNull( "platform_id" )
                                                                                                        .list();
        for ( MemberGameDataFix memberGameDataFix : memberGameDataFixes ) {
            for ( GamePlatform gamePlatform : gamePlatforms ) {
                if ( memberGameDataFix.getPlatformId() == gamePlatform.getId().intValue() ) {
                    String begin = LocalDateTimeUtils.format( memberGameDataFix.getGameStartTime() );
                    String end   = LocalDateTimeUtils.format( memberGameDataFix.getGameEndTime() );
                    if ( gamePlatform.getGameCategory() == EnumGameCategory.LOTTERY ) {
                        try {
                            gameDataService.beatLotteryCode( begin, end );
                        } catch ( Exception e ) {
                            log.error( "补单彩票拉取注单异常{}", e.getMessage(), e );
                        }
                    } else {
                        try {
                            gameDataService.beatGameCodeAgent( begin, begin, end, memberGameDataFix.getMemberId(),
                                    gamePlatform.getGameCategory() );
                        } catch ( Exception e ) {
                            log.error( "补单游戏拉取注单异常{}", e.getMessage(), e );
                        }
                    }
                    MemberGameDataFix update = new MemberGameDataFix();
                    update.setId( memberGameDataFix.getId() );
                    update.setStatus( 1 );
                    memberGameDataFixMapper.updateById( update );
                }
            }
        }
    }
}
