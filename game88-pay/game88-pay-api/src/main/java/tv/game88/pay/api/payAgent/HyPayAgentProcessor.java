package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;
import tv.game88.pay.api.type.BankCodeHyType;

import java.math.RoundingMode;
import java.util.*;

@Repository( value = ConstantsPayAgent.HY + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class HyPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "HY代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        BankCodeHyType bankType = BankCodeHyType.getCodeByBankId( withdrawDetail.getBankId() );
        if ( bankType == null ) {
            payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            log.warn( payAgentPlatform.getName() + "代付无法支持的银行类型 - 银行类型:{}", withdrawDetail.getBankId() );
            throw new BusinessException( payAgentPlatform.getName() + "代付无法支持的银行类型：" + withdrawDetail.getBankId() );
        }
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "merchantId", payAgentChannel.getMerId() );
        bodyMap.put( "merchantOrderId", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "orderAmount", withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ) );
        bodyMap.put( "payType", "1" );
        bodyMap.put( "accountHolderName", withdrawDetail.getBankUserName().trim() );
        bodyMap.put( "accountNumber", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "bankType", bankType.name().substring( 1 ) );
        bodyMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "reverseUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );
        bodyMap.put( "submitIp", "192.168.0.1" );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + signMd5;

        String sign = DigestUtils.md5Hex( signStr ).toLowerCase();
        bodyMap.put( "sign", sign );

        bodyMap.put( "subBranch", withdrawDetail.getBankUserName() );
        log.warn( JsonUtil.object2Json( bodyMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Object error_code    = resultMap.get( "ErrorCode" );
            Object error_message = resultMap.get( "ErrorMessage" );

            if ( Objects.isNull( error_message ) && Objects.isNull( error_code ) ) {
                log.info( payAgentPlatform.getName() + "代付订单提交成功 - result:{}", JsonUtil.object2Json( resultMap ) );
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "ErrorMessage", "" ).toString() );
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            }
        }
        log.warn( payAgentPlatform.getName() + "代付订单提交失败 - orderNo:{}", withdrawDetail.getWithdrawOrderNo() );
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {


        String sign            = requestMap.remove( "sign" ).toString();
        String merchantOrderId = requestMap.getOrDefault( "merchantOrderId", "" ).toString();

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( merchantOrderId );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        String status  = requestMap.getOrDefault( "status", "" ).toString();
        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put( "merchantId", requestMap.get( "merchantId" ) );
        bodyMap.put( "merchantOrderId", merchantOrderId );
        bodyMap.put( "status", status );
        bodyMap.put( "orderType", requestMap.get( "orderType" ) );
        bodyMap.put( "orderAmount", requestMap.get( "orderAmount" ) );
        bodyMap.put( "systemOrderId", requestMap.get( "systemOrderId" ) );
        bodyMap.put( "remark", requestMap.get( "remark" ) );
        bodyMap.put( "submitIp", requestMap.get( "submitIp" ) );

        String tempStr = this.assemblyUrl( bodyMap ) + signMd5;
        String signStr = DigestUtils.md5Hex( tempStr );
        log.warn( sign + " : " + signStr );
        if ( signStr.equalsIgnoreCase( sign ) ) {
            MemberWithdrawDetail withdrawLog = withdrawDetailMapper.selectById( merchantOrderId );
            if ( withdrawLog == null ) {
                log.error( "提现相关记录丢失 - merOrderNo:{}", merchantOrderId );
                return "fail";
            }
            if ( withdrawLog.getStatus() == 6 ) {
                log.error( "已有代付记录 - merOrderNo:{}", merchantOrderId );
                return "OK";
            }
            payAgentService.processOrderPay( withdrawLog, payAgentLog, requestMap.getOrDefault( "systemOrderId", "" )
                                                                                 .toString(), payAgentChannel,
                    "3".equals( status ) );
            return "OK";
        }

        return "fail";
    }

    @Override
    public Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap,
                                                     String realIp ) {
        return null;
    }

    @Override
    public String queryOrderPay( PayAgentLog payAgentLog ) {

        MemberWithdrawDetail withdrawLog      = withdrawDetailMapper.selectById( payAgentLog.getWithdrawOrderNo() );
        PayAgentPlatform     payAgentPlatform = payAgentPlatformMapper.selectById( payAgentLog.getChannelId() );
        PayAgentChannel      payAgentChannel  = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        Map<String, Object> dataMap = new LinkedHashMap<>();
        dataMap.put( "merchantId", payAgentPlatform.getId() );
        dataMap.put( "merchantOrderId", withdrawLog.getWithdrawOrderNo() );
        dataMap.put( "orderAmount", withdrawLog.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ) );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        String tempStr = this.assemblyUrl( dataMap ) + signMd5;

        log.warn( tempStr );

        String sign = DigestUtils.md5Hex( tempStr ).toLowerCase();
        dataMap.put( "sign", sign );
        log.warn( payAgentPlatform.getName() + "查询代付状态接口请求参数{}", JsonUtil.object2Json( dataMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageForm( dataMap ), null );

        log.warn( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String errorCode    = resultMap.getOrDefault( "ErrorCode", "" ).toString();
            String errorMessage = resultMap.getOrDefault( "ErrorMessage", "" ).toString();
            if ( StringUtils.isEmpty( errorCode ) && StringUtils.isEmpty( errorMessage ) ) {
                String trade_state = resultMap.getOrDefault( "Status", "" ).toString();
                if ( "1".equals( trade_state ) || "2".equals( trade_state ) || "3".equals( trade_state )
                        || "4".equals( trade_state ) ) {
                    // status 4代付中 5代付失败 6代付成功
                    // trade_state  1等待处理 2准备打款,3已打款,4已拒绝 處理中,需繼續查詢
                    int status      = 4;
                    int orderStatus = 0;
                    if ( "3".equals( trade_state ) ) {
                        status      = 6;
                        orderStatus = 1;
                    } else if ( "4".equals( trade_state ) ) {
                        status      = 5;
                        orderStatus = 2;
                    }
                    payAgentService.processOrder( payAgentChannel, withdrawLog, withdrawLog.getUpdateTime(), status,
                            orderStatus );
                }
            }
            return errorMessage;
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawLog.getWithdrawOrderNo();
    }
}
