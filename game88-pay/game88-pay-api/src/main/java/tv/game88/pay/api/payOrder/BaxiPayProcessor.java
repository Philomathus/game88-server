package tv.game88.pay.api.payOrder;


import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RSACoder;
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
import java.util.UUID;

@Repository( value = ConstantsPay.BAXI_PAY + "Processor" )
@Log4j2
public class BaxiPayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "巴西支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "merId", payPlatform.getMerId() );
        params.put( "orderId", reqPayRecharge.getOrderNo() );
        params.put( "orderAmt", reqPayRecharge.getMoney().toString() );
        params.put( "channel", payChannel.getChannelCode() );
        params.put( "desc", "desc" );
        params.put( "attch", "attch" );
        params.put( "smstyle", "1" );
        params.put( "userId", "userId" );
        params.put( "ip", reqPayRecharge.getRealIp() );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "returnUrl", configEnvCacheUtil.getConf( "payReturnUrl" ) );
        params.put( "nonceStr", UUID.randomUUID().toString().replace( "-", "" ) );
        String signTemp = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        signTemp = DigestUtils.md5Hex( signTemp ).toUpperCase();
        try {
            signTemp = RSACoder.signSha256Rsa( signTemp, AESCoder.decrypt( payPlatform.getSignPrivateKey() ) );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
        params.put( "sign", signTemp );

        log.info( params );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageJson( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "1".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                Map<String, String> urlsMap = ( Map<String, String> ) resultMap.get( "data" );
                if ( !CollectionUtils.isEmpty( urlsMap ) ) {
                    return urlsMap.get( "payurl" );
                }
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        String              orderNo = memberRechargeOnline.getOrderNo();
        Map<String, Object> params  = new TreeMap<>();
        params.put( "merId", payPlatform.getMerId() );
        params.put( "orderId", orderNo );
        params.put( "nonceStr", UUID.randomUUID().toString().replace( "-", "" ) );

        String signTemp = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        signTemp = DigestUtils.md5Hex( signTemp ).toUpperCase();
        try {
            signTemp = RSACoder.signSha256Rsa( signTemp, AESCoder.decrypt( payPlatform.getSignPrivateKey() ) );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
        params.put( "sign", signTemp );
        log.warn( JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageJson( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            int code = ( int ) resultMap.get( "code" );
            if ( code == 1 ) {
                Map<String, String> dataMap = ( Map<String, String> ) resultMap.get( "data" );
                String              status  = dataMap.getOrDefault( "status", "" );
                if ( "1".equals( status ) ) {
                    return true;
                }
            }
        }
        return false;
    }


    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String               sign                 = requestMap.getOrDefault( "sign", "" ).toString();
        String               merOrderNo           = requestMap.getOrDefault( "orderId", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( merOrderNo );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", merOrderNo );
            return "success";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );

        SortedMap<String, Object> sortedMap = new TreeMap<>( requestMap );
        sortedMap.remove( "sign" );
        String signTemp = this.assemblyUrl( sortedMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        signTemp = DigestUtils.md5Hex( signTemp ).toUpperCase();

        try {
            if ( !RSACoder.verifySha256Rsa( signTemp, AESCoder.decrypt( payPlatform.getSignPublicKey() ), sign ) ) {
                log.warn( "验签失败" );
                return "fail";
            }
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

        PayChannel payChannel = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "FAIL";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), merOrderNo ) ) {
            return "FAIL";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), merOrderNo );
            return "FAIL";
        }

        BigDecimal amount     = new BigDecimal( requestMap.getOrDefault( "orderAmt", 0 ).toString() );
        String     sysOrderId = requestMap.getOrDefault( "sysOrderId", "" ).toString();
        int        status     = Integer.parseInt( requestMap.getOrDefault( "status", -1 ).toString() );
        if ( ( status == 1 ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
            // 实际金额注入,这里单位是分，所以要除以100
            memberRechargeOnline.setRealMoney( amount.setScale( 2, RoundingMode.HALF_UP ) );
            memberRechargeOnline.setUpperOrderNo( sysOrderId );
            return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                    payChannel.getName() );
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}

