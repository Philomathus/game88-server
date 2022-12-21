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
import java.util.SortedMap;
import java.util.TreeMap;

@Repository(value = ConstantsPay.BAIFU_PAY + "Processor")
@Log4j2
public class BaiFuPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "佰富支付";
    }

    @Override
    @SuppressWarnings("unchecked")
    public String orderPay(PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge) {
        Map<String, Object> params = new TreeMap<>();
        params.put("mchId", payPlatform.getMerId());
        params.put("productId", Integer.parseInt(payChannel.getChannelCode()));
        params.put("mchOrderNo", reqPayRecharge.getOrderNo());
        params.put("amount", reqPayRecharge
                .getMoney()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toString());
        params.put("notifyUrl", configEnvCacheUtil.getConf("payCallbackUrl") + payPlatform.getCode());
        params.put("subject", "subject");
        params.put("body", "body");
        params.put("extra", "extra");

        String tempStr = this.assemblyUrl(params) + "&key=" + AESCoder.decrypt(payPlatform.getSignMd5());
        log.warn("Order: {}", tempStr);
        String sign = DigestUtils.md5Hex(tempStr).toUpperCase();
        params.put("sign", sign);

        Map<String, Object> resultMap = this.sendPostMap(payPlatform.getPayUrl(), packageForm(params), reqPayRecharge);

        log.warn(payPlatform.getName()
                        + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json(resultMap), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo());

        if (!CollectionUtils.isEmpty(resultMap)) {
            String code = resultMap.getOrDefault("retCode", "").toString();
            if ("SUCCESS".equals(code)) {
                Map<String, Object> payParams = (Map<String, Object>) resultMap.get("payParams");
                return payParams.get("payUrl").toString();
            } else {
                reqPayRecharge.setFailReason(resultMap.getOrDefault("retMsg", "").toString());
            }
        }
        return null;
    }

    @Override
    public boolean queryPay(MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put("mchId", payPlatform.getMerId());
        params.put("mchOrderNo", memberRechargeOnline.getOrderNo());

        String signStr = this.assemblyUrl(params) + "&key=" + AESCoder.decrypt(payPlatform.getSignMd5());
        log.warn("Query: {}", signStr);
        params.put("sign", DigestUtils.md5Hex(signStr).toUpperCase());

        Map<String, Object> resultMap = this.sendPostMap(payPlatform.getQueryUrl(), packageForm(params), null);

        log.warn(payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json(resultMap));
        if (!CollectionUtils.isEmpty(resultMap)) {
            if ("SUCCESS".equals(resultMap.getOrDefault("retCode", "").toString())) {
                int status = Integer.parseInt(resultMap.getOrDefault("status", -1).toString());
                if (status == 2 || status == 3) {
                    BigDecimal amount = new BigDecimal(resultMap.getOrDefault("amount", 0).toString());
                    memberRechargeOnline.setRealMoney(amount.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String callbackPay(Map<String, Object> requestMap, String realIp) {
        String mchOrderNo = requestMap.getOrDefault("mchOrderNo", "").toString();
        String payOrderId = requestMap.getOrDefault("payOrderId", "").toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById(mchOrderNo);

        if (memberRechargeOnline.getStatus() == 1) {
            log.warn("订单已成功，无需继续回调 - orderNo:{}", mchOrderNo);
            return "success";
        }

        PayPlatform payPlatform = payCacheUtil.getPayPlatform(memberRechargeOnline.getPlatformId());
        PayChannel payChannel = payCacheUtil.getPayChannel(memberRechargeOnline.getChannelId());

        if (this.verifyIP(requestMap, realIp, payPlatform)) {
            return "fail";
        }
        if (this.diffPayTime12Hour(memberRechargeOnline.getPayTime(), mchOrderNo)) {
            return "fail";
        }
        if (!payChannel.getCanCallback()) {
            log.warn("平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), mchOrderNo);
            return "fail";
        }

        String sign = requestMap.remove("sign").toString();
        // 去除空值
        requestMap.entrySet().removeIf(me -> me.getValue() == null || StringUtils.isBlank(me.getValue().toString()));

        SortedMap<String, Object> bodyMap = new TreeMap<>(requestMap);

        String signStr = this.assemblyUrl(bodyMap) + "&key=" + AESCoder.decrypt(payPlatform.getSignMd5());
        log.warn("Callback: {}", signStr);
        String rel = DigestUtils.md5Hex(signStr).toUpperCase();

        log.info(payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel);
        if (rel.equalsIgnoreCase(sign)) {
            String status = requestMap.getOrDefault("status", 0).toString();
            if (("2".equals(status) || "3".equals(status))
                    && this.queryPay(memberRechargeOnline, payPlatform, payChannel)) {
                memberRechargeOnline.setUpperOrderNo(payOrderId);
                return payService.updatePayJourStatus(memberRechargeOnline, new String[]{"success", "fail"},
                        payChannel.getName());
            }
        }
        log.info(payPlatform.getName() + "回调验签失败");
        return "fail";
    }
}
