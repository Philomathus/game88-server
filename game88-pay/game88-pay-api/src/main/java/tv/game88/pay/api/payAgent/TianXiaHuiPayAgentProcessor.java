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
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;
import tv.game88.pay.api.type.BankCodeTianXiaHuiType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPayAgent.TIANXIAHUI_PAY + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class TianXiaHuiPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "天下汇代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        BankCodeTianXiaHuiType    bankCodeType = BankCodeTianXiaHuiType.getCodeByBankId( withdrawDetail.getBankId() );
        SortedMap<String, Object> bodyMap      = new TreeMap<>();
        if ( bankCodeType == null ) {
            payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            log.warn( "此代付无法支持的银行类型 - 银行类型:{}", withdrawDetail.getBankId() );
            throw new BusinessException( "此代付无法支持的银行类型：" + withdrawDetail.getBankId() );
        } else {
            bodyMap.put( "bankNumber", bankCodeType.name() );
            bodyMap.put( "bankName", bankCodeType.getName() );
        }
        bodyMap.put( "passageId", AESCoder.decrypt( payAgentChannel.getHeaderValue() ) );
        bodyMap.put( "mchId", payAgentChannel.getMerId() );
        bodyMap.put( "mchOrderNo", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "amount", withdrawDetail
                .getWithdrawMoney()
                .multiply( BigDecimal.valueOf( 100 ) )
                .setScale( 0, RoundingMode.HALF_UP )
                .intValue() );
        bodyMap.put( "accountName", withdrawDetail.getBankUserName() );
        bodyMap.put( "accountNo", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "remark", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "reqTime", LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
        bodyMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );

        String tempStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        bodyMap.put( "sign", DigestUtils.md5Hex( tempStr ) );

        log.warn( tempStr );
        log.warn( JsonUtil.object2Json( bodyMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String return_code = resultMap.getOrDefault( "retCode", "" ).toString();
            if ( "SUCCESS".equals( return_code ) ) {
                log.info( payAgentPlatform.getName() + "代付订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "errDes", resultMap.get( "retMsg" ) ).toString() );
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            }
        }
        log.warn( payAgentPlatform.getName() + "代付订单提交失败 - orderNo:{}", withdrawDetail.getWithdrawOrderNo() );
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        if ( this.checkWhiteIp( payAgentPlatform.getWhiteIp(), realIp ) ) {
            log.warn( "请求ip非白名单:{},request:{}", realIp, JsonUtil.object2Json( requestMap ) );
            return "fail";
        }
        String orderNo = requestMap.getOrDefault( "mchOrderNo", "" ).toString();

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( orderNo );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", orderNo );
            return "fail";
        }
        if ( withdrawDetail.getStatus() == 2 ) {
            log.error( "已有代付记录 - merOrderNo:{}", orderNo );
            return "SUCCESS";
        }
        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( orderNo );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        String status = requestMap.getOrDefault( "status", "" ).toString();
        String sign   = requestMap.remove( "sign" ).toString();

        // 去除空值
        requestMap.entrySet().removeIf( me -> me.getValue() == null || StringUtils.isBlank( me.getValue().toString() ) );
        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        bodyMap.remove( "remark" );

        String tempStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = DigestUtils.md5Hex( tempStr );
        bodyMap.put( "sign", signStr );

        log.info( payAgentPlatform.getName() + "代付回调签名:" + sign + "_" + signStr );
        if ( sign.equalsIgnoreCase( signStr ) ) {
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, orderNo, payAgentChannel, "2".equals( status ) );
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
        dataMap.put( "mchId", payAgentChannel.getMerId() );
        dataMap.put( "mchOrderNo", withdrawDetail.getWithdrawOrderNo() );
        dataMap.put( "reqTime", LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String tempStr = this.assemblyUrl( dataMap ) + "&key=" + signMd5;
        String sign    = DigestUtils.md5Hex( tempStr );
        dataMap.put( "sign", sign );
        log.warn( tempStr );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageForm( dataMap ), null );

        log.warn( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String return_code = resultMap.getOrDefault( "retCode", "" ).toString();
            if ( "SUCCESS".equals( return_code ) ) {
                String statusCode = resultMap.getOrDefault( "status", "" ).toString();

                // status 4代付中5代付失败6代付成功
                // orderState 0 回调中 1 成功 2失败
                int status     = 4;
                int orderState = 0;
                if ( "2".equals( statusCode ) ) {
                    status     = 6;
                    orderState = 1;
                } else if ( "3".equals( statusCode ) ) {
                    status     = 5;
                    orderState = 2;
                }
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            }
            return resultMap.getOrDefault( "transMsg", "" ).toString();
        }
        return payAgentPlatform.getName() + "代付查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}

