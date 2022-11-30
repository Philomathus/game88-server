package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
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

@Repository( value = ConstantsPay.WANSHEN_PAY + "Processor" )
@Log4j2
public class WanShenPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "万申支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "version", "1.0" );
        params.put( "partnerid", payPlatform.getMerId() );
        params.put( "orderid", reqPayRecharge.getOrderNo() );
        params.put( "payamount", reqPayRecharge
                .getMoney()
                .multiply( BigDecimal.valueOf( 100 ) )
                .setScale( 0, RoundingMode.HALF_UP )
                .intValue() );
        params.put( "payip", reqPayRecharge.getRealIp() );
        params.put( "notifyurl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "returnurl", configEnvCacheUtil.getConf( "payReturnUrl" ) );
        params.put( "paytype", payChannel.getChannelCode() );

        String tempStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        String sign    = DigestUtils.md5Hex( tempStr ).toLowerCase();
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "0".equals( code ) ) {
                return ( String ) resultMap.get( "payurl" );
            } else {
                // 存档失败原因
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "version", "1.0" );
        params.put( "partnerid", payPlatform.getMerId() );
        params.put( "partnerorderid", memberRechargeOnline.getOrderNo() );

        String tempStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        String sign    = DigestUtils.md5Hex( tempStr ).toLowerCase();
        params.put( "sign", sign );
        log.warn( payPlatform.getName() + "查询订单请求参数:{}", JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "0".equals( code ) ) {
                String payamount = resultMap.getOrDefault( "payamount", "" ).toString();
                String status    = resultMap.getOrDefault( "orderstatus", "0" ).toString();
                if ( "1".equals( status ) || "4".equals( status ) ) {
                    BigDecimal amount = new BigDecimal( payamount ).divide( BigDecimal.valueOf( 100 ) );
                    memberRechargeOnline.setRealMoney( amount.setScale( 2, RoundingMode.HALF_UP ) );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String               merOrderNo           = requestMap.getOrDefault( "partnerorderid", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( merOrderNo );
        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", merOrderNo );
            return "success";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );
        String      sign        = ( String ) requestMap.remove( "sign" );
        requestMap.values().removeIf( value -> !StringUtils.hasText( value.toString() ) );

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

        SortedMap<String, Object> bodyMap  = new TreeMap<>( requestMap );
        String                    signTemp = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        signTemp = DigestUtils.md5Hex( signTemp ).toLowerCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + signTemp );
        if ( sign.equals( signTemp ) ) {
            String status = requestMap.getOrDefault( "orderstatus", "" ).toString();
            if ( ( "1".equals( status ) || "4".equals( status ) )
                    && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                String orderNo = requestMap.getOrDefault( "orderno", "" ).toString();
                memberRechargeOnline.setUpperOrderNo( orderNo );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
