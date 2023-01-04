package tv.game88.pay.api.payOrder;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
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

@Repository( value = ConstantsPay.BEIKE_PAY + "Processor" )
@Log4j2
public class BeiKePayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "贝壳支付";
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> params = new HashMap<>();
        params.put( "account_id", payPlatform.getMerId() );
        params.put( "thoroughfare", payChannel.getChannelCode() );
        params.put( "out_trade_no", reqPayRecharge.getOrderNo() );
        params.put( "amount", reqPayRecharge.getMoney().setScale( 2, RoundingMode.HALF_UP ) );
        params.put( "callback_url", configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode() );
        params.put( "nonce_str", IdWorker.get32UUID() );
        params.put( "content_type", "json_new" );
        params.put( "robin", "1" );
        params.put( "success_url", "xx" );
        params.put( "error_url", "xx" );

        String s       = params.get( "amount" ).toString() + params.get( "out_trade_no" ).toString();
        String tempStr = AESCoder.decrypt( payPlatform.getSignMd5() ).toLowerCase() + DigestUtils.md5Hex( s );
        params.put( "sign", DigestUtils.md5Hex( tempStr ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String                    code        = resultMap.getOrDefault( "code", "" ).toString();
            List<Map<String, Object>> dataMapList = ( List<Map<String, Object>> ) resultMap.get( "data" );
            if ( "200".equals( code ) && !CollectionUtils.isEmpty( dataMapList ) ) {
                Map<String, Object> dataMap = dataMapList.get( 0 );
                return dataMap.get( "jump_url" ).toString();
            } else {
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "msg", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put( "acc_id", payPlatform.getMerId() );
        params.put( "out_trade_no", memberRechargeOnline.getOrderNo() );
        params.put( "nonce_str", IdWorker.get32UUID() );
        params.put( "amount", memberRechargeOnline.getMoney() );

        String tempStr = AESCoder.decrypt( payPlatform.getSignMd5() ).toLowerCase() + DigestUtils.md5Hex(
                params.get( "amount" ).toString() + params.get( "out_trade_no" ) );
        log.warn( "Query: {}", tempStr );
        params.put( "sign", DigestUtils.md5Hex( tempStr ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageJson( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );

        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String              code    = resultMap.getOrDefault( "code", "" ).toString();
            Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.get( "data" );
            if ( "200".equals( code ) && !CollectionUtils.isEmpty( dataMap )
                    && "1".equals( dataMap.getOrDefault( "status", "-1" ) ) ) {
                memberRechargeOnline.setUpperOrderNo( dataMap.getOrDefault( "trade_no", "" ).toString() );
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String               mchOrderNo           = requestMap.getOrDefault( "out_trade_no", "" ).toString();
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

        String signStr = AESCoder.decrypt( payPlatform.getSignMd5() ).toLowerCase() + DigestUtils.md5Hex(
                requestMap.get( "amount" ).toString() + requestMap.get( "out_trade_no" ) );
        log.warn( "Callback: {}", signStr );
        String rel = DigestUtils.md5Hex( signStr );

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel );
        if ( rel.equalsIgnoreCase( sign ) ) {
            String status = requestMap.getOrDefault( "status", "" ).toString();
            if ( "success".equals( status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                BigDecimal amount = new BigDecimal( requestMap.getOrDefault( "amount", 0 ).toString() );
                memberRechargeOnline.setRealMoney( amount.divide( BigDecimal.valueOf( 100 ), 2, RoundingMode.HALF_UP ) );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }
}
