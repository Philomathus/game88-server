package tv.game88.pay.api.payOrder;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.HUOJIAN_PAY + "Processor" )
@Log4j2
public class HuoJianPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "阿乐火箭支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "time_stamp", String.valueOf( System.currentTimeMillis() / 1000 ) );
        params.put( "mch_id", payPlatform.getMerId() );
        params.put( "nonce", IdWorker.get32UUID() );
        params.put( "out_order_no", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge
                .getMoney()
                .multiply( new BigDecimal( 100 ) )
                .setScale( 0, RoundingMode.HALF_UP )
                .toString() );
        params.put( "pay_type", payChannel.getChannelCode() );
        params.put( "client_ip", reqPayRecharge.getRealIp() );
        params.put( "attach", "attach" );
        params.put( "notify_url", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );

        String signStr = this.assemblyUrl( params );
        String sign    = null;
        try {
            sign = HMACSHA256( signStr, AESCoder.decrypt( payPlatform.getSignMd5() ) );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
            reqPayRecharge.setFailReason( e.getMessage() );
        }
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageJson( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "2000".equals( code ) ) {
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
                return dataMap.getOrDefault( "pay_url", "" ).toString();
            } else {
                // 存档失败原因
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "retMsg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "time_stamp", String.valueOf( System.currentTimeMillis() / 1000 ) );
        params.put( "mch_id", payPlatform.getMerId() );
        params.put( "nonce", IdWorker.get32UUID() );
        params.put( "out_order_no", memberRechargeOnline.getOrderNo() );

        String signStr = this.assemblyUrl( params );
        String sign    = null;
        try {
            sign = HMACSHA256( signStr, AESCoder.decrypt( payPlatform.getSignMd5() ) );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageJson( params ), null );

        log.warn( "新火箭支付查询结果:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "2000".equals( code ) ) {
                Map<String, Object> dataMap  = ( Map<String, Object> ) resultMap.get( "data" );
                BigDecimal          amount   = new BigDecimal( dataMap.getOrDefault( "amount", 0 ).toString() );
                String              trade_no = dataMap.getOrDefault( "trade_no", "" ).toString();
                memberRechargeOnline.setRealMoney( amount.divide( BigDecimal.valueOf( 100 ), 2, RoundingMode.HALF_UP ) );
                memberRechargeOnline.setUpperOrderNo( trade_no );
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        // 订单id
        String out_order_no = requestMap.getOrDefault( "out_order_no", "" ).toString();
        // 上游订单ID
        String trade_no = requestMap.getOrDefault( "trade_no", "" ).toString();

        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( out_order_no );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", out_order_no );
            return "success";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), out_order_no ) ) {
            return "fail";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(),
                    out_order_no );
            return "fail";
        }

        String     time_stamp = requestMap.getOrDefault( "time_stamp", "" ).toString();
        String     mch_id     = requestMap.getOrDefault( "mch_id", "" ).toString();
        String     nonce      = requestMap.getOrDefault( "nonce", "" ).toString();
        String     pay_type   = requestMap.getOrDefault( "pay_type", "" ).toString();
        BigDecimal amount     = new BigDecimal( requestMap.getOrDefault( "amount", 0 ).toString() );
        BigDecimal fee        = new BigDecimal( requestMap.getOrDefault( "fee", 0 ).toString() );
        String     status     = requestMap.getOrDefault( "status", "" ).toString();
        String     attach     = requestMap.getOrDefault( "attach", "" ).toString();
        String     sign       = requestMap.getOrDefault( "sign", "" ).toString();

        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "out_order_no", out_order_no );
        bodyMap.put( "trade_no", trade_no );
        bodyMap.put( "time_stamp", time_stamp );
        bodyMap.put( "mch_id", mch_id );
        bodyMap.put( "nonce", nonce );
        bodyMap.put( "pay_type", pay_type );
        bodyMap.put( "amount", amount );
        bodyMap.put( "fee", fee );
        bodyMap.put( "status", status );
        bodyMap.put( "attach", attach );

        String signStr = this.assemblyUrl( bodyMap );
        String rel     = null;
        try {
            rel = HMACSHA256( signStr, AESCoder.decrypt( payPlatform.getSignMd5() ) );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel );
        if ( sign.equalsIgnoreCase( rel ) ) {
            if ( "SUCCESS".equals( status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                memberRechargeOnline.setRealMoney( amount.divide( BigDecimal.valueOf( 100 ), 2, RoundingMode.HALF_UP ) );
                memberRechargeOnline.setUpperOrderNo( trade_no );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }

    public static String HMACSHA256( String data, String key ) throws Exception {
        Mac           sha256_HMAC = Mac.getInstance( "HmacSHA256" );
        SecretKeySpec secret_key  = new SecretKeySpec( key.getBytes( StandardCharsets.UTF_8 ), "HmacSHA256" );
        sha256_HMAC.init( secret_key );
        byte[]        array = sha256_HMAC.doFinal( data.getBytes( StandardCharsets.UTF_8 ) );
        StringBuilder sb    = new StringBuilder();
        for ( byte item : array ) {
            sb.append( Integer.toHexString( ( item & 0xFF ) | 0x100 ).substring( 1, 3 ) );
        }
        return sb.toString();
    }
}
