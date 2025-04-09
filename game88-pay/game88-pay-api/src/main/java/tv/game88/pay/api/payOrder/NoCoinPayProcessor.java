package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.ServletUtil;
import tv.game88.pay.api.base.AbstractPay;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.constants.ConstantsPay;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayPlatform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.NO_COIN_PAY + "Processor" )
@Log4j2
public class NoCoinPayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "No数字货币支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> params                   = new TreeMap<>();
        BigDecimal          usdtWithdrawExchangeRate = configEnvCacheUtil.getConfBd( "usdt_withdraw_exchange_rate" );
        params.put( "appId", payPlatform.getMerId() );
        params.put( "merchantMemberNo", reqPayRecharge.getUserId() );
        params.put( "merchantOrderNo", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge.getMoney().divide( usdtWithdrawExchangeRate, 6, RoundingMode.HALF_DOWN ) );
        params.put( "rate", usdtWithdrawExchangeRate.stripTrailingZeros().toPlainString() );
        params.put( "language", "zh" );
        params.put( "coin", "USDT" );
        params.put( "protocol", "TRC20" );
        params.put( "rateType", 1 );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "timestamp", System.currentTimeMillis() );

        String signStr = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        log.warn( signStr );
        params.put( "sign", DigestUtils.sha256Hex( signStr ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set( "version", "V1" );
        httpHeaders.set( "appId", payPlatform.getMerId() );
        httpHeaders.set( "language", "zh_CN" );
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( params, httpHeaders );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), httpEntity, reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},订单号:{}", JsonUtil.object2Json( resultMap ), reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "0".equals( resultMap.getOrDefault( "code", "" ).toString() ) ) {
                Map urlsMap = ( Map ) resultMap.get( "data" );
                return urlsMap.get( "url" ).toString();
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        Map<String, Object> params = new TreeMap<>();
        params.put( "appId", payPlatform.getMerId() );
        params.put( "merchantOrderNo", memberRechargeOnline.getOrderNo() );
        params.put( "merchantMemberNo", memberRechargeOnline.getMemberId() );
        params.put( "timestamp", System.currentTimeMillis() );

        String signStr = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        log.warn( signStr );
        params.put( "sign", DigestUtils.sha256Hex( signStr ) );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set( "version", "V1" );
        httpHeaders.set( "appId", payPlatform.getMerId() );
        httpHeaders.set( "language", "zh_CN" );
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( params, httpHeaders );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), httpEntity, null );

        log.warn( payPlatform.getName() + "查询结果:{}", JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String              code    = resultMap.getOrDefault( "code", "" ).toString();
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            if ( "0".equals( code ) && !CollectionUtils.isEmpty( dataMap ) && "3".equals( dataMap.getOrDefault( "state", "" )
                    .toString() ) ) {
                memberRechargeOnline.setUpperOrderNo( dataMap.getOrDefault( "orderNo", 0 ).toString() );
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        // 订单id
        String mchOrderNo = requestMap.getOrDefault( "merchantOrderNo", "" ).toString();
        String state      = requestMap.getOrDefault( "state", "" ).toString();

        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( mchOrderNo );

        if ( "1".equals( memberRechargeOnline.getStatus() ) ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", mchOrderNo );
            return "SUCCESS";
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
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(),
                    mchOrderNo );
            return "fail";
        }
        String sign = requestMap.remove( "sign" ).toString();

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + payPlatform.getSignMd5();
        String rel     = DigestUtils.sha256Hex( signStr );

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel );
        if ( sign.equalsIgnoreCase( rel ) ) {
            if ( "3".equals( state ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                String rmbAmount = ServletUtil.getHttpServletRequest().getHeader( "rmbAmount" );
                memberRechargeOnline.setRealMoney( new BigDecimal( rmbAmount ) );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "SUCCESS", "fail" }, payChannel.getName() );
            } else {
                log.info( payPlatform.getName() + " :: " + mchOrderNo + " : 订单失败或超时" );
                return "SUCCESS";
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}

