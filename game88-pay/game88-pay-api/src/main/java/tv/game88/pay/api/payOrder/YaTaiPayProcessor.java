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
import java.util.Objects;
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
    public String orderPay(PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "mchId", payPlatform.getMerId() );
        params.put( "mchOrderNo", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge.getMoney().multiply( BigDecimal.valueOf( 100 ) )
                .setScale( 0, RoundingMode.HALF_UP ) );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );

        params.values().removeIf( v -> StringUtils.isBlank( Objects.toString(v, null) ) );

        String tempStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        params.put( "sign", DigestUtils.md5Hex( tempStr ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                        + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );

        if ( !CollectionUtils.isEmpty( resultMap ) && "SUCCESS".equals( resultMap.getOrDefault( "retCode", "" ).toString() ) ) {
            Map<String, Object> payParams = ( Map<String, Object> ) resultMap.get( "payParams" );
            if ( !CollectionUtils.isEmpty( payParams ) ) {
                return ( String ) payParams.get( "payUrl" );
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "retMsg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay(MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "mchId", payPlatform.getMerId() );
        params.put( "mchOrderNo", memberRechargeOnline.getOrderNo() );

        params.values().removeIf( v -> StringUtils.isBlank( Objects.toString(v, null) ) );

        String signStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Query: {}, ", signStr );
        params.put( "sign", DigestUtils.md5Hex( signStr ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( params ), null );

        if ( !CollectionUtils.isEmpty( resultMap ) && "SUCCESS".equals( resultMap.getOrDefault( "retCode", "" ).toString() )) {
            int status = Integer.parseInt( resultMap.getOrDefault( "status", -1 ).toString() );
            if ( status == 2 || status == 3 ) {
                BigDecimal amount = new BigDecimal( resultMap.getOrDefault( "amount", 0 ).toString() );
                memberRechargeOnline.setRealMoney( amount.divide( BigDecimal.valueOf( 100 ), 2, RoundingMode.HALF_UP ) );
                return true;
            }

        }
        return false;
    }

    @Override
    public String callbackPay(Map<String, Object> requestMap, String realIp) {
        String orderNum   = requestMap.getOrDefault( "mchOrderNo", "" ).toString();
        String payOrderId = requestMap.getOrDefault( "payOrderId", "" ).toString();
        int    status     = Integer.parseInt( requestMap.getOrDefault( "status", "0" ).toString() );

        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( orderNum );
        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", orderNum );
            return "success";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );
        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "fail";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), orderNum ) ) {
            return "fail";
        }
        if ( !payChannel.getCanCallback() ) {
            log.warn( "平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), orderNum );
            return "fail";
        }

        requestMap.values().removeIf( v -> StringUtils.isBlank( Objects.toString(v, null) ) );
        SortedMap<String, Object> treeMap = new TreeMap<>( requestMap );
        String                    sign    = ( String ) treeMap.remove( "sign" );

        String signMd5 = this.assemblyUrl( treeMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        signMd5 = DigestUtils.md5Hex( signMd5 ).toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + signMd5 );
        if ( sign.equals( signMd5 ) ) {
            if ( ( status == 2 || status == 3 ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                memberRechargeOnline.setUpperOrderNo( payOrderId );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
