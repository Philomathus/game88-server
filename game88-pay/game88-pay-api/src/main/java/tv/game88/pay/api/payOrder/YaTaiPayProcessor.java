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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.YATAI_PAY + "Processor" )
@Log4j2
public class YaTaiPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "亚太支付";
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) throws Exception {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "mchId", payPlatform.getMerId() );
        params.put( "productId", payChannel.getChannelCode() );
        params.put( "mchOrderNo", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge
                .getMoney()
                .multiply( BigDecimal.valueOf( 100 ) )
                .setScale( 0, RoundingMode.HALF_UP ) );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "clientIp", reqPayRecharge.getRealIp() );

        String tempStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        params.put( "sign", DigestUtils.md5Hex( tempStr ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            Map<String, Object> payParams = ( Map<String, Object> ) resultMap.get( "payParams" );
            if ( "SUCCESS".equals( resultMap.getOrDefault( "retCode", "" ).toString() )
                    && !CollectionUtils.isEmpty( payParams ) ) {
                return ( String ) payParams.get( "payUrl" );
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "errDes", "" ).toString() + "," + resultMap
                        .getOrDefault( "retMsg", "" )
                        .toString() );
            }
        }
        return null;

    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) throws Exception {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "mchId", payPlatform.getMerId() );
        params.put( "mchOrderNo", memberRechargeOnline.getOrderNo() );

        String signStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Query: {}, ", signStr );
        params.put( "sign", DigestUtils.md5Hex( signStr ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( params ), null );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "SUCCESS".equals( resultMap.getOrDefault( "retCode", "" ).toString() ) ) {
                int status = Integer.parseInt( resultMap.getOrDefault( "status", -1 ).toString() );
                if ( status == 2 || status == 3 ) {
                    BigDecimal amount = new BigDecimal( resultMap.getOrDefault( "amount", 0 ).toString() );
                    memberRechargeOnline.setRealMoney( amount.divide( BigDecimal.valueOf( 100 ), 2, RoundingMode.HALF_UP ) );
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception {
        String               PayOrderId           = requestMap.getOrDefault( "mchOrderNo", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( PayOrderId );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - PayOrderId:{}", PayOrderId );
            return "success";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );

        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "non-success";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), PayOrderId ) ) {
            return "non-success";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), PayOrderId );
            return "non-success";
        }

        String                    sign    = requestMap.remove( "sign" ).toString();
        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Callback: {}", signStr );
        String rel = DigestUtils.md5Hex( signStr ).toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel );
        if ( rel.equalsIgnoreCase( sign ) ) {
            int status = Integer.parseInt( requestMap.getOrDefault( "status", -1 ).toString() );
            if ( ( status == 3 || status == 2 ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                memberRechargeOnline.setUpperOrderNo( requestMap.getOrDefault( "payOrderId", "" ).toString() );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "non-success" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "FAIL";
    }
}
