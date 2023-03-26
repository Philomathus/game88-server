package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.time.Instant;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.pay.api.base.AbstractPay;
import tv.game88.pay.api.constants.ConstantsPay;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayPlatform;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Repository( value = ConstantsPay.YONGXIN_PAY + "Processor" )
@Log4j2
public class YongXinPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "永信支付";
    }

    @Override
    public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put("mchId",payPlatform.getMerId());
        params.put("wayCode",payChannel.getChannelCode());
        params.put("subject","subject");
        params.put("outTradeNo",reqPayRecharge.getOrderNo());
        params.put("amount", reqPayRecharge.getMoney().multiply(BigDecimal.valueOf(100)).setScale(0,RoundingMode.HALF_UP).toString());
        params.put("clientIp",reqPayRecharge.getRealIp());
        params.put("notifyUrl",configEnvCacheUtil.getConf( "payCallbackUrl" ) + payPlatform.getCode());
        params.put("reqTime", System.currentTimeMillis());
        String sign = DigestUtils
                .md5Hex( this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() ) )
                .toLowerCase();
        params.put( "sign", sign );
        log.warn( JsonUtil.object2Json( params ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageJson( params ), reqPayRecharge );

        log.warn( payPlatform.getName()
                + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json( resultMap ), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo() );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            int code = ( int ) resultMap.get( "code" );
            if ( code == 0 ) {
                resultMap = ( Map<String, Object> ) resultMap.get( "data" );
                if ( !CollectionUtils.isEmpty( resultMap ) ) {
                    return ( String ) resultMap.get( "payUrl" );
                }
            } else {
                // 存档失败原因
                reqPayRecharge.setFailReason( resultMap.getOrDefault( "message", "" ).toString() );
            }
        }
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        Map<String, Object> params = new TreeMap<>();
        params.put("mchId",payPlatform.getMerId());
        params.put("outTradeNo",memberRechargeOnline.getOrderNo());
        params.put("reqTime", System.currentTimeMillis());
        String sign = DigestUtils
                .md5Hex( this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() ) )
                .toLowerCase();
        params.put( "sign", sign );
        log.warn( JsonUtil.object2Json( params ) );

        Map<String, Object> response = this.sendPostMap( payPlatform.getQueryUrl(), packageJson( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( response ) );
        if ( !CollectionUtils.isEmpty( response ) ) {
            int status = ( int ) response.getOrDefault( "code", -1 );
            if ( status == 0 ) {
                Map<String, Object> dataMap = ( Map<String, Object> ) response.getOrDefault( "data", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( response ) ) {
                    String trade_no = dataMap.getOrDefault( "tradeNo", "" ).toString();
                    String money    = dataMap.getOrDefault( "amount", "" ).toString();
                    status = ( int ) dataMap.getOrDefault( "state", 0 );
                    int notify = ( int ) dataMap.getOrDefault( "notifyState", -1 );
                    if ( notify == 0 && status == 2 ) {
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
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String               merOrderNo           = requestMap.getOrDefault( "outTradeNo", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( merOrderNo );
        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", merOrderNo );
            return "SUCCESS";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        PayChannel  payChannel  = payCacheUtil.getPayChannel( memberRechargeOnline.getChannelId() );
        String      sign        = ( String ) requestMap.remove( "sign" );
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

        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );
        String signTemp = DigestUtils
                .md5Hex( this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() ) )
                .toLowerCase();

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + signTemp );
        if ( sign.equalsIgnoreCase( signTemp ) ) {
            String status = requestMap.getOrDefault( "state", "" ).toString();
            if ( "1".equals( status ) && this.queryPay( memberRechargeOnline, payPlatform, payChannel ) ) {
                String userPayAmount = requestMap.getOrDefault( "amount", "" ).toString();
                memberRechargeOnline.setRealMoney( new BigDecimal( userPayAmount ).setScale( 2, BigDecimal.ROUND_HALF_UP ) );
                String orderNo = requestMap.getOrDefault( "tradeNo", "" ).toString();
                memberRechargeOnline.setUpperOrderNo( orderNo );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "SUCCESS", "FAIL" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "FAIL";
    }
}
