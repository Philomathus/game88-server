package tv.game88.pay.api.payOrder;


import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Base64Utils;
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
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Repository( value = ConstantsPay.BAXI_PAY + "Processor" )
@Log4j2
public class BaxiPayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "麒麟支付";
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
        params.put( "returnUrl", configEnvCacheUtil.getConf( "payReturnUrl" ));
        params.put( "nonceStr", UUID.randomUUID().toString().replace( "-", "" ) );
        String signTemp = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        signTemp = DigestUtils.md5Hex( signTemp ).toUpperCase();
        try {
            log.info( signTemp );
            log.info( signTemp );
            signTemp = RSACoder.encryptByPrivateKey( signTemp, AESCoder.decrypt( payPlatform.getSignPrivateKey() ) );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
        params.put( "sign", signTemp );

        log.info(  params );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageJson( params ), reqPayRecharge );

        log.warn( payPlatform.getName()+"下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ),payChannel.getChannelCode(),reqPayRecharge.getOrderNo());
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

        String signTemp = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        signTemp = DigestUtils.md5Hex( signTemp ).toUpperCase();
        try {
            signTemp = RSACoder.encryptByPrivateKey( signTemp, AESCoder.decrypt( payPlatform.getSignPrivateKey() ) );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
        params.put( "sign", signTemp );
        log.warn( JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap(payPlatform.getQueryUrl(), packageJson( params ), null);

        log.warn( "麒麟支付查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );

        int code = ( int ) resultMap.get( "code" );
        resultMap = ( Map<String, Object> ) resultMap.get( "data" );
        if ( code != 1 || CollectionUtils.isEmpty( resultMap ) ) {
            log.warn( "查询失败" );
            return false;
        }
        String sign = resultMap.getOrDefault( "sign", "" ).toString();
        params.clear();
        params.putAll( resultMap );
        params.remove( "sign" );
        String str = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        str = DigestUtils.md5Hex( str ).toUpperCase();
        return buildRSAverifyByPublicKey( str, payPlatform.getSignPublicKey(), sign ) && resultMap
                .getOrDefault( "status", "-1" )
                .equals( "1" );
    }


    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String        sign    = requestMap.getOrDefault( "sign", "" ).toString();
        String        merOrderNo = requestMap.getOrDefault( "orderId", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( merOrderNo );

        if (memberRechargeOnline.getStatus() == 1) {
            log.warn("订单已成功，无需继续回调 - orderNo:{}", merOrderNo);
            return "success";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId());

        SortedMap<String, Object> sortedMap      = new TreeMap<>( requestMap );
        sortedMap.remove( "sign" );
        String signTemp = this.assemblyUrl( sortedMap ) + "&key=" + payPlatform.getSignMd5();
        signTemp = DigestUtils.md5Hex( signTemp ).toUpperCase();

        try {
            if ( !RSACoder.verifySha256Rsa( signTemp, AESCoder.decrypt( payPlatform.getSignPublicKey() ), sign ) ) {
                log.warn( "验签失败" );
                return "fail";
            }
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

        PayChannel payChannel = payCacheUtil.getPayChannel(memberRechargeOnline.getChannelId());

        if (this.verifyIP(requestMap, realIp, payPlatform)) {
            return "FAIL";
        }
        if (this.diffPayTime12Hour(memberRechargeOnline.getPayTime(), merOrderNo)) {
            return "FAIL";
        }
        if (!payChannel.getCanCallback()) {
            log.warn("平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), merOrderNo);
            return "FAIL";
        }

        BigDecimal amount     = new BigDecimal( requestMap.getOrDefault( "orderAmt", 0 ).toString() );
        String     sysOrderId = requestMap.getOrDefault( "sysOrderId", "" ).toString();
        int        status     = Integer.parseInt( requestMap.getOrDefault( "status", -1 ).toString() );
        if ( ( status == 1 ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
            // 实际金额注入,这里单位是分，所以要除以100
            memberRechargeOnline.setRealMoney( amount.setScale( 2, RoundingMode.HALF_UP ) );
            memberRechargeOnline.setUpperOrderNo( sysOrderId );
            return payService.updatePayJourStatus( memberRechargeOnline, new String[]{ "success", "fail" }, payChannel.getName() );
        }
        log.info( payPlatform.getName()+"回调验签失败");
        return "fail";
    }

    public boolean buildRSAverifyByPublicKey( String data, String key, String sign ) {
        try {
            //通过X509编码的Key指令获得公钥对象
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec( Base64.getDecoder().decode( key ) );
            KeyFactory keyFactory  = KeyFactory.getInstance( "RSA" );
            PublicKey publicKey   = keyFactory.generatePublic( x509KeySpec );
            Signature signature   = Signature.getInstance( "SHA256WithRSA" );
            signature.initVerify( publicKey );
            signature.update( data.getBytes( StandardCharsets.UTF_8 ) );
            return signature.verify( Base64.getDecoder().decode( sign ) );
        } catch ( Exception e ) {
            throw new RuntimeException( "验签字符串[" + data + "]时遇到异常", e );
        }
    }

    public String buildRSASignByPrivateKey( String data, String key ) {
        try {
            //通过PKCS#8编码的Key指令获得私钥对象
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec( Base64.getDecoder().decode( key ) );
            KeyFactory          keyFactory   = KeyFactory.getInstance( "RSA" );
            PrivateKey privateKey   = keyFactory.generatePrivate( pkcs8KeySpec );
            Signature           signature    = Signature.getInstance( "SHA256WithRSA" );
            signature.initSign( privateKey );
            signature.update( data.getBytes( StandardCharsets.UTF_8 ) );
            return Base64.getEncoder().encodeToString( signature.sign() );
        } catch ( Exception e ) {
            throw new RuntimeException( "签名字符串[" + data + "]时遇到异常", e );
        }
    }

}

