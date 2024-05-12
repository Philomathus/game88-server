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

@Repository( value = ConstantsPay.QIANYI_PAY + "Processor" )
@Log4j2
public class QianYiPayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "千亿支付";
    }

    @Override
     public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) throws Exception {
        Map<String, Object> params = new TreeMap<>();
        params.put( "mchNum", payPlatform.getMerId() );
        params.put( "payType", payChannel.getChannelCode() );
        params.put( "outOrderNum", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge.getMoney().setScale( 0, RoundingMode.HALF_UP ).toString() );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "timestamp", System.currentTimeMillis() + "" );

        String sign = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        sign = DigestUtils.md5Hex( sign ).toUpperCase();
        params.put( "sign", sign );
        log.warn( JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "200".equals( code ) ) {
                resultMap = ( Map<String, Object> ) resultMap.get( "attrData" );
                if ( !CollectionUtils.isEmpty( resultMap ) ) {
                    return ( String ) resultMap.get( "payUrl" );
                }
            } else {
                // 存档失败原因
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) throws Exception {
        Map<String, Object> params = new TreeMap<>();
        params.put( "mchNum", payPlatform.getMerId() );
        params.put( "outOrderNum", memberRechargeOnline.getOrderNo() );
        params.put( "timestamp", System.currentTimeMillis() + "" );

        String sign = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        sign = DigestUtils.md5Hex( sign ).toUpperCase();
        params.put( "sign", sign );
        log.warn( JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "200".equals( code ) ) {
                resultMap = ( Map<String, Object> ) resultMap.get( "attrData" );
                if ( !CollectionUtils.isEmpty( resultMap ) ) {
                    String trade_no = resultMap.getOrDefault( "outOrderNum", "" ).toString();
                    String money    = resultMap.getOrDefault( "amount", "" ).toString();
                    String status   = resultMap.getOrDefault( "status", "" ).toString();
                    if ( "success".equals( status ) ) {
                        BigDecimal amount = new BigDecimal( money );
                        memberRechargeOnline.setUpperOrderNo( trade_no );
                        memberRechargeOnline.setRealMoney( amount.setScale( 2, RoundingMode.HALF_UP ) );
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception {
        String               merOrderNo           = requestMap.getOrDefault( "outOrderNum", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( merOrderNo );
        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", merOrderNo );
            return "success";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        // 去除空值
        requestMap.entrySet().removeIf( me -> me.getValue() == null || StringUtils.isBlank( me.getValue().toString() ) );

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

        String sign = ( String ) requestMap.remove( "sign" );
        requestMap.remove( "errMsg" );

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signTemp = DigestUtils
                .md5Hex( this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() ) )
                .toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + signTemp );
        if ( sign.equalsIgnoreCase( signTemp ) ) {
            String status = requestMap.getOrDefault( "status", "" ).toString();
            if ( "success".equals( status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "FAIL" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "FAIL";
    }
}
