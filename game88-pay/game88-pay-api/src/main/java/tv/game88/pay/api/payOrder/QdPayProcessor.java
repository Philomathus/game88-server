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
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository( value = ConstantsPay.QD_PAY + "Processor" )
@Log4j2
public class QdPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "QDPay";
    }

    @Override
     public String orderPay( PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) throws Exception {
        return null;
    }

    @Override
    public boolean queryPay( MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) throws Exception {
        Map<String, Object> params = new TreeMap<>();
        params.put( "merchantId", payPlatform.getMerId() );
        params.put( "orderNo", memberRechargeOnline.getOrderNo() );

        String tempStr = this.assemblyUrl( params ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        params.put( "sign", DigestUtils.md5Hex( tempStr ) );

        Map<String, Object> resultMap = this.sendPostMap(
                payPlatform.getPayUrl() + "/api/common/orderQuery", packageJson( params ), null );

        log.warn( payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json( resultMap ) );
        if ( !CollectionUtils.isEmpty( resultMap ) ) {
            String code = resultMap.getOrDefault( "code", "" ).toString();
            if ( "200".equals( code ) ) {
                Map<String, Object> dataMap = ( Map<String, Object> ) resultMap.getOrDefault( "data", new HashMap<>() );
                if ( !CollectionUtils.isEmpty( dataMap ) ) {
                    return "1".equals( dataMap.getOrDefault( "status", "" ).toString() );
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception {
        String               orderNo              = requestMap.getOrDefault( "orderNo", "" ).toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( orderNo );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", orderNo );
            return "success";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform( memberRechargeOnline.getPlatformId() );
        if ( this.verifyIP( requestMap, realIp, payPlatform ) ) {
            return "FAIL";
        }
        if ( this.diffPayTime12Hour( memberRechargeOnline.getPayTime(), orderNo ) ) {
            return "FAIL";
        }

        // 去除空值
        requestMap.entrySet().removeIf( me -> me.getValue() == null || StringUtils.isBlank( me.getValue().toString() ) );

        SortedMap<String, Object> treeMap = new TreeMap<>( requestMap );

        String sign    = ( String ) treeMap.remove( "sign" );
        String tempStr = this.assemblyUrl( treeMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        String mySign  = DigestUtils.md5Hex( tempStr );

        log.info( payPlatform.getName() + "回调签名字符串:" + sign + "_" + mySign );
        if ( StringUtils.equals( sign, mySign ) ) {
            String status = requestMap.getOrDefault( "status", "-1" ).toString();
            if ( StringUtils.equals( "1", status ) && this.queryPay( memberRechargeOnline, payPlatform, null ) ) {
                BigDecimal pay_amount = new BigDecimal( requestMap.getOrDefault( "amount", 0 ).toString() );
                memberRechargeOnline.setRealMoney( pay_amount.setScale( 2, RoundingMode.HALF_UP ) );
                String tradeNo = requestMap.getOrDefault( "tradeNo", "" ).toString();
                memberRechargeOnline.setUpperOrderNo( tradeNo );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "FAIL" },
                        payPlatform.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "FAIL";
    }
}
