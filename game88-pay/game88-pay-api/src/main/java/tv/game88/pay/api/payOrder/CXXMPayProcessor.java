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

import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.CXXM_PAY + "Processor" )
@Log4j2
public class CXXMPayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "创新熊猫支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "userCode", payPlatform.getMerId() );
        params.put( "channelCode", payChannel.getChannelCode() );
        params.put( "orderId", reqPayRecharge.getOrderNo() );
        params.put( "orderMoney", reqPayRecharge.getMoney().setScale( 0, RoundingMode.HALF_UP ).toString() );
        params.put( "callbackUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        //        params.put( "clientIp", reqPayRecharge.getRealIp() );
        //        params.put( "returnUrl", configEnvCacheUtil.getConf( "payReturnUrl" ) );

        String signStr = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        log.info( signStr );
        params.put( "sign", DigestUtils.md5Hex( signStr ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "200".equals( code ) ) {
                return ( String ) resultMap.getOrDefault( "result", Collections.emptyMap() );
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "userCode", payPlatform.getMerId() );
        params.put( "orderId", memberRechargeOnline.getOrderNo() );

        String sign = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        sign = DigestUtils.md5Hex( sign ).toUpperCase();
        params.put( "sign", sign );
        log.warn( JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "true".equals( resultMap.getOrDefault( "success", "" ).toString() ) ) {
                memberRechargeOnline.setRealMoney( memberRechargeOnline.getMoney() );
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String merOrderNo = requestMap.getOrDefault( "orderId", "" ).toString();
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
        requestMap.remove( "errMsg" );

        SortedMap<String, Object> bodyMap  = new TreeMap<>( requestMap );
        String                    signTemp = this.assemblyUrl( bodyMap ) + "&key=" + payPlatform.getSignMd5();
        String mySign = DigestUtils.md5Hex( signTemp ).toUpperCase();

        String status = requestMap.getOrDefault( "orderStatus", "1" ).toString();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + mySign );
        if ( StringUtils.equalsIgnoreCase( sign, mySign ) && "2".equals( status )
                && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
            memberRechargeOnline.setUpperOrderNo( merOrderNo );
            return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" }, payChannel.getName() );
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
