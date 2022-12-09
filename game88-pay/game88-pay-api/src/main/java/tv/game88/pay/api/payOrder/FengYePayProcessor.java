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
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository(value = ConstantsPay.FENGYE_PAY + "Processor")
@Log4j2
public class FengYePayProcessor extends AbstractPay {

    @Override
    public String getName() {
        return "枫夜支付";
    }

    @Override
    public String orderPay(PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put("mchId", payPlatform.getMerId());
        params.put("productId", Integer.parseInt(payChannel.getChannelCode()));
        params.put("mchOrderNo", reqPayRecharge.getOrderNo());
        params.put("amount", reqPayRecharge.getMoney().setScale(0, RoundingMode.HALF_UP));
        params.put("notifyUrl", configEnvCacheUtil.getConf("payCallbackUrl") + payPlatform.getCode());
        params.put("returnUrl", configEnvCacheUtil.getConf("payReturnUrl"));
        createSignParam(params, payPlatform.getSignMd5());

        Map<String, Object> resultMap = this.sendPostMap(payPlatform.getPayUrl(), packageForm(params), reqPayRecharge);

        log.warn(payPlatform.getName()
                        + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json(resultMap), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo());
        if (!CollectionUtils.isEmpty(resultMap)) {
            if ("SUCCESS".equals(resultMap.get("retCode")) && !resultMap.containsKey("errDes")) {
                Map urlsMap = (Map) resultMap.get("payParams");
                return urlsMap.get("payUrl").toString();
            } else {
                reqPayRecharge.setFailReason(resultMap.getOrDefault("errDes", "").toString() + "," + resultMap
                        .getOrDefault("retMsg", "")
                        .toString());
            }
        }
        return null;
    }

    @Override
    public boolean queryPay(MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel) {
        Map<String, Object> params = new TreeMap<>();
        params.put("mchId", payPlatform.getMerId());
        params.put("mchOrderNo", memberRechargeOnline.getOrderNo());
        createSignParam(params, payPlatform.getSignMd5());

        Map<String, Object> resultMap = this.sendPostMap(payPlatform.getQueryUrl(), packageJson(params), null);

        log.warn("鸿运支付查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json(resultMap));
        if (!CollectionUtils.isEmpty(resultMap)) {
            if ("SUCCESS".equals(resultMap.getOrDefault("retCode", "FAIL").toString())) {
                int status = Integer.parseInt(resultMap.getOrDefault("status", "0").toString());
                return status == 2;
            }
        }
        return false;
    }

    @Override
    public String callbackPay(Map<String, Object> requestMap, String realIp) {
        String mchOrderNo = requestMap.getOrDefault( "mchOrderNo", "" ).toString();
        String transactionId = requestMap.getOrDefault( "payOrderId", "" ).toString();

        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById( mchOrderNo );

        if ( memberRechargeOnline.getStatus() == 2 ) {
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
                memberRechargeOnline.setRealMoney( new BigDecimal( amount ));
                memberRechargeOnline.setUpperOrderNo( transactionId );
                return payService.updatePayJourStatus( memberRechargeOnline, new String[] { "success", "fail" },
                        payChannel.getName() );
            }
        }
        log.info( payPlatform.getName() + "回调验签失败" );
        return "fail";
    }

    private void createSignParam(Map<String, Object> params, String md5) {
        String signStr = this.assemblyUrl(params) + "&key=" + AESCoder.decrypt(md5);
        log.warn(signStr);

        String sign = DigestUtils.md5Hex(signStr).toUpperCase();
        params.put("sign", sign);
    }
}
