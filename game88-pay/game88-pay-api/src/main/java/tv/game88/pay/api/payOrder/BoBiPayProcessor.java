package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
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

@Repository( value = ConstantsPay.BOBI_PAY + "Processor" )
@Log4j2
public class BoBiPayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "波币支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "mch_id", payPlatform.getMerId() );
        params.put( "currency_id", "1" );
        params.put( "money", reqPayRecharge.getMoney().setScale( 0, RoundingMode.HALF_UP ) );
        params.put( "cp_order_id", reqPayRecharge.getOrderNo() );
        params.put( "callback_url", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "time", System.currentTimeMillis() / 1000 );

        String signTemp = this.assemblyUrl( params ) + "&pri_key=" + payPlatform.getSignMd5();
        String sign     = DigestUtils.md5Hex( signTemp ).toLowerCase();
        params.put( "sign", sign );
        log.warn( JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageJson( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "0".equals( code ) ) {
                return resultMap.getOrDefault( "pay_url", "" ).toString();
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "mch_id", payPlatform.getMerId() );
        bodyMap.put( "cp_order_id", memberRechargeOnline.getOrderNo() );

        String sign = this.assemblyUrl( bodyMap ) + "&pri_key=" + payPlatform.getSignMd5();
        bodyMap.put( "sign", DigestUtils.md5Hex( sign ).toLowerCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageJson( bodyMap ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "0".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                int status = Integer.parseInt( resultMap.getOrDefault( "status", -1 ).toString() );
                if ( status == 1 ) {
                    BigDecimal amount = new BigDecimal( resultMap.getOrDefault( "money", 0 ).toString() );
                    memberRechargeOnline.setRealMoney( amount.divide( BigDecimal.valueOf( 100 ), 0, RoundingMode.HALF_UP ) );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {

        String               merOrderNo           = requestMap.getOrDefault( "cp_order_id", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( merOrderNo );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", merOrderNo );
            return "ok";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }

        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), merOrderNo ) ) {
            return "fail";
        }

        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), merOrderNo );
            return "fail";
        }

        SortedMap<String, Object> treeMap = new TreeMap<>( requestMap );

        String sign   = ( String ) treeMap.remove( "sign" );
        String data   = this.assemblyUrl( treeMap ) + "&pri_key=" + payPlatform.getSignMd5();
        String mySign = DigestUtils.md5Hex( data );

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + mySign );
        if ( StringUtils.equalsIgnoreCase( sign, mySign ) ) {
            String code = requestMap.getOrDefault( "status", "-1" ).toString();
            if ( StringUtils.equals( "1", code ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "ok", "fail" },
                        payChannel.getName() );
            }
        }

        log.info( payPlatform.getName() + "回调验签失败" );

        return "fail";
    }
}
