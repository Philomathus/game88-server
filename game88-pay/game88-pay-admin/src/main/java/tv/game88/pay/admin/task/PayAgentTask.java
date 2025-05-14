package tv.game88.pay.admin.task;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.RedisUtils;
import tv.game88.pay.api.service.PayAgentService;

import jakarta.annotation.Resource;

@Log4j2
@Component
public class PayAgentTask {
    @Resource
    private RedisUtils      redisUtil;
    @Resource
    private PayAgentService payAgentService;

    // @Scheduled( cron = "0 * * * * ?" ) // 每分钟执行一次
    public void confirmPayAgentOrder() {
        try {
            if ( redisUtil.lock( "confirmPayAgentOrder", 30 ) ) {
                log.warn( "开始执行代付订单的超时查询" );
                payAgentService.queryAgent4Status5Min();
            }
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
    }
}
