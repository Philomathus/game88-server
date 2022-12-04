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

@Repository( value = ConstantsPay.CHUANGYIN_PAY + "Processor" )
@Log4j2
public class ChuangYinPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "创银支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "mchId", payPlatform.getMerId() );
        params.put( "currency", "CNY" );
        params.put( "amount", reqPayRecharge.getMoney().setScale( 2, BigDecimal.ROUND_HALF_UP ).toString() );
        params.put( "orderId", reqPayRecharge.getOrderNo() );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "channel", payChannel.getChannelCode() );

        String signStr = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        String sign    = DigestUtils.md5Hex( signStr );
        sign = DigestUtils.md5Hex( sign );
        sign = DigestUtils.md5Hex( sign );
        params.put( "sign", sign.toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageJson( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "0".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
                return ( String ) dataMap.get( "payData" );
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "mchId", payPlatform.getMerId() );
        params.put( "orderId", memberRechargeOnline.getOrderNo() );

        String signStr = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        String sign    = DigestUtils.md5Hex( signStr );
        sign = DigestUtils.md5Hex( sign );
        sign = DigestUtils.md5Hex( sign );
        params.put( "sign", sign.toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageJson( params ), null );

        log.warn( payPlatform.getName() + "查询结果 - result:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", -1 ).toString();
            String data = resultMap.getOrDefault( "data", "N" ).toString();
            return "0".equals( code ) && "Y".equals( data );
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {

        String order_sn = requestMap.getOrDefault( "orderId", "" ).toString();

        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( order_sn );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", order_sn );
            return "ok";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), order_sn ) ) {
            return "fail";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), order_sn );
            return "fail";
        }

        BigDecimal money     = new BigDecimal( requestMap.getOrDefault( "amount", "" ).toString() );
        String     payStatus = requestMap.getOrDefault( "payStatus", "" ).toString();
        String     sign      = requestMap.remove( "sign" ).toString();

        SortedMap<String, Object> params  = new TreeMap<>( requestMap );
        String                    signStr = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        String                    rel     = DigestUtils.md5Hex( signStr );
        rel = DigestUtils.md5Hex( rel );
        rel = DigestUtils.md5Hex( rel ).toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel );
        if ( rel.equalsIgnoreCase( sign ) ) {
            if ( "Y".equals( payStatus ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                memberRechargeOnline.setRealMoney( money.setScale( 2, RoundingMode.HALF_UP ) );
                memberRechargeOnline.setUpperOrderNo( order_sn );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "ok", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
