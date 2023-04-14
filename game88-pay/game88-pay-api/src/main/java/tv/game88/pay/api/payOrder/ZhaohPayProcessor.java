package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

@Repository( value = ConstantsPay.ZHAOH_PAY + "Processor" )
@Log4j2
public class ZhaohPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "找换宝支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "merchNo", payPlatform.getMerId() );
        params.put( "userId", reqPayRecharge.getUserId() );
        params.put( "orderNo", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge.getMoney().setScale( 0, RoundingMode.HALF_UP ) );
        params.put( "cashierType", "PC" );
        params.put( "callbackUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "viewUrl", configEnvCacheUtil.getConf( "payReturnUrl" ) );

        String signStr = this.assemblyUrl( params ) + "&token=" + payPlatform.getAppId() + "&sign="
                + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Order: {}", signStr );
        params.put( "sign", DigestUtils.md5Hex( signStr ) );

        if ( StringUtils.isNotBlank( payChannel.getChannelCode() ) ) {
            params.put( "payChannelCode", payChannel.getChannelCode() );
        }

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), createEntity( params, payPlatform.getAppId()
                , AESCoder.decrypt( payPlatform.getSignPublicKey() ) ), reqPayRecharge );
        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String dataStr = resultMap.getOrDefault( "data", "" ).toString();
            try {
                Map<String, Object> resDataMap = JsonUtil.json2Map( RSACoder.decryptByPublicKey( dataStr,
                        AESCoder.decrypt( payPlatform.getSignPublicKey() ) ) );
                log.warn( "解密数据:" + JsonUtil.object2Json( resDataMap ) );
                if ( "0".equals( resDataMap.getOrDefault( "code", "" ) ) ) {
                    Map<String, Object> data = JsonUtil.json2Map( resDataMap.getOrDefault( "data", "" ).toString() );
                    if ( !CollectionUtils.isEmpty( data ) ) {
                        return data.get( "payUrl" ).toString();
                    }
                } else {
                    reqPayRecharge.setFailReason( resDataMap.getOrDefault( "message", "" ).toString() );
                }
            } catch ( Exception e ) {
                throw new RuntimeException( e );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "merchNo", payPlatform.getMerId() );
        params.put( "orderNo", memberRechargeOnline.getOrderNo() );

        String signStr = this.assemblyUrl( params ) + "&token=" + payPlatform.getAppId() + "&sign="
                + AESCoder.decrypt( payPlatform.getSignMd5() );
        params.put( "sign", DigestUtils.md5Hex( signStr ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), createEntity( params,
                payPlatform.getAppId(), AESCoder.decrypt( payPlatform.getSignPublicKey() ) ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String dataStr = resultMap.getOrDefault( "data", "" ).toString();
            try {
                Map<String, Object> resDataMap = JsonUtil.json2Map( RSACoder.decryptByPublicKey( dataStr,
                        AESCoder.decrypt( payPlatform.getSignPublicKey() ) ) );
                log.warn( "解密数据:" + JsonUtil.object2Json( resDataMap ) );
                if ( "0".equals( resDataMap.getOrDefault( "code", "" ) ) ) {
                    Map<String, Object> data = JsonUtil.json2Map( resDataMap.getOrDefault( "data", "" ).toString() );
                    if ( !CollectionUtils.isEmpty( data ) && "PAY_SUC".equals( data.getOrDefault( "orderState", "" ) ) ) {
                        memberRechargeOnline.setRealMoney( new BigDecimal( data.getOrDefault( "payAmount", "0" ).toString() ) );
                        return true;
                    }
                }
            } catch ( Exception e ) {
                throw new RuntimeException( e );
            }

        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String               merOrderNo           = requestMap.getOrDefault( "orderNo", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( merOrderNo );
        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", merOrderNo );
            return "SUCCESS";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );
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
        String dataStr = requestMap.getOrDefault( "data", "" ).toString();
        try {
            Map<String, Object> dataMap = JsonUtil.json2Map( RSACoder.decryptByPrivateKey( dataStr,
                    AESCoder.decrypt( payPlatform.getSignPrivateKey() ) ) );
            // 解密后对签名验证
            SortedMap<String, Object> treeMap = new TreeMap<>( dataMap );

            String sign = ( String ) treeMap.remove( "sign" );

            String mySign = DigestUtils.md5Hex( this.assemblyUrl( treeMap ) + "&token=" + payPlatform.getAppId() + "&sign="
                    + AESCoder.decrypt( payPlatform.getSignMd5() ) );

            log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + mySign );
            if ( StringUtils.equalsIgnoreCase( sign, mySign ) ) {
                String orderState = treeMap.getOrDefault( "orderState", "" ).toString();
                if ( StringUtils.equals( "PAY_SUC", orderState )
                        && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                    return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "SUCCESS", "FAIL" },
                            payChannel.getName() );
                }
            }
            log.info( payPlatform.getName() + "回调验签失败" );
            return "FAIL";
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    private HttpEntity<Map<String, Object>> createEntity( Map<String, Object> params, String token, String publicKey ) {

        try {
            params.put( "data", RSACoder.encryptByPublicKey( this.assemblyUrl( params ), publicKey ) );
        } catch ( Exception e ) {
            log.error( "Error inserting data param into Map", e );
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add( "token", token );
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );

        return new HttpEntity<>( params, httpHeaders );
    }
}
