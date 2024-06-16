package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.GOPAY + "Processor" )
@Log4j2
public class GoPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "GoPay支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) throws Exception {
        SortedMap<String, String> reqMap = new TreeMap<>();
        reqMap.put( "recvid", payPlatform.getMerId() );
        reqMap.put( "orderid", reqPayRecharge.getOrderNo() );
        reqMap.put( "amount", reqPayRecharge.getMoney().setScale( 2, RoundingMode.HALF_UP ).toString() );
        reqMap.put( "notifyurl", URLEncoder.encode(
                configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode(), StandardCharsets.UTF_8 ) );

        String tempStr = reqMap.get( "recvid" ) + reqMap.get( "orderid" ) + reqMap.get( "amount" )
                + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( tempStr );
        reqMap.put( "sign", DigestUtils.md5Hex( tempStr ) );

        log.warn( payPlatform.getName() + "下单请求参数:{}", JsonUtil.object2Json( reqMap ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageJson( reqMap ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            int code = Integer.parseInt( resultMap.getOrDefault( "code", -1 ).toString() );
            if ( code == 1 ) {
                String              dataStr = resultMap.getOrDefault( "data", "" ).toString();
                String              data    = dataStr.replaceAll( "\\\\", "" );
                Map<String, String> dataMap = JsonUtil.json2Map( data );
                return dataMap.getOrDefault( "navurl", "" ).toString();
            } else {
                // 存档失败原因
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;


    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        Map<String, Object> resultMap = null;
        try {
            resultMap = restTemplate.getForObject(
                    payPlatform.getQueryUrl() + "?id=" + memberRechargeOnline.getUpperOrderNo(), Map.class );
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            int code = Integer.parseInt( resultMap.getOrDefault( "code", -1 ).toString() );
            if ( code == 1 ) {
                String              dataStr = resultMap.getOrDefault( "data", "" ).toString();
                String              data    = dataStr.replaceAll( "\\\\", "" );
                Map<String, String> dataMap = JsonUtil.json2Map( data );
                return "4".equals( dataMap.getOrDefault( "state", "" ) );
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception {
        // 订单id
        String orderid = requestMap.getOrDefault( "orderid", "" ).toString();

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

        String state   = requestMap.getOrDefault( "state", "" ).toString();
        String amount  = requestMap.getOrDefault( "amount", "" ).toString();
        String sign    = requestMap.getOrDefault( "sign", "" ).toString();
        String retsign = requestMap.getOrDefault( "retsign", "" ).toString();

        String ret = DigestUtils.md5Hex( sign + AESCoder.decrypt( payPlatform.getSignMd5() ) );

        log.info( payPlatform.getName() + "回调签名字符串:" + retsign + "_" + ret );
        if ( retsign.equalsIgnoreCase( ret ) ) {
            if ( "4".equals( state ) ) {
                memberRechargeOnline.setRealMoney( new BigDecimal( amount ).setScale( 2, RoundingMode.HALF_UP ) );
                memberRechargeOnline.setUpperOrderNo( requestMap.getOrDefault( "id", "" ).toString() );
                if ( this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                    return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                            payChannel.getName() );
                }
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
