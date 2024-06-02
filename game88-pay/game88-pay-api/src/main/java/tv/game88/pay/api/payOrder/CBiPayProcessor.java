package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.pay.api.base.AbstractPay;
import tv.game88.pay.api.constants.ConstantsPay;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayPlatform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.CBI_PAY + "Processor" )
@Log4j2
public class CBiPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "C币支付";
    }

    @Override
     public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) throws Exception {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "userCode", payPlatform.getMerId() );
        params.put( "orderCode", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge.getMoney().setScale( 0, RoundingMode.HALF_UP ).toString() );
        params.put( "payType", "3" );
        params.put( "callbackUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );

        // MD5(orderCode&amount&payType&userCode&key)
        String signStr = reqPayRecharge.getOrderNo() + "&" + reqPayRecharge.getMoney() + "&3&" + payPlatform.getMerId() + "&"
                + payPlatform.getSignMd5();
        params.put( "sign", DigestUtils.md5Hex( signStr ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                Map<String, Object> payParams = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
                return payParams.getOrDefault( "url", "" ).toString();
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) throws Exception {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "userCode", payPlatform.getMerId() );
        params.put( "orderCode", "" );
        params.put( "customerOrderCode", memberRechargeOnline.getOrderNo() );

        // MD5(orderCode&customerOrderCode&userCode&key)
        String signStr = "&" + memberRechargeOnline.getOrderNo() + "&" + payPlatform.getMerId() + "&" + payPlatform.getSignMd5();
        params.put( "sign", DigestUtils.md5Hex( signStr ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                Map<String, Object> payParams = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
                int                 status    = Integer.parseInt( payParams.getOrDefault( "status", "1" ).toString() );
                if ( status == 3 ) {
                    memberRechargeOnline.setRealMoney( new BigDecimal( payParams.getOrDefault( "amount", "0" ).toString() ) );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception {
        String               merOrderNo           = requestMap.getOrDefault( "orderCode", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( merOrderNo );
        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", merOrderNo );
            return "success";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );
        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), merOrderNo ) ) {
            return "fail";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), merOrderNo );
            return "fail";
        }

        String sign = ( String ) requestMap.remove( "sign" );

        String status    = requestMap.getOrDefault( "status", "1" ).toString();
        String orderCode = requestMap.getOrDefault( "orderCode", "" ).toString();
        String amount    = requestMap.getOrDefault( "amount", "-1" ).toString();
        String userCode  = requestMap.getOrDefault( "userCode", "" ).toString();

        // MD5(orderCode&amount&userCode&status&key)
        String signStr = orderCode + "&" + amount + "&" + userCode + "&" + status + "&" + payPlatform.getSignMd5();
        String mySign  = DigestUtils.md5Hex( signStr ).toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + mySign );
        if ( StringUtils.equalsIgnoreCase( sign, mySign ) && "3".equals( status )
                && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
            memberRechargeOnline.setUpperOrderNo( merOrderNo );
            return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                    payChannel.getName() );
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
