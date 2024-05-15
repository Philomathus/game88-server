package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

@Repository( value = ConstantsPay.SHUNDA2_PAY + "Processor" )
@Log4j2
public class ShunDa3PayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "顺达3支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "mchKey", payPlatform.getMerId() );
        params.put( "product", payChannel.getChannelCode() );
        params.put( "nonce", UUID.randomUUID().toString().replace( "-", "" ) );
        params.put( "timestamp", System.currentTimeMillis() );
        params.put( "mchOrderNo", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge
                .getMoney()
                .multiply( BigDecimal.valueOf( 100 ) )
                .setScale( 0, RoundingMode.HALF_UP )
                .intValue() );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );

        String signStr = this.assemblyUrl( params ) + payPlatform.getSignMd5();
        params.put( "sign", DigestUtils.md5Hex( signStr ) );

        log.warn( JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageJson( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                Map<String, Object> urlMap = ( Map<String, Object> ) dataMap.get( "url" );
                return ( String ) urlMap.get( "payUrl" );
            } else {
                // 存档失败原因
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        String                    orderNo = memberRechargeOnline.getOrderNo();
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "mchKey", payPlatform.getMerId() );
        bodyMap.put( "mchOrderNo", orderNo );
        bodyMap.put( "nonce", UUID.randomUUID().toString().replace( "-", "" ) );
        bodyMap.put( "timestamp", System.currentTimeMillis() );

        String signStr = this.assemblyUrl( bodyMap ) + payPlatform.getSignMd5();
        bodyMap.put( "sign", DigestUtils.md5Hex( signStr ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageJson( bodyMap ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            if ( "200".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                if ( "SUCCESS".equals( dataMap.getOrDefault( "payStatus", "0" ).toString() ) ) {
                    String realAmount = dataMap.getOrDefault( "realAmount", "-1" ).toString();
                    memberRechargeOnline.setRealMoney( new BigDecimal( realAmount ).divide( BigDecimal.valueOf( 100 ), 2,
                            RoundingMode.HALF_DOWN ) );
                    memberRechargeOnline.setUpperOrderNo( dataMap.getOrDefault( "serialOrderNo", "" ).toString() );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String               orderNo              = ( String ) requestMap.get( "mchOrderNo" );
        String               sign                 = ( String ) requestMap.remove( "sign" );
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( orderNo );
        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", orderNo );
            return "SUCCESS";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "FAIL";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), orderNo ) ) {
            return "FAIL";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), orderNo );
            return "FAIL";
        }

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signStr  = this.assemblyUrl( bodyMap ) + payPlatform.getSignMd5();
        String signTemp = DigestUtils.md5Hex( signStr );

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + signTemp );
        if ( signTemp.equals( sign ) ) {
            String status = requestMap.getOrDefault( "payStatus", "FAIL" ).toString();
            if ( "SUCCESS".equals( status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "SUCCESS", "FAIL" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "FAIL";
    }
}
