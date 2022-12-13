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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.VIP_PAY + "Processor" )
@Log4j2
public class VipPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "vipPay";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> reqMap = new TreeMap<>();
        reqMap.put( "merchantNo", payPlatform.getMerId() );
        reqMap.put( "depositNo", reqPayRecharge.getOrderNo() );
        reqMap.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        reqMap.put( "amount", reqPayRecharge.getMoney().setScale( 0, RoundingMode.HALF_UP ).toString() );
        String signTemp = this.assemblyUrl( reqMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        String sign     = DigestUtils.md5Hex( signTemp ).toUpperCase();
        reqMap.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageJson( reqMap ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "status", "" ).toString();
            if ( "200".equals( code ) ) {
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( dataMap ) ) {
                    return ( String ) dataMap.get( "qrCodeUrl" );
                }
            } else {
                // 存档失败原因
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "merchantNo", payPlatform.getMerId() );
        params.put( "orderNo", memberRechargeOnline.getOrderNo() );

        String tempStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        String sign    = DigestUtils.md5Hex( tempStr ).toUpperCase();
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageJson( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "200".equals( code ) ) {
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "result", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( dataMap ) ) {
                    return "1".equals( dataMap.getOrDefault( "status", "" ).toString() );
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String               merOrderNo           = requestMap.getOrDefault( "depositNo", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( merOrderNo );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", merOrderNo );
            return "success";
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

        String sign    = ( String ) treeMap.remove( "sign" );
        String tempStr = this.assemblyUrl( treeMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        String mySign  = DigestUtils.md5Hex( tempStr ).toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + mySign );
        if ( StringUtils.equals( sign, mySign ) ) {
            String status = requestMap.getOrDefault( "status", "-1" ).toString();
            if ( StringUtils.equals( "1", status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                BigDecimal pay_amount = new BigDecimal( requestMap.getOrDefault( "amount", 0 ).toString() );
                memberRechargeOnline.setRealMoney( pay_amount.setScale( 2, RoundingMode.HALF_UP ) );
                String trade_no = requestMap.getOrDefault( "platformOrderId", "" ).toString();
                memberRechargeOnline.setUpperOrderNo( trade_no );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "FAIL" },
                        payPlatform.getName() + "-" + payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "FAIL";
    }
}
