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
import java.util.*;

@Repository( value = ConstantsPay.JINXIN_PAY + "Processor" )
@Log4j2
public class JinXinPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "金鑫支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> params = new HashMap<>();
        params.put( "mchId", Long.parseLong( payPlatform.getMerId() ) );
        params.put( "productId", Integer.parseInt( payChannel.getChannelCode() ) );
        params.put( "mchOrderNo", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge
                .getMoney()
                .multiply( new BigDecimal( 100 ) )
                .setScale( 0, RoundingMode.HALF_UP )
                .intValue() );
        params.put( "notifyUrl", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        if ( "8016".equals( payChannel.getChannelCode() ) ) {
            params.put( "param2", reqPayRecharge.getUserId() );
        }

        Object[] key = params.keySet().toArray();
        Arrays.sort( key );
        //生成加密原串
        StringBuilder res = new StringBuilder();
        for ( Object o : key ) {
            res.append( o ).append( "=" ).append( params.get( o ) ).append( "&" );
        }
        //再拼接秘钥
        String src = res.append( "key=" ).append( AESCoder.decrypt( payPlatform.getSignMd5() ) ).toString();
        //MD5加密并转为大写
        String sign = DigestUtils.md5Hex( src ).toUpperCase();
        params.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            if ( "SUCCESS".equals( resultMap.get( "retCode" ) ) && !resultMap.containsKey( "errDes" ) ) {
                Map urlsMap = ( Map ) resultMap.get( "payParams" );
                return urlsMap.get( "payUrl" ).toString();
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "errDes", "" ).toString() + "," + resultMap
                        .getOrDefault( "retMsg", "" )
                        .toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, Object> reqMap = new TreeMap<>();
        reqMap.put( "mchId", payPlatform.getMerId() );
        reqMap.put( "mchOrderNo", memberRechargeOnline.getOrderNo() );

        //对参数名按照ASCII升序排序
        Object[] key = reqMap.keySet().toArray();
        Arrays.sort( key );
        //生成加密原串
        StringBuilder res = new StringBuilder();
        for ( Object o : key ) {
            res.append( o ).append( "=" ).append( reqMap.get( o ) ).append( "&" );
        }
        //再拼接秘钥
        String src = res.append( "key=" ).append( AESCoder.decrypt( payPlatform.getSignMd5() ) ).toString();
        //MD5加密并转为大写
        String sign = DigestUtils.md5Hex( src ).toUpperCase();
        reqMap.put( "sign", sign );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( reqMap ), null );

        log.warn( "金鑫支付查询结果:{}", JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            int status = Integer.parseInt( resultMap.get( "status" ).toString() );
            if ( status == 2 || status == 3 ) {
                BigDecimal price = new BigDecimal( resultMap.getOrDefault( "amount", 0 ).toString() );
                memberRechargeOnline.setRealMoney( price.divide( BigDecimal.valueOf( 100 ), 2, RoundingMode.HALF_UP ) );
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        // 订单id
        String mchOrderNo = requestMap.getOrDefault( "mchOrderNo", "" ).toString();
        // 上游订单ID
        String transactionId = requestMap.getOrDefault( "payOrderId", "" ).toString();

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

        int    amount      = Integer.parseInt( requestMap.getOrDefault( "amount", "" ).toString() );
        int    status      = Integer.parseInt( requestMap.getOrDefault( "status", "" ).toString() );
        String mchId       = requestMap.getOrDefault( "mchId", "" ).toString();
        String sign        = requestMap.getOrDefault( "sign", "" ).toString();
        long   paySuccTime = Long.parseLong( requestMap.getOrDefault( "paySuccTime", "" ).toString() );
        int    productId   = Integer.parseInt( requestMap.getOrDefault( "productId", "" ).toString() );

        SortedMap<String, Object> signMap = new TreeMap<>();
        signMap.put( "mchId", mchId );
        signMap.put( "mchOrderNo", mchOrderNo );
        signMap.put( "amount", amount );
        signMap.put( "payOrderId", transactionId );
        signMap.put( "status", status );
        signMap.put( "paySuccTime", paySuccTime );
        signMap.put( "productId", productId );

        if ( "8016".equals( payChannel.getChannelCode() ) ) {
            signMap.put( "param2", requestMap.get( "param2" ) );
        }

        //对参数名按照ASCII升序排序
        Object[] key = signMap.keySet().toArray();
        Arrays.sort( key );
        //生成加密原串
        StringBuilder res = new StringBuilder();
        for ( Object o : key ) {
            res.append( o ).append( "=" ).append( signMap.get( o ) ).append( "&" );
        }
        //再拼接秘钥
        String src = res.append( "key=" ).append( AESCoder.decrypt( payPlatform.getSignMd5() ) ).toString();
        //MD5加密并转为大写
        String rel = DigestUtils.md5Hex( src ).toUpperCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel );
        if ( sign.equals( rel ) ) {
            if ( ( status == 2 || status == 3 ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                memberRechargeOnline.setRealMoney( new BigDecimal( amount ).divide( BigDecimal.valueOf( 100 ), 2,
                        RoundingMode.HALF_UP ) );
                memberRechargeOnline.setUpperOrderNo( transactionId );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
