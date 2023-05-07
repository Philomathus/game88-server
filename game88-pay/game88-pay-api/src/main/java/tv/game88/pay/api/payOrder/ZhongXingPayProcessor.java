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

@Log4j2
@Repository( value = ConstantsPay.ZHONG_XING_PAY + "Processor" )
public class ZhongXingPayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "中心支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "Amount", reqPayRecharge.getMoney().setScale( 2, RoundingMode.HALF_UP ) );
        params.put( "ClientRealName", "name" );
        params.put( "Ip", DigestUtils.md5Hex( reqPayRecharge.getUserId() ) );
        params.put( "MerchantId", payPlatform.getMerId() );
        params.put( "MerchantUniqueOrderId", reqPayRecharge.getOrderNo() );
        params.put( "NotifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "PayTypeId", payChannel.getChannelCode() );
        params.put( "PayTypeIdFormat", "URL" );
        params.put( "Remark", "remark" );

        String signStr = this.assemblyUrl( params ) + payPlatform.getSignMd5();
        log.warn( signStr );
        params.put( "Sign", DigestUtils.md5Hex( signStr ) );

        log.warn( JsonUtil.object2Json( params ) );
        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String Code = resultMap.getOrDefault( "Code", "" ).toString();
            if ( "0".equals( Code ) ) {
                return resultMap.getOrDefault( "Url", "" ).toString();
            } else {
                // 存档失败原因
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "MessageForSystem", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, Object> reqMap = new TreeMap<>();
        reqMap.put( "MerchantId", payPlatform.getMerId() );
        reqMap.put( "MerchantUniqueOrderId", memberRechargeOnline.getOrderNo() );

        String signStr = this.assemblyUrl( reqMap ) + payPlatform.getSignMd5();
        log.warn( signStr );
        reqMap.put( "Sign", DigestUtils.md5Hex( signStr ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( reqMap ), null );

        log.warn( payPlatform.getName() + "查询结果:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code           = resultMap.getOrDefault( "Code", "" ).toString();
            String payOrderStatus = resultMap.getOrDefault( "PayOrderStatus", "" ).toString();
            if ( "0".equals( code ) && "100".equals( payOrderStatus ) ) {
                BigDecimal RealAmount = new BigDecimal( resultMap.getOrDefault( "Amount", 0 ).toString() );
                memberRechargeOnline.setRealMoney( RealAmount.setScale( 2, RoundingMode.HALF_UP ) );
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        // 订单id
        String MerchantUniqueOrderId = requestMap.getOrDefault( "MerchantUniqueOrderId", "" ).toString();

        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( MerchantUniqueOrderId );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", MerchantUniqueOrderId );
            return "SUCCESS";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), MerchantUniqueOrderId ) ) {
            return "fail";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(),
                    MerchantUniqueOrderId );
            return "fail";
        }

        BigDecimal amount         = new BigDecimal( requestMap.getOrDefault( "Amount", 0 ).toString() );
        String     payOrderStatus = requestMap.getOrDefault( "PayOrderStatus", "" ).toString();
        String     sign           = requestMap.remove( "Sign" ).toString();

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signStr = this.assemblyUrl( bodyMap ) + payPlatform.getSignMd5();
        String rel     = DigestUtils.md5Hex( signStr );

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel );
        if ( sign.equalsIgnoreCase( rel ) ) {
            memberRechargeOnline.setUpperOrderNo( MerchantUniqueOrderId );
            if ( "100".equals( payOrderStatus ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                memberRechargeOnline.setRealMoney( amount.setScale( 2, RoundingMode.HALF_UP ) );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "SUCCESS", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
