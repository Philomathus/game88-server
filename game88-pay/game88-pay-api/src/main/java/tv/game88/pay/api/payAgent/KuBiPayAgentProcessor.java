package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;
import tv.game88.pay.api.type.BankCodeKuBiType;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository( value = ConstantsPayAgent.KUBI + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class KuBiPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "酷币代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        BankCodeKuBiType bankCodeType = BankCodeKuBiType.getCodeByBankId( withdrawDetail.getBankId() );
        if ( bankCodeType == null ) {
            payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            log.warn( "此代付无法支持的银行类型 - 银行类型:{}", withdrawDetail.getBankId() );
            throw new BusinessException( "此代付无法支持的银行类型：" + withdrawDetail.getBankId() );
        }
        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put( "cmd", "transfer" );
        dataMap.put( "ver", "1.1" );
        dataMap.put( "payType", "0" );
        dataMap.put( "mchId", payAgentChannel.getMerId() );
        dataMap.put( "mchOrderId", withdrawDetail.getWithdrawOrderNo() );
        dataMap.put( "payAmt", withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ).toString() );
        dataMap.put( "accNo", withdrawDetail.getBankAccount().trim() );
        dataMap.put( "accName", withdrawDetail.getBankUserName().trim() );
        dataMap.put( "accType", "1" );
        dataMap.put( "fee_Type", "0" );
        dataMap.put( "urgency", "0" );
        dataMap.put( "bankCode", bankCodeType.name() );
        dataMap.put( "province", "广东省" );
        dataMap.put( "city", "深圳市" );
        dataMap.put( "openBank", "南油支行" );
        dataMap.put( "clientTime", LocalDateTimeUtils.format( reqPayAgent.getCurrentTime(),
                LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        dataMap.put( "schTime", "" );
        dataMap.put( "rmk", "" );
        dataMap.put( "tel", "" );
        dataMap.put( "Email", "" );
        dataMap.put( "smsFlag", "0" );
        dataMap.put( "bankPayPurpose", "" );
        dataMap.put( "Leave_word", "" );
        dataMap.put( "Ext", "" );
        dataMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        dataMap.put( "userId", AESCoder.decrypt( payAgentChannel.getHeaderValue() ) );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String sign    = DigestUtils.md5Hex( this.assemblyUrl( dataMap ) + "&key=" + signMd5 );
        dataMap.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( dataMap ), reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "0000".equals( resultMap.getOrDefault( "status", "" ).toString() ) ) {
                log.info( payAgentPlatform.getName() + "订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );

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
        String sign      = requestMap.remove( "sign" ).toString();
        String bankstate = requestMap.getOrDefault( "bankstate", "" ).toString();
        String orderid   = requestMap.getOrDefault( "orderid", "" ).toString();

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( orderid );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", orderid );
            return "fail";
        }
        if ( withdrawDetail.getStatus() == 6 ) {
            log.error( "已有代付记录 - merOrderNo:{}", orderid );
            return "success";
        }
        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( orderid );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        Map<String, String> dataMap = new LinkedHashMap<>();
        dataMap.put( "cmd", requestMap.getOrDefault( "cmd", "" ).toString() );
        dataMap.put( "ver", requestMap.getOrDefault( "ver", "" ).toString() );
        dataMap.put( "apiid", requestMap.getOrDefault( "apiid", "" ).toString() );
        dataMap.put( "orderid", orderid );
        dataMap.put( "accno", requestMap.getOrDefault( "accno", "" ).toString() );
        dataMap.put( "payamt", requestMap.getOrDefault( "payamt", "" ).toString() );
        dataMap.put( "bankstate", bankstate );
        dataMap.put( "orderstate", requestMap.getOrDefault( "orderstate", "" ).toString() );
        dataMap.put( "servertime", requestMap.getOrDefault( "servertime", "" ).toString() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        dataMap.put( "key", signMd5 );

        String mySign = DigestUtils.md5Hex( this.assemblyUrl( dataMap ) );

        if ( mySign.equalsIgnoreCase( sign ) ) {

            payAgentService.processOrderPay( withdrawDetail, payAgentLog, orderid, payAgentChannel, "1".equals( bankstate ) );
            return "SUCCESS";
        }
        return "fail";
    }

    @Override
    public Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap,
                                                     String realIp ) throws Exception {
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            return null;
        }
        Map<String, Object>  resultMap            = new HashMap<>();
        String               merchantNo           = requestMap.getOrDefault( "merchantNo", "" ).toString();
        String               orderNo              = requestMap.getOrDefault( "orderNo", "" ).toString();
        MemberWithdrawDetail memberWithdrawDetail = withdrawDetailMapper.selectById( orderNo );
        if ( memberWithdrawDetail == null ) {
            resultMap.put( "code", "ERROR" );
            resultMap.put( "message", "订单不存在" );
            resultMap.put( "orderNo", orderNo );
            return resultMap;
        }
        PayAgentLog payAgentLog = payAgentLogMapper.selectById( orderNo );
        if ( payAgentLog == null ) {
            resultMap.put( "code", "ERROR" );
            resultMap.put( "message", "代付记录不存在" );
            resultMap.put( "orderNo", orderNo );
            return resultMap;
        }
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );
        if ( !merchantNo.equals( payAgentChannel.getMerId() ) ) {
            resultMap.put( "code", "ERROR" );
            resultMap.put( "message", "商户号错误" );
            resultMap.put( "orderNo", orderNo );
        } else {
            resultMap.put( "code", "SUCCESS" );
            resultMap.put( "message", "信息正确" );
            resultMap.put( "orderNo", orderNo );
        }
        return resultMap;
    }

    @Override
    public String queryOrderPay( PayAgentLog payAgentLog ) throws Exception {
        MemberWithdrawDetail withdrawDetail   = withdrawDetailMapper.selectById( payAgentLog.getWithdrawOrderNo() );
        PayAgentChannel      payAgentChannel  = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );
        PayAgentPlatform     payAgentPlatform = payAgentPlatformMapper.selectById( payAgentChannel.getPlatformId() );
        Map<String, Object>  dataMap          = new LinkedHashMap<>();
        dataMap.put( "cmd", "transferquery" );
        dataMap.put( "ver", "1.2" );
        dataMap.put( "mchId", payAgentChannel.getMerId() );
        dataMap.put( "mchOrderId", withdrawDetail.getWithdrawOrderNo() );
        dataMap.put( "clientTime", LocalDateTimeUtils.format( LocalDateTime.now(),
                LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String tempStr = this.assemblyUrl( dataMap ) + "&key=" + signMd5;
        String sign    = DigestUtils.md5Hex( tempStr );
        dataMap.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( dataMap ), null );

        log.info( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "1".equals( resultMap.getOrDefault( "queryStatus", "" ).toString() ) ) {
                int orderState = Integer.parseInt( resultMap.getOrDefault( "orderStatus", 0 ).toString() );
                // status 4代付中5代付失败6代付成功
                // orderState (0待处理，1处理中，2处理成功，3处理失败,4未知)
                int status = 4;
                switch ( orderState ) {
                case 2 -> status = 6;
                case 3 -> status = 5;
                default -> {
                }
                }
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status,
                        orderState );
            }
            return resultMap.getOrDefault( "msg", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
