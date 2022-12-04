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

@Repository( value = ConstantsPay.AS_PAY + "Processor" )
@Log4j2
public class ASPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "AS支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "pay_memberid", payPlatform.getMerId() );
        params.put( "pay_orderid", reqPayRecharge.getOrderNo() );
        params.put( "pay_applydate", LocalDateTimeUtils.format( LocalDateTime.now() ) );
        params.put( "pay_bankcode", payChannel.getChannelCode().trim() );
        params.put( "pay_notifyurl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "pay_callbackurl", configEnvCacheUtil.getConf( "payReturnUrl" ) );
        params.put( "pay_amount", reqPayRecharge.getMoney().setScale( 0, BigDecimal.ROUND_HALF_UP ).toString() );
        String sign = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        params.put( "pay_md5sign", DigestUtils.md5Hex( sign ).toUpperCase() );
        params.put( "pay_productname", "pay_productname" );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "1".equals( resultMap.getOrDefault( "status", "" ) ) ) {
                return resultMap.getOrDefault( "h5_url", "" ).toString();
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        String                    orderNo = memberRechargeOnline.getOrderNo();
        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "pay_memberid", payPlatform.getMerId() );
        bodyMap.put( "pay_orderid", orderNo );
        String sign = this.assemblyUrl( bodyMap ) + "&key=" + payPlatform.getSignMd5();
        bodyMap.put( "pay_md5sign", DigestUtils.md5Hex( sign ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( bodyMap ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( StringUtils.equals( "00", String.valueOf( resultMap.get( "returncode" ) ) )
                    && StringUtils.equals( "SUCCESS", String.valueOf( resultMap.get( "trade_state" ) ) ) ) {
                BigDecimal amount = new BigDecimal( resultMap.getOrDefault( "amount", "" ).toString() );
                memberRechargeOnline.setRealMoney( amount.setScale( 2, RoundingMode.HALF_UP ) );
                memberRechargeOnline.setUpperOrderNo( resultMap.getOrDefault( "transaction_id", "" ).toString() );
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String               merOrderNo           = requestMap.getOrDefault( "orderid", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( merOrderNo );
        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", merOrderNo );
            return "OK";
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
        String                    sign    = ( String ) treeMap.remove( "sign" );
        treeMap.remove( "attach" );
        treeMap.remove( "attcah" );
        String mySign = DigestUtils.md5Hex( this.assemblyUrl( treeMap ) + "&key=" + payPlatform.getSignMd5() ).toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + mySign );
        if ( StringUtils.equals( sign, mySign ) ) {
            String status = requestMap.getOrDefault( "returncode", "-1" ).toString();
            if ( StringUtils.equals( "00", status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "OK", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
