package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
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

@Repository( value = ConstantsPay.MING_FA2_PAY + "Processor" )
@Log4j2
public class Mingfa2PayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "明发2支付";
    }

    @Override
     public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) throws Exception {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "merchId", payPlatform.getMerId() );
        params.put( "outTradeNo", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge.getMoney().setScale( 2, RoundingMode.HALF_UP ).toString() );
        params.put( "channelCode", payChannel.getChannelCode() );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );

        String signStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( signStr );
        String sign = DigestUtils.md5Hex( signStr );
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageJson( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "40000" ).toString();
            if ( "20000".equals( code ) ) {
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
                return dataMap.getOrDefault( "payUrl", "" ).toString();
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) throws Exception {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "merchId", payPlatform.getMerId() );
        params.put( "outTradeNo", memberRechargeOnline.getOrderNo() );

        String signStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        params.put( "sign", DigestUtils.md5Hex( signStr ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageJson( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "40000" ).toString();
            if ( "20000".equals( code ) ) {
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", Collections.emptyMap() );
                String              status  = dataMap.getOrDefault( "status", "MI" ).toString();
                if ( "PS".equals( status ) ) {
                    memberRechargeOnline.setRealMoney( new BigDecimal( dataMap.getOrDefault( "amount", "0" ).toString() ) );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception {
        String               merOrderNo           = requestMap.getOrDefault( "outTradeNo", "" ).toString();
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

        requestMap.values().removeIf( value -> org.apache.commons.lang3.StringUtils.isBlank( value.toString() ) );

        SortedMap<String, Object> treeMap = new TreeMap<>( requestMap );
        String                    sign    = ( String ) treeMap.remove( "sign" );
        String mySign = DigestUtils.md5Hex(
                this.assemblyUrl( treeMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() ) );

        if ( StringUtils.equals( sign, mySign ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
            memberRechargeOnline.setUpperOrderNo( requestMap.getOrDefault( "tradeNo", "" ).toString() );
            return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                    payChannel.getName() );
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
