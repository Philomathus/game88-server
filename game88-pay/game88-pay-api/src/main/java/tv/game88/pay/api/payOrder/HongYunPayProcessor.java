package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.util.UUID;

@Repository( value = ConstantsPay.HONGYUN_PAY + "Processor" )
@Log4j2
public class HongYunPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "鸿运支付";
    }

    @Override
     public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) throws Exception {
        Map<String, Object> params = new TreeMap<>();
        params.put( "mch_id", payPlatform.getMerId() );
        params.put( "trade_type", payChannel.getChannelCode() );
        params.put( "nonce", UUID.randomUUID().toString().replace( "-", "" ) );
        params.put( "timestamp", System.currentTimeMillis() );
        params.put( "subject", "subject" );
        params.put( "out_trade_no", reqPayRecharge.getOrderNo() );
        params.put( "total_fee", reqPayRecharge
                .getMoney()
                .multiply( BigDecimal.valueOf( 100 ) )
                .setScale( 0, RoundingMode.HALF_UP )
                .intValue() );
        params.put( "spbill_create_ip", reqPayRecharge.getRealIp() );
        params.put( "notify_url", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "sign_type", "MD5" );
        StringBuilder sb = new StringBuilder();
        params.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String sign = sb.substring( 0, sb.length() - 1 ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        sign = DigestUtils.md5Hex( sign ).toUpperCase();
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageJson( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "SUCCESS".equals( resultMap.getOrDefault( "result_code", "" ).toString() ) ) {
                return ( String ) resultMap.get( "pay_url" );
            } else {
                // 存档失败原因
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "result_msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) throws Exception {
        String                    orderNo = memberRechargeOnline.getOrderNo();
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "mch_id", payPlatform.getMerId() );
        bodyMap.put( "out_trade_no", orderNo );
        bodyMap.put( "sign_type", "MD5" );
        StringBuilder sb = new StringBuilder();
        bodyMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String sign = sb.substring( 0, sb.length() - 1 ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( sign );
        sign = DigestUtils.md5Hex( sign ).toUpperCase();
        bodyMap.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageJson( bodyMap ), null );

        log.warn( "鸿运支付查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "SUCCESS".equals( resultMap.getOrDefault( "result_code", "FAIL" ).toString() ) ) {
                int status = Integer.parseInt( resultMap.getOrDefault( "trade_status", "0" ).toString() );
                return status == 1;
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception {
        String               orderNo              = ( String ) requestMap.get( "out_trade_no" );
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
        StringBuilder             sb      = new StringBuilder();
        bodyMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String signTemp = sb.substring( 0, sb.length() - 1 ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        signTemp = DigestUtils.md5Hex( signTemp ).toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + signTemp );
        if ( signTemp.equals( sign ) ) {
            BigDecimal userPayAmount = new BigDecimal( requestMap.getOrDefault( "total_fee", 0 ).toString() );
            String     trade_no      = ( String ) requestMap.get( "trade_no" );
            String     status        = ( String ) requestMap.getOrDefault( "result_code", "FAIL" );
            if ( "SUCCESS".equals( status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                memberRechargeOnline.setRealMoney( userPayAmount.divide( BigDecimal.valueOf( 100 ), 2, RoundingMode.HALF_UP ) );
                memberRechargeOnline.setUpperOrderNo( trade_no );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "SUCCESS", "FAIL" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "FAIL";
    }
}
