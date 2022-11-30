package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.pay.api.base.AbstractPay;
import tv.game88.pay.api.constants.ConstantsPay;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayPlatform;

import java.math.BigDecimal;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.MINGFAWUJI_PAY + "Processor" )
@Log4j2
public class MingFaWuJiPayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "明发无极支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        SortedMap<String, String> params = new TreeMap<>();
        params.put( "merId", payPlatform.getMerId() );
        params.put( "orderId", reqPayRecharge.getOrderNo() );
        params.put( "channel", payChannel.getChannelCode() );
        //支付金额,单位分
        params.put( "orderAmt", reqPayRecharge.getMoney().toString() );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );

        String signStr = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        log.warn( signStr );
        String sign = DigestUtils.md5Hex( signStr );
        params.put( "sign", sign );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( params );

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( payPlatform.getPayUrl() )
                .queryParams( requestMap )
                .build();

        Map<String, Object> resultMap = this.sendPostMap( uriComponents.toUri().toString(), null, reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "1".equals( resultMap.get( "code" ).toString() ) ) {
                Map urlsMap = ( Map ) resultMap.get( "data" );
                return urlsMap.get( "payUrl" ).toString();
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, String> reqMap = new TreeMap<>();
        reqMap.put( "merId", payPlatform.getMerId() );
        reqMap.put( "orderId", memberRechargeOnline.getOrderNo() );

        String signStr = this.assemblyUrl( reqMap ) + "&key=" + payPlatform.getSignMd5();
        log.warn( signStr );
        String sign = DigestUtils.md5Hex( signStr );
        reqMap.put( "sign", sign );

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.setAll( reqMap );

        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString( payPlatform.getQueryUrl() )
                .queryParams( requestMap )
                .build();

        Map<String, Object> resultMap = this.sendPostMap( uriComponents.toUri().toString(), null, null );

        log.warn( payPlatform.getName() + "查询结果:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String retCode = resultMap.getOrDefault( "code", 0 ).toString();
            if ( "1".equals( retCode ) ) {
                Map data = ( Map ) resultMap.get( "data" );
                if ( data != null && data.getOrDefault( "status", "0" ).equals( "1" ) ) {
                    memberRechargeOnline.setRealMoney( new BigDecimal( data.getOrDefault( "orderAmt", "0" ).toString() ) );
                    memberRechargeOnline.setUpperOrderNo( data.getOrDefault( "sysOrderId", 0 ).toString() );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        // 订单id
        String mchOrderNo = requestMap.getOrDefault( "orderId", "" ).toString();

        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( mchOrderNo );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", mchOrderNo );
            return "success";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail:回调IP不正确";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), mchOrderNo ) ) {
            return "fail:超时";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), mchOrderNo );
            return "fail:通道拒绝回调";
        }
        String sign = requestMap.remove( "sign" ).toString();

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + payPlatform.getSignMd5();
        String rel     = DigestUtils.md5Hex( signStr );

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel );
        if ( sign.equalsIgnoreCase( rel ) ) {
            String status = requestMap.getOrDefault( "status", "0" ).toString();
            if ( "1".equals( status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                if ( !StringUtils.hasText( memberRechargeOnline.getUpperOrderNo() ) ) {
                    // 上游订单ID
                    String payOrderId = requestMap.getOrDefault( "sysOrderId", "" ).toString();
                    memberRechargeOnline.setUpperOrderNo( payOrderId );
                }
                if ( memberRechargeOnline.getRealMoney() == null ) {
                    BigDecimal price = new BigDecimal( requestMap.getOrDefault( "orderAmt", 0 ).toString() );
                    memberRechargeOnline.setRealMoney( price );
                }
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail:验签失败";
    }
}

