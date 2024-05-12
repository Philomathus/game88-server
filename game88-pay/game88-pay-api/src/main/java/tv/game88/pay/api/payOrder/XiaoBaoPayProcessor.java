package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.pay.api.base.AbstractPay;
import tv.game88.pay.api.constants.ConstantsPay;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayPlatform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.XIAOBAO_PAY + "Processor" )
@Log4j2
public class XiaoBaoPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "小宝支付";
    }

    @Override
     public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) throws Exception {
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "merchant_no", payPlatform.getMerId() );
        bodyMap.put( "order_money", reqPayRecharge.getMoney().setScale( 2, RoundingMode.HALF_UP ) );
        bodyMap.put( "order_time", LocalDateTimeUtils.format( LocalDateTime.now() ) );
        bodyMap.put( "pay_type", payChannel.getChannelCode() );
        bodyMap.put( "order_no", reqPayRecharge.getOrderNo() );
        bodyMap.put( "notify_url", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        String str = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        bodyMap.put( "sign", DigestUtils.md5Hex( str ).toUpperCase() );

        StringBuilder sb = new StringBuilder(
                "<form id='Form1' name='Form1' method='post' action='" + payPlatform.getPayUrl() + "'>" );

        bodyMap.forEach( ( k, v ) -> sb
                .append( "<input type='hidden' name='" )
                .append( k )
                .append( "' value='" )
                .append( v )
                .append( "'>" ) );

        sb.append( "</form><script>var form = document.getElementById('Form1');form.submit();</script>" );
        reqPayRecharge.setUrlType( 1 );
        return sb.toString();
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) throws Exception {
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "merchant_no", payPlatform.getMerId() );
        bodyMap.put( "merchant_order_no", memberRechargeOnline.getOrderNo() );
        bodyMap.put( "rand", System.currentTimeMillis() );
        String str = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        bodyMap.put( "sign", DigestUtils.md5Hex( str ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( bodyMap ), null );

        log.warn( "戏子查询 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        Map<String, Object> signMap   = new TreeMap<>( resultMap );
        String              sign      = String.valueOf( signMap.remove( "sign" ) );
        String              signValue = this.assemblyUrl( signMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        if ( !CollectionUtils.isEmpty( resultMap ) && StringUtils.equals( sign, DigestUtils.md5Hex( signValue ).toUpperCase() )
                && "1".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) && "success".equals( resultMap
                .getOrDefault( "order_status", "fail" )
                .toString() ) ) {
            return true;
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception {
        String               out_trade_no         = String.valueOf( requestMap.get( "order_no" ) );
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( out_trade_no );
        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", out_trade_no );
            return "success";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );
        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), out_trade_no ) ) {
            return "fail";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(),
                    out_trade_no );
            return "fail";
        }
        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );
        String                    sign    = ( String ) bodyMap.remove( "sign" );
        String                    signMd5 = DigestUtils
                .md5Hex( this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() ) )
                .toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + signMd5 );
        if ( StringUtils.equals( sign, signMd5 )
                && StringUtils.equals( "success", String.valueOf( requestMap.get( "order_status" ) ) )
                && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
            memberRechargeOnline.setRealMoney( new BigDecimal( String.valueOf( requestMap.get( "order_money" ) ) ) );
            memberRechargeOnline.setUpperOrderNo( String.valueOf( requestMap.get( "transaction_no" ) ) );
            return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                    payChannel.getName() );
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
