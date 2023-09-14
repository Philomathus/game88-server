package tv.game88.pay.api.payAgent;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.pay.api.base.AbstractPayAgent;
import tv.game88.pay.api.constants.ConstantsPayAgent;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPayAgent.CBI_PAY + "PayAgentProcessor" )
@Log4j2
public class CBiPayAgentProcessor extends AbstractPayAgent {
    @Override
    public String getName() {
        return "C币代付";
    }

    @Override
    public boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                             PayAgentPlatform payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "userCode", payAgentChannel.getMerId() );
        params.put( "orderCode", withdrawDetail.getWithdrawOrderNo() );
        params.put( "amount", withdrawDetail.getWithdrawMoney().setScale( 0, RoundingMode.HALF_UP ).toString() );
        params.put( "address", withdrawDetail.getBankAccount() );
        params.put( "callbackUrl", configEnvCacheUtil.getConf( "payAgentNotifyUrl" ) + payAgentPlatform.getCode() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        // MD5(orderCode&amount&address&userCode&key)
        String tempStr = String.format( "%s&%s&%s&%s&%s", params.get( "orderCode" ), params.get( "amount" ), params.get(
                "address" ), params.get( "userCode" ), signMd5 );
        params.put( "sign", DigestUtils.md5Hex( tempStr ).toUpperCase() );

        log.warn( JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderUrl(), packageForm( params ), reqPayAgent );
        log.info( payAgentPlatform.getName()
                + "下单结果{},订单号:{}", JsonUtil.object2Json( resultMap ), withdrawDetail.getWithdrawOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                return true;
            } else {
                reqPayAgent.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
                payAgentService.callBackOrder( withdrawDetail, payAgentChannel.getName() );
            }
        }
        log.warn( payAgentPlatform.getName() + "订单提交失败 - orderNo:{}", withdrawDetail.getWithdrawOrderNo() );
        return false;
    }

    @Override
    public String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception {
        String rspSign = requestMap.remove( "sign" ).toString();

        String status            = requestMap.getOrDefault( "status", "1" ).toString();
        String orderCode         = requestMap.getOrDefault( "orderCode", "" ).toString();
        String customerOrderCode = requestMap.getOrDefault( "customerOrderCode", "" ).toString();
        String amount            = requestMap.getOrDefault( "amount", "-1" ).toString();
        String userCode          = requestMap.getOrDefault( "userCode", "" ).toString();

        PayAgentLog     payAgentLog     = payAgentLogMapper.selectById( customerOrderCode );
        PayAgentChannel payAgentChannel = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );

        // MD5(orderCode&customerOrderCode&amount&userCode&status&key)
        String signStr = orderCode + "&" + customerOrderCode + "&" + amount + "&" + userCode + "&" + status + "&" + signMd5;
        String mySign  = DigestUtils.md5Hex( signStr ).toUpperCase();

        log.info( payAgentPlatform.getName() + "回调签名:" + rspSign + "_" + mySign );
        if ( rspSign.equalsIgnoreCase( mySign ) ) {

            MemberWithdrawDetail withdrawDetail = withdrawDetailMapper.selectById( customerOrderCode );
            if ( withdrawDetail == null ) {
                log.error( "提现相关记录丢失 - merOrderNo:{}", customerOrderCode );
                return "fail";
            }
            if ( withdrawDetail.getStatus() == 3 ) {
                log.error( "订单已拒绝，无需回调 - merOrderNo:{}", customerOrderCode );
                return "fail";
            }
            if ( withdrawDetail.getStatus() == 2 ) {
                log.error( "已有代付记录 - merOrderNo:{}", customerOrderCode );
                return "success";
            }
            payAgentService.processOrderPay( withdrawDetail, payAgentLog, orderCode, payAgentChannel, "2".equals( status ) );
            log.info( payAgentPlatform.getName()
                    + "订单号:{},回调状态:{},", customerOrderCode, "2".equals( status ) ? "成功" : "失败" );
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
    public String queryOrderPay( PayAgentLog payAgentLog ) throws Exception {
        MemberWithdrawDetail withdrawDetail   = withdrawDetailMapper.selectById( payAgentLog.getWithdrawOrderNo() );
        PayAgentChannel      payAgentChannel  = payCacheUtil.getPayAgentChannel( payAgentLog.getChannelId() );
        PayAgentPlatform     payAgentPlatform = payAgentPlatformMapper.selectById( payAgentChannel.getPlatformId() );

        Map<String, Object> paramsMap = new TreeMap<>();
        paramsMap.put( "userCode", payAgentChannel.getMerId() );
        paramsMap.put( "orderCode", withdrawDetail.getWithdrawOrderNo() );
        paramsMap.put( "customerOrderCode", "" );

        String signMd5 = AESCoder.decrypt( payAgentChannel.getSignMd5() );
        // MD5(orderCode&customerOrderCode&userCode&key)
        String signStr = "&" + withdrawDetail.getWithdrawOrderNo() + "&" + payAgentChannel.getMerId() + "&" + signMd5;
        paramsMap.put( "sign", DigestUtils.md5Hex( signStr ).toUpperCase() );

        log.warn( JsonUtil.object2Json( paramsMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payAgentPlatform.getOrderQueryUrl(), packageForm( paramsMap ), null );
        if ( !CollectionUtils.isEmpty( resultMap ) && "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
            Map<String, Object> payParams = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
            int                 status    = Integer.parseInt( payParams.getOrDefault( "status", "1" ).toString() );
            if ( status == 2 || status == 3 ) {
                int orderStatus = status == 2 ? 6 : 5;
                int orderState  = status == 2 ? 1 : 2;
                payAgentService.processOrder( payAgentChannel, withdrawDetail, withdrawDetail.getUpdateTime(), orderStatus,
                        orderState );
            }
            return resultMap.getOrDefault( "message", "" ).toString();
        }
        return payAgentPlatform.getName() + "查询失败,订单号:" + withdrawDetail.getWithdrawOrderNo();
    }
}
