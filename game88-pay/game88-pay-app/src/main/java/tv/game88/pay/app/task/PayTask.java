package tv.game88.pay.app.task;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.pay.api.service.PayService;

import javax.annotation.Resource;

@Log4j2
@Component
public class PayTask {
    @Resource
    private RedisUtils redisUtil;
    @Resource
    private PayService payService;

    @Scheduled( cron = "0 * * * * ?" ) // 每分钟执行一次
    public void confirmPayOrder() {
        try {
            if ( redisUtil.lock( "confirmPayOrder", 30 ) ) {
                payService.payQuery10Min();
            }
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
    }
}
