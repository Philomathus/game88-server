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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.XGT_PAY + "Processor" )
@Log4j2
public class XgtPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "新GT支付";
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) throws Exception {
        Map<String, Object> params = new TreeMap<>();
        params.put( "merchantId", payPlatform.getMerId() );
        params.put( "orderId", reqPayRecharge.getOrderNo() );
        params.put( "orderAmount", reqPayRecharge.getMoney().setScale( 2, RoundingMode.HALF_UP ) );
        params.put( "channelType", payChannel.getChannelCode() );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );

        String sign = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Order: {}", sign );
        params.put( "sign", DigestUtils.md5Hex( sign ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) && "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            return dataMap.get( "payUrl" ).toString();
        } else {
            reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
        }

        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) throws Exception {
        Map<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "merchantId", payPlatform.getMerId() );
        bodyMap.put( "orderId", memberRechargeOnline.getOrderNo() );
        String sign = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Query: {}", sign );
        bodyMap.put( "sign", DigestUtils.md5Hex( sign ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( bodyMap ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) && "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            return "paid".equals( dataMap.getOrDefault( "status", "" ).toString() );
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception {
        String               orderNo              = requestMap.getOrDefault( "orderId", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( orderNo );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", orderNo );
            return "ok";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );
        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), orderNo ) ) {
            return "fail";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), orderNo );
            return "fail";
        }

        String                    sign    = ( String ) requestMap.remove( "sign" );
        SortedMap<String, Object> treeMap = new TreeMap<>( requestMap );
        String                    tempStr = this.assemblyUrl( treeMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Callback: {}", tempStr );
        String mySign = DigestUtils.md5Hex( tempStr );

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + mySign );
        if ( StringUtils.equalsIgnoreCase( sign, mySign ) ) {
            String status = requestMap.getOrDefault( "status", "" ).toString();
            if ( StringUtils.equals( "ok", status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                BigDecimal amount = new BigDecimal( requestMap.getOrDefault( "amount", 0 ).toString() );
                memberRechargeOnline.setRealMoney( amount.setScale( 2, RoundingMode.HALF_UP ) );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "ok", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "FAIL";
    }
}
