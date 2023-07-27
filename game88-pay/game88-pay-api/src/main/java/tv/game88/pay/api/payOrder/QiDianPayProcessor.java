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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository ( value = ConstantsPay.QIDIAN_PAY + "Processor" )
@Log4j2
public class QiDianPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "起点支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "app_id", payPlatform.getMerId() );
        params.put( "out_trade_no", reqPayRecharge.getOrderNo() );
        params.put( "trade_type", payChannel.getChannelCode() );
        params.put( "total_amount", reqPayRecharge.getMoney().multiply( BigDecimal.valueOf( 100 ) )
                .setScale( 0, RoundingMode.HALF_UP ) );

        params.put( "notify_url", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );

        String tempStr = this.assemblyUrl( params ) + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Order: {}", tempStr );
        params.put( "sign", DigestUtils.md5Hex( tempStr ) );
        params.put( "interface_version", "V2.0" );

        log.warn( JsonUtil.object2Json( params ) );

        String resultStr = this.sendPostString( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", resultStr, payChannel.getChannelCode(), reqPayRecharge.getOrderNo() );
        if ( StringUtils.isNotEmpty( resultStr ) ) {
            return filterSpecialStr( resultStr ).replaceAll( "&amp;", "&" );
        } else {
            // 存档失败原因
            reqPayRecharge.setFailReason( resultStr );
            return null;
        }
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "app_id", payPlatform.getMerId() );
        params.put( "out_trade_no", memberRechargeOnline.getOrderNo() );

        String signStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        params.put( "sign", DigestUtils.md5Hex( signStr ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( params ), null );

        if ( !CollectionUtils.isEmpty( resultMap ) && resultMap.containsKey( "orderinfo" ) ) {
            Map<String, Object> orderinfo = ( Map<String, Object> ) resultMap.getOrDefault( "orderinfo", new HashMap<>() );
            String              payState  = orderinfo.getOrDefault( "PayState", "" ).toString();
            if ( "1".equals( payState ) ) {
                BigDecimal amount = new BigDecimal( orderinfo.getOrDefault( "MerOrderAmt", 0 ).toString() );
                memberRechargeOnline.setRealMoney( amount.divide( BigDecimal.valueOf( 100 ), 2, RoundingMode.HALF_UP ) );
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String               mchOrderNo           = requestMap.getOrDefault( "out_trade_no", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( mchOrderNo );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", mchOrderNo );
            return "SUCCESS";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "FAIL";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), mchOrderNo ) ) {
            return "FAIL";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), mchOrderNo );
            return "FAIL";
        }

        String sign    = requestMap.remove( "sign" ).toString();
        String tradeNo = requestMap.remove( "trade_no" ).toString();
        requestMap.remove( "extra_return_param" );
        requestMap.remove( "trade_time" );
        requestMap.entrySet().removeIf( me -> me.getValue() == null || StringUtils.isBlank( me.getValue().toString() ) );

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signStr = this.assemblyUrl( bodyMap ) + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Callback: {}", signStr );
        String rel = DigestUtils.md5Hex( signStr );

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel );
        if ( rel.equalsIgnoreCase( sign ) ) {
            String status = requestMap.getOrDefault( "trade_status", "" ).toString();
            if ( "SUCCESS".equals( status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                memberRechargeOnline.setUpperOrderNo( tradeNo );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[]{ "SUCCESS", "FAIL" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "FAIL";
    }
}
