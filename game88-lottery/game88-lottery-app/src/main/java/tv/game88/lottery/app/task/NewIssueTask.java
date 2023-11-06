package tv.game88.lottery.app.task;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.lottery.api.cache.LotteryCacheUtils;
import tv.game88.lottery.api.dto.RspLotteryInfo;
import tv.game88.lottery.api.service.LotteryHistoryService;
import tv.game88.lottery.api.utils.LotteryUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 开新期
 */
@Log4j2
@Component
public class NewIssueTask {
    @Resource
    private RedisUtils            redisUtils;
    @Resource
    private LotteryHistoryService lotteryHistoryService;

    @Scheduled( cron = "0 * * * * *" )
    public void runTask() {
        if ( !redisUtils.lock( Constants.LOTTERY_PREX + "issueNew", 20 ) ) {
            return;
        }
        log.warn( "开始开新期" );
        LocalDateTime now = LocalDateTime.now();
        for ( RspLotteryInfo lotteryInfo : LotteryCacheUtils.me.getRspLotteryInfo() ) {
            // 默认新开10期
            for ( int i = 0; i <= 9; i++ ) {
                LocalDateTime time  = now.plusMinutes( i );
                String        issue = LotteryUtils.getLotteryIssue( lotteryInfo.getCycle(), time );
                try {
                    lotteryHistoryService.newIssue( lotteryInfo, issue, time, i );
                } catch ( Exception e ) {
                    log.error( "开新期发生错误" + e.getMessage(), e );
                }
            }
        }
    }
}
