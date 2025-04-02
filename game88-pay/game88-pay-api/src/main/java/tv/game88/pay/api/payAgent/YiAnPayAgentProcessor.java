package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
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
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Repository( value = ConstantsPayAgent.YI_AN_PAY + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class YiAnPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "易安代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        SortedMap<String, Object> bodyMap = new TreeMap<>();

        bodyMap.put( "MerchantId", payAgentChannel.getMerId() );
        bodyMap.put( "Amount", withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ) );
        ConfigBankList configBank = configBankListCache.getConfigBank( withdrawDetail.getBankId() );
        if ( configBank == null ) {
            payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            log.warn( "未知银行类型 - 银行类型:{}", withdrawDetail.getBankId() );
            throw new BusinessException( "未知银行类型：" + withdrawDetail.getBankId() );
        }
        bodyMap.put( "BankCardBankName", configBank.getBankName() );
        bodyMap.put( "BankCardNumber", withdrawDetail.getBankAccount() );
        bodyMap.put( "BankCardRealName", withdrawDetail.getBankUserName() );
        bodyMap.put( "MerchantUniqueOrderId", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "NotifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "Remark", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "WithdrawTypeId", 0 );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String tempStr = this.assemblyUrl( bodyMap ) + signMd5;
        String sign    = DigestUtils.md5Hex( tempStr ).toLowerCase();

        bodyMap.put( "Sign", sign );
        bodyMap.put( "Timestamp", LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.LOCALTIME_SP_NOM_FORMATTER ) );

        log.warn( tempStr );
        log.warn( JsonUtil.object2Json( bodyMap ) );
        log.warn( "sign: {}", sign );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( bodyMap ), reqPayAgent );
        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "0".equals( resultMap.getOrDefault( "Code", "" ).toString() ) ) {
                log.info( payAgentPlatform.getName() + "代付订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "Message", "" ).toString() );
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            }
        }
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            return "fail";
        }

        String sign      = requestMap.remove( "Sign" ).toString();
        String order_num = requestMap.getOrDefault( "MerchantUniqueOrderId", "" ).toString();
        String status    = requestMap.getOrDefault( "WithdrawOrderStatus", "" ).toString();

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( order_num );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        requestMap.values().removeIf( value -> value == null || StringUtils.isBlank( value.toString() ) );
        requestMap.remove( "NO_SIGN_FailReason" );

        SortedMap<String, Object> dataMap = new TreeMap<>( requestMap );
        String                    tempStr = assemblyUrl( dataMap ) + signMd5;
        log.warn( tempStr );
        String signStr = DigestUtils.md5Hex( tempStr ).toLowerCase();
        log.info( payAgentPlatform.getName() + "代付回调签名:" + sign + "_" + signStr );

        if ( sign.equalsIgnoreCase( signStr ) ) {
            MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( order_num );
            if ( withdrawDetail == null ) {
                log.error( "提现相关记录丢失 - merOrderNo:{}", order_num );
                return "fail";
            }
            if ( withdrawDetail.getStatus() == 3 ) {
                log.error( "订单已拒绝，无需回调 - merOrderNo:{}", order_num );
                return "SUCCESS";
            }
            if ( withdrawDetail.getStatus() == 6 ) {
                log.error( "已有代付记录 - merOrderNo:{}", order_num );
                return "SUCCESS";
            }

            payAgentService.processOrderPay( withdrawDetail, payAgentLog, requestMap.getOrDefault( "WithdrawOrderId", "" )
                    .toString(), payAgentChannel, "100".equals( status ) );
            log.info(
                    payAgentPlatform.getName() + "订单号:{},回调状态:{},", order_num, "100".equals( status ) ? "成功" : "失败" );
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
        Map<String, Object> paramsMap = new TreeMap<>();
        paramsMap.put( "MerchantId", payAgentChannel.getMerId() );
        paramsMap.put( "MerchantUniqueOrderId", withdrawDetail.getWithdrawOrderNo() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String tempStr = this.assemblyUrl( paramsMap ) + signMd5;
        String sign    = DigestUtils.md5Hex( tempStr ).toLowerCase();
        paramsMap.put( "Sign", sign );

        log.warn( JsonUtil.object2Json( paramsMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageForm( paramsMap ), null );
        log.info( payAgentPlatform.getName()
                + "查询结果{}，订单号：{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String trade_state = resultMap.getOrDefault( "WithdrawOrderStatus", "" ).toString();
            if ( "100".equals( trade_state ) || "0".equals( trade_state ) || "-90".equals( trade_state ) ) {
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
