package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RSACoder;
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
import java.util.UUID;

@Repository(value = ConstantsPay.LAN_BO_PAY + "Processor")
@Log4j2
public class LanBoPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "兰博支付";
    }

    @Override
    @SuppressWarnings("unchecked")
    public String orderPay(PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge) {
        Map<String, Object> params = new TreeMap<>();
        params.put("merId", payPlatform.getMerId());
        params.put("orderId", reqPayRecharge.getOrderNo());
        params.put("orderAmt", reqPayRecharge.getMoney());
        params.put("channel", payChannel.getChannelCode());
        params.put("desc", "desc");
        params.put("attch", "attch");
        params.put("smstyle", "1");
        params.put("userId", reqPayRecharge.getUserId());
        params.put("ip", reqPayRecharge.getRealIp());
        params.put("notifyUrl", configEnvCacheUtil.getConf("payCallbackUrl") + payPlatform.getCode());
        params.put("returnUrl", configEnvCacheUtil.getConf("payReturnUrl"));
        params.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));

        String tempStr = this.assemblyUrl(params) + "&key=" + AESCoder.decrypt(payPlatform.getSignMd5());
        log.warn("Order: {}", tempStr);
        String signUppercase = DigestUtils.md5Hex(tempStr).toUpperCase();
        String sign = null;
        try {
            sign = RSACoder.signSha256Rsa(signUppercase, AESCoder.decrypt(payPlatform.getSignPrivateKey()));
        } catch (Exception e) {
            reqPayRecharge.setFailReason(e.getMessage());
            return null;
        }
        params.put("sign", sign);

        Map<String, Object> resultMap = this.sendPostMap(payPlatform.getPayUrl(), packageForm(params), reqPayRecharge);

        log.warn(payPlatform.getName()
                        + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json(resultMap), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo());
        if (!CollectionUtils.isEmpty(resultMap)) {
            String code = resultMap.getOrDefault("code", "").toString();
            if ("1".equals(code)) {
                Map<String, Object> payParams = (Map<String, Object>) resultMap.get("data");
                return payParams.get("payurl").toString();
            } else {
                reqPayRecharge.setFailReason(resultMap.getOrDefault("msg", "").toString());
            }
        }
        return null;
    }

    @Override
    public boolean queryPay(MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put("merId", payPlatform.getMerId());
        params.put("orderId", memberRechargeOnline.getOrderNo());
        params.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
        String signStr = this.assemblyUrl(params) + "&key=" + AESCoder.decrypt(payPlatform.getSignMd5());
        log.warn("Query: {}", signStr);
        signStr = DigestUtils.md5Hex(signStr).toUpperCase();
        try {
            signStr = RSACoder.signSha256Rsa(signStr, AESCoder.decrypt(payPlatform.getSignPrivateKey()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        params.put("sign", signStr);

        Map<String, Object> resultMap = this.sendPostMap(payPlatform.getQueryUrl(), packageForm(params), null);

        log.warn(payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json(resultMap));
        if (!CollectionUtils.isEmpty(resultMap)) {
            int status = Integer.parseInt(resultMap.getOrDefault("status", -1).toString());
            if (status == 1) {
                BigDecimal amount = new BigDecimal(resultMap.getOrDefault("amount", 0).toString());
                memberRechargeOnline.setRealMoney(amount.setScale(2, RoundingMode.HALF_UP));
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay(Map<String, Object> requestMap, String realIp) {
        String payOrderId = requestMap.getOrDefault("orderId", "").toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById(payOrderId);

        if (memberRechargeOnline.getStatus() == 1) {
            log.warn("订单已成功，无需继续回调 - orderNo:{}", payOrderId);
            return "success";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform(memberRechargeOnline.getPlatformId());
        PayChannel payChannel = payCacheUtil.getPayChannel(memberRechargeOnline.getChannelId());

        String sign = requestMap.remove("sign").toString();
        // 去除空值
        requestMap.entrySet().removeIf(me -> me.getValue() == null || StringUtils.isBlank(me.getValue().toString()));
        SortedMap<String, Object> bodyMap = new TreeMap<>(requestMap);
        String signStr = this.assemblyUrl(bodyMap) + "&key=" + AESCoder.decrypt(payPlatform.getSignMd5());
        log.warn("Callback: {}", signStr);
        String rel = DigestUtils.md5Hex(signStr).toUpperCase();

        log.info(payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel);
        try {
            if (!RSACoder.verifySha256Rsa(rel, AESCoder.decrypt(payPlatform.getSignPublicKey()), sign)) {
                log.warn("验签失败");
                return "fail";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (this.verifyIP(requestMap, realIp, payPlatform)) {
            return "fail";
        }
        if (this.diffPayTime12Hour(memberRechargeOnline.getPayTime(), payOrderId)) {
            return "fail";
        }
        if (!payChannel.getCanCallback()) {
            log.warn("平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), payOrderId);
            return "fail";
        }

        String status = requestMap.getOrDefault("status", 0).toString();
        if (("1".equals(status)) && this.queryPay(memberRechargeOnline, payPlatform, payChannel)) {
            memberRechargeOnline.setUpperOrderNo(requestMap.getOrDefault("sysOrderId", "").toString());
            return payService.updatePayJourStatus(memberRechargeOnline, new String[]{"success", "fail"},
                    payChannel.getName());
        }
        log.info(payPlatform.getName() + "回调验签失败");
        return "fail";
    }

}
