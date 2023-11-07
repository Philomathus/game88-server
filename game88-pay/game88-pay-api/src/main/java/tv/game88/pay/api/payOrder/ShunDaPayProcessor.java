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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.SHUNDA_PAY + "Processor" )
@Log4j2
public class ShunDaPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "顺达支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "merchantId", payPlatform.getMerId() );
        params.put( "outTradeNo", reqPayRecharge.getOrderNo() );
        params.put( "channel", payChannel.getChannelCode() );
        params.put( "amount", reqPayRecharge.getMoney().multiply( BigDecimal.valueOf( 100 ) ) );
        params.put( "ts", System.currentTimeMillis() );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );

        String tempStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        String sign    = DigestUtils.md5Hex( tempStr ).toUpperCase();
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "200".equals( code ) ) {
                Map dataMap = ( Map ) resultMap.get( "data" );
                return ( String ) dataMap.get( "url" );
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "merchantId", payPlatform.getMerId() );
        params.put( "outTradeNo", memberRechargeOnline.getOrderNo() );
        params.put( "ts", System.currentTimeMillis() );

        String tempStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        String sign    = DigestUtils.md5Hex( tempStr ).toUpperCase();
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String state = resultMap.getOrDefault( "state", "" ).toString();
            String code  = resultMap.getOrDefault( "code", "" ).toString();
            if ( "success".equals( state ) && "200".equals( code ) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String               orderid              = ( String ) requestMap.get( "outTradeNo" );
        String               sign                 = ( String ) requestMap.get( "sign" );
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( orderid );
        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", orderid );
            return "success";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );
        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), orderid ) ) {
            return "fail";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), orderid );
            return "fail";
        }
        // 去除空值
        requestMap.entrySet().removeIf( me -> me.getValue() == null || StringUtils.isBlank( me.getValue().toString() ) );
        SortedMap<String, Object> map = new TreeMap<>( requestMap );
        map.remove( "sign" );
        String tempStr = this.assemblyUrl( map ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        String signMd5 = DigestUtils.md5Hex( tempStr ).toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + signMd5 );
        if ( signMd5.equals( sign ) ) {
            String result = ( String ) requestMap.getOrDefault( "state", "" );
            if ( "success".equals( result ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                String     amount        = requestMap.getOrDefault( "amount", "" ).toString();
                BigDecimal userPayAmount = new BigDecimal( amount );
                BigDecimal money         = userPayAmount.divide( BigDecimal.valueOf( 100 ), 2, RoundingMode.HALF_UP );
                memberRechargeOnline.setRealMoney( money );
                String orderNo = ( String ) requestMap.getOrDefault( "orderId", "" );
                memberRechargeOnline.setUpperOrderNo( orderNo );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
