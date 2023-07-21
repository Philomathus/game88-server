package tv.game88.pay.api.payOrder;


import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
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

@Repository(value = ConstantsPay.XIAO_MA_PAY + "Processor")
@Log4j2
public class XiaoMaPayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "小马支付";
    }

    @Override
    public String orderPay(PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge ) {
        Map<String, Object> params = new TreeMap<>();
        params.put("mchId",  payPlatform.getMerId() );
        params.put("productId", payChannel.getChannelCode() );
        params.put("mchOrderNo", reqPayRecharge.getOrderNo());
        params.put("amount", reqPayRecharge.getMoney().multiply( BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).intValue());

        params.put("notifyUrl", configEnvCacheUtil.getConf("payCallbackUrl") + payPlatform.getCode());

        String signStr = this.assemblyUrl( params ) + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( signStr );
        params.put( "Sign", DigestUtils.md5Hex( signStr ) );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getPayUrl(), packageForm( params ), reqPayRecharge );

        log.warn(payPlatform.getName() + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json(resultMap), payChannel.getChannelCode(), reqPayRecharge.getOrderNo());

        if (!CollectionUtils.isEmpty(resultMap)) {
            String retCode = resultMap.getOrDefault("retCode", "").toString();
            if ("SUCCESS".equals(retCode)) {
                Map<String, Object> payParamsMap = (Map<String, Object>) resultMap.getOrDefault("payParams", new HashMap<>() );
                return (String) payParamsMap.get("payUrl");
            } else {
                reqPayRecharge.setFailReason(resultMap.getOrDefault("retMsg", "").toString());
            }
        }
        return null;
    }

    @Override
    public boolean queryPay(  MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel ) {
        Map<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put("mchId", payPlatform.getMerId());
        bodyMap.put("mchOrderNo",memberRechargeOnline.getOrderNo() );

        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Query: {}, ", signStr );
        bodyMap.put( "sign", DigestUtils.md5Hex( signStr ).toUpperCase() );

        Map<String, Object> resultMap = this.sendPostMap( payPlatform.getQueryUrl(), packageForm( bodyMap ), null );

        log.warn(payPlatform.getName() + "查询结果 - orderNo:{};result:{}", payChannel.getChannelCode(), JsonUtil.object2Json(resultMap));

        if (!CollectionUtils.isEmpty(resultMap)) {
            String retCode = resultMap.getOrDefault("retCode", "").toString();
            if ( "SUCCESS".equals( retCode )) {
                int status = Integer.parseInt(resultMap.getOrDefault("status", "").toString());
                if (status == 2 || status == 3) {
                    BigDecimal amount = new BigDecimal(resultMap.getOrDefault("amount", 0).toString());
                    memberRechargeOnline.setRealMoney(amount.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                    memberRechargeOnline.setUpperOrderNo(resultMap.getOrDefault("productId", "").toString());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay( Map<String, Object> requestMap, String realIp ) {
        String payOrderId = requestMap.getOrDefault("payOrderId", "").toString();
        String orderNum = requestMap.getOrDefault("mchOrderNo", "").toString();
        String amount = requestMap.getOrDefault("amount", "").toString();

        int status = Integer.parseInt(requestMap.getOrDefault("status", "0").toString());

        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( orderNum );

        if ( memberRechargeOnline.getStatus() == 1 ) {
            log.warn( "订单已成功，无需继续回调 - orderNo:{}", orderNum );
            return "ok";
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

        String                    sign    = requestMap.remove( "sign" ).toString();
        SortedMap<String, Object> bodyMap = new TreeMap<>( requestMap );

        String signStr = this.assemblyUrl( bodyMap ) + "&key=" + AESCoder.decrypt( payPlatform.getSignMd5() );
        log.warn( "Callback: {}", signStr );
        String relKey = DigestUtils.md5Hex( signStr ).toUpperCase();

        log.info(payPlatform.getName() + "回调签名字符串:" + sign + "_" + relKey);

        if (sign.equals(relKey)) {
            if (status == 3 || status == 2) {
                if (this.queryPay(memberRechargeOnline, payPlatform, payChannel)) {
                    memberRechargeOnline.setRealMoney(new BigDecimal(amount).divide(BigDecimal.valueOf(100), 2,
                            RoundingMode.HALF_UP));
                    memberRechargeOnline.setUpperOrderNo(payOrderId);  // trans
                    return payService.updatePayJourStatus(memberRechargeOnline, new String[]{"success", "fail"}, payChannel.getName());
                }
            }

        }
        log.info( payPlatform.getName() + "回调验签失败");
        return "fail";
    }
}
