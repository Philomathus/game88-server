package tv.game88.pay.api.payOrder;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

@Repository( value = ConstantsPay.QIANG_WEI_PAY + "Processor" )
@Log4j2
public class QiangWeiPayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "蔷薇支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) throws Exception {
        Map<String, Object> params = new TreeMap<>();
        params.put( "merchantno", payPlatform.getMerId() );
        params.put( "bankkey", payChannel.getChannelCode() );
        params.put( "orderno", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge.getMoney().setScale( 2, RoundingMode.HALF_UP ).toString() );
        params.put( "account", reqPayRecharge.getUserId() );

        String tempStr = this.assemblyUrl( params ) + "#" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Order: {}", tempStr );
        params.put( "sign", DigestUtils.md5Hex( tempStr ) );

        params.put( "format", "json" );
        params.put( "callbackurl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );

        log.warn( JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String              code    = resultMap.getOrDefault( "code", "" ).toString();
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( "1".equals( code ) && !CollectionUtils.isEmpty( dataMap ) ) {
                String payMode = dataMap.getOrDefault( "pay_mode", "" ).toString();
                if ( "print".equals( payMode ) ) {
                    reqPayRecharge.setUrlType( 1 );
                }
                String paySrc = dataMap.getOrDefault( "pay_src", "" ).toString();
                return new String( Base64.decodeBase64( paySrc ), StandardCharsets.UTF_8 );
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "info", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) throws Exception {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "merchantno", payPlatform.getMerId() );
        params.put( "orderno", memberRechargeOnline.getOrderNo() );
        params.put( "r", IdWorker.get32UUID() );

        String signStr = this.assemblyUrl( params ) + "#" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Query: {}, ", signStr );
        params.put( "sign", DigestUtils.md5Hex( signStr ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String              code    = resultMap.getOrDefault( "code", "" ).toString();
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
            if ( "1".equals( code ) && !CollectionUtils.isEmpty( dataMap ) ) {
                int status = Integer.parseInt( dataMap.getOrDefault( "state", -1 ).toString() );
                if ( status == 1 ) {
                    memberRechargeOnline.setRealMoney( new BigDecimal( dataMap.getOrDefault( "amount", 0 ).toString() ) );
                    memberRechargeOnline.setUpperOrderNo( dataMap.getOrDefault( "tradeno", "" ).toString() );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception {
        String               mchOrderNo           = requestMap.getOrDefault( "orderno", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( mchOrderNo );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", mchOrderNo );
            return "success";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), mchOrderNo ) ) {
            return "fail";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), mchOrderNo );
            return "fail";
        }

        String sign = requestMap.remove( "sign" ).toString();
        // 去除空值
        requestMap.entrySet().removeIf( me -> me.getValue() == null || StringUtils.isBlank( me.getValue().toString() ) );

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signStr = this.assemblyUrl( bodyMap ) + "#" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Callback: {}", signStr );
        String rel = DigestUtils.md5Hex( signStr );

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel );
        if ( rel.equalsIgnoreCase( sign ) ) {
            String status = requestMap.getOrDefault( "tradestatus", "" ).toString();
            if ( ( "success".equals( status ) ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
