package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.config.entity.ConfigBankList;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.core.config.dto.RspConfigBankList;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Repository( value = ConstantsPayAgent.CHONG_U_PAY + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class ChongUPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "充u代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {

        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "MerchantId", payAgentChannel.getMerId() );
        bodyMap.put( "Amount", withdrawDetail.getWithdrawMoney().setScale( 0, RoundingMode.HALF_UP ) );
        ConfigBankList configBank = configBankListCache.getConfigBank( withdrawDetail.getBankId() );
        if ( configBank == null ) {
            payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            log.warn( "未知银行类型 - 银行类型:{}", withdrawDetail.getBankId() );
            throw new BusinessException( "未知银行类型：" + withdrawDetail.getBankId() );
        }
        bodyMap.put( "BankCardBankName", configBank.getBankName() );
        bodyMap.put( "BankCardNumber", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "BankCardRealName", withdrawDetail.getBankUserName().trim() );
        bodyMap.put( "MerchantUniqueOrderId", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "NotifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "Timestamp", LocalDateTimeUtils.format( reqPayAgent.getCurrentTime(),
                LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        bodyMap.put( "WithdrawTypeId", 0 );

        String signStr = this.assemblyUrl( bodyMap ) + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        log.warn( signStr );

        bodyMap.put( "Sign", DigestUtils.md5Hex( signStr ) );

        log.warn( payAgentPlatform.getName() + "下单请求参数 - {}", JsonUtil.object2Json( bodyMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String return_code = resultMap.getOrDefault( "Code", "" ).toString();
            if ( "0".equals( return_code ) ) {
                log.info( payAgentPlatform.getName() + "代付订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "Message", "" ).toString() );
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            }
        }
        log.warn( payAgentPlatform.getName() + "订单提交失败 - orderNo:{}", withdrawDetail.getWithdrawOrderNo() );
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            return "fail";
        }
        String sign            = requestMap.remove( "Sign" ).toString();
        String withdrawOrderId = requestMap.getOrDefault( "MerchantUniqueOrderId", "" ).toString();
        String status          = requestMap.getOrDefault( "Status", "" ).toString();

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( withdrawOrderId );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signStr = this.assemblyUrl( bodyMap ) + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        log.warn( signStr );
        String mySign = DigestUtils.md5Hex( signStr ).toLowerCase();

        log.warn( sign + " : " + mySign );
        if ( mySign.equalsIgnoreCase( sign ) ) {
            MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( withdrawOrderId );
            if ( withdrawDetail == null ) {
                log.error( "提现相关记录丢失 - withdrawOrderId:{}", withdrawOrderId );
                return "fail";
            }
            if ( withdrawDetail.getStatus() == 6 ) {
                log.error( "已有代付记录 - withdrawOrderId:{}", withdrawOrderId );
                return "SUCCESS";
            }

            String orderId = requestMap.getOrDefault( "WithdrawOrderId", "" ).toString();
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, orderId, payAgentChannel, "100".equals( status ) );
            log.info( payAgentPlatform.getName()
                    + "订单号:{},回调状态:{},", withdrawOrderId, "100".equals( status ) ? "成功" : "失败" );
            return "SUCCESS";
        }

        return "fail";
    }

    @Override
    public Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap,
                                                     String realIp ) throws Exception {
        return null;
    }

    @Override
    public String queryOrderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                                 PayAgentPlatform payAgentPlatform ) throws Exception {
        Map<String, Object> dataMap = new TreeMap<>();
        dataMap.put( "MerchantId", payAgentPlatform.getId() );
        dataMap.put( "Timestamp", LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        dataMap.put( "MerchantUniqueOrderId", withdrawDetail.getWithdrawOrderNo() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        // 生成签名信息
        String signStr = this.assemblyUrl( dataMap ) + signMd5;
        String sign    = DigestUtils.md5Hex( signStr );
        dataMap.put( "Sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageForm( dataMap ), null );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String return_code = resultMap.getOrDefault( "Code", "" ).toString();
            if ( "0".equals( return_code ) ) {
                String trade_state = resultMap.getOrDefault( "WithdrawOrderStatus", "" ).toString();
                // status 4代付中 5代付失败 6代付成功
                // trade_state  100成功 -90失败 0 處理中,需繼續查詢
                int status = switch ( trade_state ) {
                    case "100" -> 6;
                    case "-90" -> 5;
                    default -> 4;
                };
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            }
            return resultMap.getOrDefault( "Message", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
