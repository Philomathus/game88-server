package tv.game88.lottery.app.task;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.lottery.api.service.LotteryService;

import javax.annotation.Resource;

/**
 * 派奖逻辑
 */
@Log4j2
@Component
public class AwardTask {
    @Resource
    private RedisUtils     redisUtils;
    @Resource
    private LotteryService lotteryService;

    @Scheduled( cron = "5 * * * * *" )
    public void runTask1001() {
        if ( !redisUtils.lock( Constants.LOTTERY_PREX + "Award1001", 20 ) ) {
            return;
        }
        try {
            lotteryService.awardLottery( 1001 );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
    }

    @Scheduled( cron = "5 * * * * *" )
    public void runTask1002() {
        if ( !redisUtils.lock( Constants.LOTTERY_PREX + "Award1002", 20 ) ) {
            return;
        }
        try {
            lotteryService.awardLottery( 1002 );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
    }

    @Scheduled( cron = "5 * * * * *" )
    public void runTask1003() {
        if ( !redisUtils.lock( Constants.LOTTERY_PREX + "Award1003", 20 ) ) {
            return;
        }
        try {
            lotteryService.awardLottery( 1003 );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
    }

    @Scheduled( cron = "5 * * * * *" )
    public void runTask1004() {
        if ( !redisUtils.lock( Constants.LOTTERY_PREX + "Award1004", 20 ) ) {
            return;
        }
        try {
            lotteryService.awardLottery( 1004 );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
    }

    @Scheduled( cron = "5 * * * * *" )
    public void runTask1005() {
        if ( !redisUtils.lock( Constants.LOTTERY_PREX + "Award1005", 20 ) ) {
            return;
        }
        try {
            lotteryService.awardLottery( 1005 );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
    }

    @Scheduled( cron = "0 * * * * *" )
    public void runTask2001() {
        if ( !redisUtils.lock( Constants.LOTTERY_PREX + "Award2001", 20 ) ) {
            return;
        }
        try {
            lotteryService.awardLottery( 2001 );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
    }
}
