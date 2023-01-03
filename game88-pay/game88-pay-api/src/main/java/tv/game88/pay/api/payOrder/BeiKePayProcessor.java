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
import java.util.UUID;

@Repository(value = ConstantsPay.BEIKE_PAY + "Processor")
@Log4j2
public class BeiKePayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "贝壳支付";
    }

    @Override
    @SuppressWarnings("unchecked")
    public String orderPay(PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge) {
        Map<String, Object> params = new TreeMap<>();
        params.put("account_id", payPlatform.getMerId());
        params.put("thoroughfare", payChannel.getChannelCode());
        params.put("out_trade_no", reqPayRecharge.getOrderNo());
        params.put("amount", reqPayRecharge.getMoney().setScale(2, RoundingMode.HALF_UP));
        params.put("callback_url", configEnvCacheUtil.getConf("payCallbackUrl") + payPlatform.getCode());
        params.put("nonce_str", UUID.randomUUID().toString().replace("-", ""));
        params.put("content_type", "json_new");
        params.put("robin", "1");

        String tempStr = this.assemblyUrl(params) + "&key=" + AESCoder.decrypt(payPlatform.getSignMd5());
        log.warn("Order: {}", tempStr);
        String sign = DigestUtils.md5Hex(tempStr).toLowerCase();
        params.put("sign", sign);

        Map<String, Object> resultMap = this.sendPostMap(payPlatform.getPayUrl(), packageJson(params), reqPayRecharge);

        log.warn(payPlatform.getName()
                        + "下单结果:{},支付通道:{},订单号:{}", JsonUtil.object2Json(resultMap), payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo());

        if (!CollectionUtils.isEmpty(resultMap)) {
            String code = resultMap.getOrDefault("code", "").toString();
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
            if ("200".equals(code) && !CollectionUtils.isEmpty(dataMap)) {
                return resultMap.get("qr_str").toString();
            } else {
                reqPayRecharge.setFailReason(resultMap.getOrDefault("msg", "").toString());
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean queryPay(MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel) {
        SortedMap<String, Object> params = new TreeMap<>();
        params.put("acc_id", payPlatform.getMerId());
        params.put("out_trade_no", memberRechargeOnline.getOrderNo());
        params.put("nonce_str", UUID.randomUUID().toString().replace("-", ""));

        String signStr = this.assemblyUrl(params) + "&key=" + AESCoder.decrypt(payPlatform.getSignMd5());
        log.warn("Query: {}", signStr);
        params.put("sign", DigestUtils.md5Hex(signStr).toLowerCase());

        Map<String, Object> resultMap = this.sendPostMap(payPlatform.getQueryUrl(), packageJson(params), null);

        log.warn(payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json(resultMap));

        if (!CollectionUtils.isEmpty(resultMap)) {
            String code = resultMap.getOrDefault("code", "").toString();
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
            if ("200".equals(code) && !CollectionUtils.isEmpty(dataMap) &&
                    "1".equals(dataMap.getOrDefault("status", "-1"))) {
                BigDecimal amount = new BigDecimal(dataMap.getOrDefault("amount", 0).toString());
                memberRechargeOnline.setRealMoney(amount.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay(Map<String, Object> requestMap, String realIp) {
        String mchOrderNo = requestMap.getOrDefault("out_trade_no", "").toString();
        String payOrderId = requestMap.getOrDefault("trade_no", "").toString();
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
        String rel = DigestUtils.md5Hex(signStr).toLowerCase();

        log.info(payPlatform.getName() + "回调签名字符串:" + sign + "_" + rel);
        if (rel.equalsIgnoreCase(sign)) {
            String status = requestMap.getOrDefault("status", "").toString();
            if ("success".equals(status) && this.queryPay(memberRechargeOnline, payPlatform, payChannel)) {
                memberRechargeOnline.setUpperOrderNo(payOrderId);
                return payService.updatePayJourStatus(memberRechargeOnline, new String[]{"success", "fail"},
                        payChannel.getName());
            }
        }
        log.info(payPlatform.getName() + "回调验签失败");
        return "fail";
    }
}
