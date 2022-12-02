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

@Repository( value = ConstantsPay.ET_PAY + "Processor" )
@Log4j2
public class ETPayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "ET支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "mch_id", payPlatform.getMerId() );
        params.put( "ptype", payChannel.getChannelCode() );
        params.put( "order_sn", reqPayRecharge.getOrderNo() );
        params.put( "money", reqPayRecharge.getMoney().setScale( 2, RoundingMode.HALF_UP ).toString() );
        params.put( "format", "json" );
        params.put( "notify_url", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "time", System.currentTimeMillis() / 1000 );
        params.put( "goods_desc", "goods_desc" );
        params.put( "client_ip", reqPayRecharge.getRealIp() );

        String signStr = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        String sign    = DigestUtils.md5Hex( signStr );
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", -1 ).toString();
            if ( "1".equals( code ) ) {
                Map<String, Object> dataMap  = ( Map<String, Object> ) resultMap.get( "data" );
                String              order_sn = dataMap.getOrDefault( "order_sn", "" ).toString();
                return payPlatform.getPayUrl() + "&a=info&osn=" + order_sn;
            } else {
                // 存档失败原因
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "mch_id", payPlatform.getMerId() );
        params.put( "out_order_sn", memberRechargeOnline.getOrderNo() );
        params.put( "time", System.currentTimeMillis() / 1000 );

        String signStr = this.assemblyUrl( params ) + "&key=" + payPlatform.getSignMd5();
        String sign    = DigestUtils.md5Hex( signStr );
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( params ), null );

        log.warn( payPlatform.getName() + "查询结果:{}", JsonUtil.object2Json( resultMap ) );
        String code = resultMap.getOrDefault( "code", -1 ).toString();
        if ( "1".equals( code ) ) {
            Map<String, Object> map    = ( Map<String, Object> ) resultMap.get( "data" );
            String              status = map.getOrDefault( "status", -1 ).toString();
            if ( "9".equals( status ) ) {
                BigDecimal money = new BigDecimal( map.getOrDefault( "money", 0 ).toString() );
                if ( money.compareTo( BigDecimal.ZERO ) > 0 ) {
                    memberRechargeOnline.setRealMoney( money.setScale( 2, RoundingMode.HALF_UP ) );
                    memberRechargeOnline.setUpperOrderNo( map.getOrDefault( "order_sn", "" ).toString() );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        // 订单id
        String sh_order = requestMap.getOrDefault( "sh_order", "" ).toString();
        // 上游订单ID
        String pt_order = requestMap.getOrDefault( "pt_order", "" ).toString();

        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( sh_order );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", sh_order );
            return "success";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), sh_order ) ) {
            return "fail";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), sh_order );
            return "fail";
        }

        BigDecimal money  = new BigDecimal( requestMap.getOrDefault( "money", "" ).toString() );
        String     time   = requestMap.getOrDefault( "time", "" ).toString();
        String     status = requestMap.getOrDefault( "status", "" ).toString();
        String     sign   = requestMap.getOrDefault( "sign", "" ).toString();

        SortedMap<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put( "sh_order", sh_order );
        bodyMap.put( "pt_order", pt_order );
        bodyMap.put( "money", money );
        bodyMap.put( "time", time );
        bodyMap.put( "status", status );

        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + payPlatform.getSignMd5();
        log.warn( signStr );
        String rel = DigestUtils.md5Hex( signStr );
        log.warn( rel + ":" + sign );

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel );
        if ( rel.equalsIgnoreCase( sign ) ) {
            if ( "success".equals( status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                memberRechargeOnline.setRealMoney( money.setScale( 2, RoundingMode.HALF_UP ) );
                memberRechargeOnline.setUpperOrderNo( pt_order );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
