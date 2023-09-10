package tv.game88.wallet.app.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.*;
import tv.game88.core.config.constants.Constants;
import tv.game88.wallet.api.dto.RspWalletRecord;
import tv.game88.wallet.api.entity.WalletRecord;
import tv.game88.wallet.api.service.WalletRecordService;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
@Component
public class OrderCallbackProcessTask {
    @Resource
    private RedisUtils               redisUtils;
    @Resource
    private ScheduledExecutorService scheduledExecutorService;
    @Resource
    private RestTemplate             restTemplate;
    @Resource
    private WalletRecordService      walletRecordService;

    @Scheduled( cron = "0/15 * * * * ?" ) // 每15秒执行一次
    public void orderCallbackProcess() {
        String              key = Constants.WALLET_PREX + "callback:orderNo";
        Map<Object, Object> map = redisUtils.hGetAll( key );
        long                now = LocalDateTimeUtils.localDateToTimestamp( LocalDateTime.now() );
        for ( Map.Entry<Object, Object> entry : map.entrySet() ) {
            String              tradeNo = entry.getKey().toString();
            String              data    = entry.getValue().toString();
            Map<String, Object> dataMap = JsonUtil.json2Map( data );
            int                 num     = Integer.parseInt( dataMap.get( "num" ).toString() );
            if ( num == 5 ) {
                redisUtils.hRemove( key, tradeNo );
                log.warn( "通知交易订单:{} 5次不成功,忽略回调, 回调地址:{}", tradeNo, dataMap.get( "notifyUrl" ).toString() );
                continue;
            }
            long time = Long.parseLong( dataMap.get( "time" ).toString() );
            if ( time > now ) {
                continue;
            }
            if ( !redisUtils.lock( "OrderCallback:" + tradeNo, 30 ) ) {
                continue;
            }
            String notifyUrl = dataMap.get( "notifyUrl" ).toString();
            scheduledExecutorService.schedule( () -> {
                RspWalletRecord rspWalletRecord = null;
                String          resultStr       = null;
                WalletRecord    update          = new WalletRecord();
                update.setTradeNo( tradeNo );
                try {
                    rspWalletRecord = walletRecordService.getRspData( tradeNo );
                    resultStr       = restTemplate.postForObject( notifyUrl, rspWalletRecord, String.class );
                    if ( StringUtils.equalsIgnoreCase( resultStr, "success" ) ) {
                        log.info( "通知商户:{}回调订单:{}成功,回调地址:{},通知数据:{}", rspWalletRecord.getMerchantId(),
                                rspWalletRecord.getOrderNo(), notifyUrl, JsonUtil.object2Json( rspWalletRecord ) );
                        update.setNotifyStatus( 1 );
                        redisUtils.hRemove( key, tradeNo );
                    } else {
                        log.warn( "通知商户:{}回调订单:{}失败,回调地址:{},商户返回结果:{},通知数据:{}", rspWalletRecord.getMerchantId(),
                                rspWalletRecord.getOrderNo(), notifyUrl, resultStr, JsonUtil.object2Json( rspWalletRecord ) );
                        update.setNotifyStatus( 2 );
                    }
                } catch ( Exception e ) {
                    log.error( "通知商户:{}回调订单:{}失败,回调地址:{},回调报错信息:{},通知数据:{}", rspWalletRecord.getMerchantId(),
                            rspWalletRecord.getOrderNo(), notifyUrl, e.getMessage(), JsonUtil.object2Json( rspWalletRecord ) );
                    resultStr = e.getMessage();
                    log.error( e.getMessage(), e );
                } finally {
                    redisUtils.unLock( "OrderCallback:" + tradeNo );
                }
                update.setNotifyResult( resultStr );
                walletRecordService.updateById( update );
            }, RandomUtils.randomIntWithMax( 0, 5 ), TimeUnit.SECONDS );
        }
    }

    @Scheduled( cron = "0 * * * * ?" ) // 每1分钟执行一次
    public void processTimeoutOrder() {
        if ( !redisUtils.lock( "OrderCallback:processTimeoutOrder", 30 ) ) {
            return;
        }
        // 将15分钟前未完成的订单设置为失败
        LocalDateTime dateTime = LocalDateTime.now().minusMinutes( 15 );
        walletRecordService.update( new UpdateWrapper<WalletRecord>().set( "status", 0 ).eq( "status", 2 )
                                                                     .ge( "update_time", dateTime ) );
    }
}
