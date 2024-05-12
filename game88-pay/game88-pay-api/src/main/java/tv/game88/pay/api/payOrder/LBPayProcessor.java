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

@Repository( value = ConstantsPay.LB_PAY + "Processor" )
@Log4j2
public class LBPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "LB支付";
    }

    @Override
     public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) throws Exception {
        Map<String, Object> reqMap = new TreeMap<>();
        reqMap.put( "pay_memberid", payPlatform.getMerId() );
        reqMap.put( "pay_orderid", reqPayRecharge.getOrderNo() );
        reqMap.put( "pay_applydate", LocalDateTimeUtils.format( LocalDateTime.now() ) );
        reqMap.put( "pay_bankcode", payChannel.getChannelCode() );
        reqMap.put( "pay_notifyurl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        reqMap.put( "pay_callbackurl", configEnvCacheUtil.getConf( "payReturnUrl" ) );
        reqMap.put( "pay_amount", reqPayRecharge.getMoney().setScale( 0, RoundingMode.HALF_UP ).toString() );
        String signTemp = this.assemblyUrl( reqMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        String sign     = DigestUtils.md5Hex( signTemp ).toUpperCase();
        reqMap.put( "pay_md5sign", sign );
        reqMap.put( "pay_productname", "pay_productname" );
        reqMap.put( "pay_post", "json" );
        reqMap.put( "pay_ip", reqPayRecharge.getRealIp() );

        String result = this.sendPostString( payPlatform.getPayUrl(), packageForm( reqMap ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", result, payChannel.getChannelCode(), reqPayRecharge.getOrderNo() );
        if ( StringUtils.isNotBlank( result ) ) {
            return filterSpecialStr( result );
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) throws Exception {
        Map<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "pay_memberid", payPlatform.getMerId() );
        bodyMap.put( "pay_orderid", memberRechargeOnline.getOrderNo() );
        String sign = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        bodyMap.put( "pay_md5sign", DigestUtils.md5Hex( sign ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( bodyMap ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String state = resultMap.getOrDefault( "trade_state", "" ).toString();
            String code  = resultMap.getOrDefault( "returncode", "" ).toString();
            if ( "SUCCESS".equals( state ) && "00".equals( code ) ) {
                String     amount     = resultMap.getOrDefault( "amount", 0 ).toString();
                BigDecimal pay_amount = new BigDecimal( amount );
                memberRechargeOnline.setRealMoney( pay_amount.setScale( 2, RoundingMode.HALF_UP ) );
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception {
        String               merOrderNo           = requestMap.getOrDefault( "orderid", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( merOrderNo );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", merOrderNo );
            return "OK";
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

        SortedMap<String, Object> treeMap = new TreeMap<>( requestMap );
        String                    sign    = ( String ) treeMap.remove( "sign" );
        treeMap.remove( "attach" );
        String tempStr = this.assemblyUrl( treeMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        String mySign  = DigestUtils.md5Hex( tempStr ).toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + mySign );
        if ( StringUtils.equals( sign, mySign ) ) {
            String status = requestMap.getOrDefault( "returncode", "-1" ).toString();
            if ( StringUtils.equals( "00", status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                BigDecimal pay_amount = new BigDecimal( requestMap.getOrDefault( "amount", 0 ).toString() );
                memberRechargeOnline.setRealMoney( pay_amount.setScale( 2, RoundingMode.HALF_UP ) );
                String trade_no = requestMap.getOrDefault( "transaction_id", "" ).toString();
                memberRechargeOnline.setUpperOrderNo( trade_no );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "OK", "FAIL" },
                        payPlatform.getName() + "-" + payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "FAIL";
    }
}
