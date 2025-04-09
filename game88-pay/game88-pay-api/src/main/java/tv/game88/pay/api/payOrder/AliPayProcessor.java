package tv.game88.pay.api.payOrder;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import java.util.*;

@Repository( value = ConstantsPay.ALI_PAY + "Processor" )
@Log4j2
public class AliPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "支付宝钱包支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        SortedMap<String, Object> payMap = new TreeMap<>();
        payMap.put( "busiAmount", reqPayRecharge.getMoney().setScale( 2, RoundingMode.DOWN ) );
        payMap.put( "reqId", reqPayRecharge.getOrderNo() );
        payMap.put( "callbackUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );

        log.warn( JsonUtil.object2Json( payMap ) );

        String params = null;
        try {
            String signMd5       = payPlatform.getSignMd5();
            String signPublicKey = payPlatform.getSignPublicKey();
            params = AESCoder.encryptBase64ByKeyIv( JsonUtil.object2Json( payMap ), signMd5, signPublicKey );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "pay", params );
        requestMap.put( "dc", payPlatform.getMerId() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.set( "TraceId", reqPayRecharge.getUserId() );
        httpHeaders.set( "uuid", IdWorker.get32UUID() );
        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>( requestMap, httpHeaders );

        log.warn( JsonUtil.object2Json( httpEntity ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), httpEntity, reqPayRecharge );
        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "C2".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
                Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", Collections.emptyMap() );
                return result.getOrDefault( "payUrl", "" ).toString();
            } else {
                reqPayRecharge.setFailReason( JsonUtil.object2Json( resultMap ) );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, String> payMap = new TreeMap<>();
        payMap.put( "reqId", memberRechargeOnline.getOrderNo() );

        String params = null;
        try {
            params = AESCoder.encryptBase64ByKeyIv( JsonUtil.object2Json( payMap ), payPlatform.getSignMd5(),
                    payPlatform.getSignPublicKey() );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put( "pay", params );
        requestMap.put( "dc", payPlatform.getMerId() );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.set( "TraceId", memberRechargeOnline.getMemberId() );
        httpHeaders.set( "uuid", IdWorker.get32UUID() );
        HttpEntity<String> httpEntity = new HttpEntity<>( JsonUtil.object2Json( requestMap ), httpHeaders );

        log.warn( JsonUtil.object2Json( httpEntity ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), httpEntity, null );

        log.warn( payPlatform.getName() + "查询:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "C2".equals( resultMap.getOrDefault( "code", "-1" ).toString() ) ) {
                Map<String, Object> result = ( Map<String, Object> ) resultMap.getOrDefault( "result", Collections.emptyMap() );
                return "1".equals( result.getOrDefault( "status", -1 ).toString() );
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        //订单号
        String orderNo = requestMap.get( "reqId" ).toString();
        String payId   = requestMap.get( "payId" ).toString();
        String result  = requestMap.get( "result" ).toString();

        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( orderNo );
        if ( 1 == memberRechargeOnline.getStatus() ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", orderNo );
            return "success";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), orderNo ) ) {
            return "fail";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(),
                    orderNo );
            return "fail";
        }

        try {
            String resultJson = AESCoder.decryptBase64ByKeyIv( result, payPlatform.getSignMd5(),
                    payPlatform.getSignPublicKey() );
            log.warn( resultJson );
            Map<String, Object> resultMap = JsonUtil.json2Map( resultJson );
            if ( !CollectionUtils.isEmpty( resultMap ) ) {
                String status = resultMap.getOrDefault( "status", "" ).toString();
                String amount = resultMap.getOrDefault( "amount", "0" ).toString();
                if ( "1".equals( status ) ) {
                    memberRechargeOnline.setRealMoney( new BigDecimal( amount ) );
                    memberRechargeOnline.setUpperOrderNo( payId );
                    return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" }, payChannel.getName() );
                }
            }
            return "fail";
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }


}
