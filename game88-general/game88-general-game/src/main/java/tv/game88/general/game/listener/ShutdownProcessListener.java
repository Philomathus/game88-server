package tv.game88.general.game.listener;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.api.mapper.GamePlatformMapper;

import java.util.List;

/**
 * @author meng.jun
 */
@Component
@Log4j2
public class ShutdownProcessListener implements ApplicationListener<ContextClosedEvent> {
    @Resource
    private RedisUtils         redisUtils;
    @Resource
    private GamePlatformMapper gamePlatformMapper;

    @Override
    public void onApplicationEvent( ContextClosedEvent event ) {
        log.warn( "服务关闭或重启, 准备注销所有Fix锁" );
        List<GamePlatform> gamePlatformList = gamePlatformMapper.selectGamePlatformAndVersionList();
        for ( GamePlatform gamePlatform : gamePlatformList ) {
            redisUtils.unLock( "remoteGameDataRecordFix:" + gamePlatform.getId() );
            redisUtils.unLock( "remoteGameDataRecord:" + gamePlatform.getId() );
        }
        log.warn( "服务关闭或重启, 已注销所有锁Fix" );
    }
}
