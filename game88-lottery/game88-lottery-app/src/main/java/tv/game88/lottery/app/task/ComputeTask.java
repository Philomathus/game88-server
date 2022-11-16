package tv.game88.lottery.app.task;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.lottery.api.cache.LotteryCacheUtils;
import tv.game88.lottery.api.dto.RspLotteryInfo;
import tv.game88.lottery.api.service.LotteryService;

import javax.annotation.Resource;

/**
 * 开奖逻辑
 */
@Log4j2
@Component
public class ComputeTask {
    @Resource
    private RedisUtils     redisUtils;
    @Resource
    private LotteryService lotteryService;

    @Scheduled( cron = "0 * * * * *" )
    public void runTask() {
        if ( !redisUtils.lock( Constants.LOTTERY_PREX + "Compute", 20 ) ) {
            return;
        }
        for ( RspLotteryInfo lotteryInfo : LotteryCacheUtils.me.getRspLotteryInfo() ) {
            if ( lotteryInfo.getId() == 2001 ) {
                continue;
            }
            try {
                lotteryService.computeResult( lotteryInfo.getId() );
            } catch ( Exception e ) {
                log.error( e.getMessage(), e );
            }
        }
    }

    @Scheduled( cron = "51 * * * * *" )
    public void runTask2001() {
        if ( !redisUtils.lock( Constants.LOTTERY_PREX + "Compute2001", 20 ) ) {
            return;
        }
        try {
            lotteryService.computeResult( 2001 );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
    }
}
