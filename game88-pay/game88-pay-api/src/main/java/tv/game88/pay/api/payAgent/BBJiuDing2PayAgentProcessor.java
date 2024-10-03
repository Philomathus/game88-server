package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.core.config.dto.RspConfigBankList;
import tv.game88.core.config.entity.ConfigBankList;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.math.RoundingMode;
import java.util.*;

@Repository( value = ConstantsPayAgent.BB_JIUDING2_PAY + ConstantsPayAgent.PROCESSOR )
@Log4j2
public class BBJiuDing2PayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "BB玖鼎2代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "merchant_no", payAgentChannel.getMerId() );
        bodyMap.put( "out_trade_no", withdrawDetail.getWithdrawOrderNo() );
        bodyMap.put( "amount", withdrawDetail.getWithdrawMoney().setScale( 2, RoundingMode.HALF_UP ).toString() );
        ConfigBankList configBank = configBankListCache.getConfigBank( withdrawDetail.getBankId() );
        if ( configBank == null ) {
            payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            log.warn( "未知银行类型 - 银行类型:{}", withdrawDetail.getBankId() );
            throw new BusinessException( "未知银行类型：" + withdrawDetail.getBankId() );
        }
        bodyMap.put( "bank_name", configBank.getBankName() );
        bodyMap.put( "account_name", withdrawDetail.getBankUserName().trim() );
        bodyMap.put( "account", withdrawDetail.getBankAccount().trim() );
        bodyMap.put( "payment_type", "prepaid" );
        bodyMap.put( "notify_url", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String tempStr = this.assemblyUrl( bodyMap ) + "&key=" + signMd5;
        log.warn( tempStr );
        bodyMap.put( "sign", DigestUtils.md5Hex( tempStr ) );

        log.warn( payAgentPlatform.getName() + "下单请求参数 - {}", JsonUtil.object2Json( bodyMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageJson( bodyMap ), reqPayAgent );

        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "0".equals( resultMap.getOrDefault( "code", "" ).toString() ) && resultMap.get( "data" ) != null ) {
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

        String signRes         = requestMap.remove( "sign" ).toString();
        String withdrawOrderId = requestMap.getOrDefault( "out_trade_no", "" ).toString();

        MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( withdrawOrderId );
        if ( withdrawDetail == null ) {
            log.error( "提现相关记录丢失 - merOrderNo:{}", withdrawOrderId );
            return "fail";
        }
        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( withdrawOrderId );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        Map<String, Object> dataMap = new TreeMap<>( requestMap );
        String              signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String tempStr = this.assemblyUrl( dataMap ) + "&key=" + signMd5;
        String sign    = DigestUtils.md5Hex( tempStr );
        if ( signRes.equalsIgnoreCase( sign ) ) {
            if ( withdrawDetail.getStatus() == 0 ) {
                log.error( "已有代付记录 - merOrderNo:{}", withdrawOrderId );
                return "success";
            }

            String  status  = dataMap.getOrDefault( "status", "" ).toString();
            boolean success = "success".equals( status );
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, requestMap
                    .getOrDefault( "order_no", "" )
                    .toString(), payAgentChannel, success );

            log.info( payAgentPlatform.getName() + "订单号:{},回调状态:{},", withdrawOrderId, success ? "成功" : "失败" );
            return "success";
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
        dataMap.put( "out_trade_no", withdrawDetail.getWithdrawOrderNo() );
        dataMap.put( "merchant_no", payAgentChannel.getMerId() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        String tempStr = this.assemblyUrl( dataMap ) + "&key=" + signMd5;
        dataMap.put( "sign", DigestUtils.md5Hex( tempStr ) );

        log.warn( JsonUtil.object2Json( dataMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageJson( dataMap ), null );

        log.info( payAgentPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String              code       = resultMap.getOrDefault( "code", "" ).toString();
            Map<String, Object> dataResMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( "0".equals( code ) && !CollectionUtils.isEmpty( dataResMap ) ) {
                String statusRes = dataResMap.getOrDefault( "status", "" ).toString();
                // status 4代付中 5代付失败 6代付成功
                int status = switch ( statusRes ) {
                    case "SUCCESSFUL" -> 6;
                    case "FAILURE" -> 5;
                    default -> 4;
                };
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), status );
            }
            return resultMap.getOrDefault( "msg", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
