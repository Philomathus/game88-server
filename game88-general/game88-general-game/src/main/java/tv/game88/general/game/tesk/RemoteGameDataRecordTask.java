package tv.game88.general.game.tesk;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;

import javax.annotation.Resource;

@Log4j2
@Component
public class RemoteGameDataRecordTask {
    @Resource
    private RedisUtils redisUtils;

    @Scheduled( cron = "0/15 * * * * ?" ) // 每15秒执行一次
    public void remoteGameDataRecord() {
        if ( !redisUtils.lock( this.getClass().getSimpleName(), 14 ) ) {
            return;
        }
        log.info( "开始执行游戏注单数据拉取任务" );

    }
}
