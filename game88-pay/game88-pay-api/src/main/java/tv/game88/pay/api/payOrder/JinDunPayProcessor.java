package tv.game88.pay.api.payOrder;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.time.LocalDateTime;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Repository(value = ConstantsPay.JIN_DUN_PAY + "Processor")
@Log4j2
public class JinDunPayProcessor extends AbstractPay {
    @Override
    public String getName() {
        return "金盾支付";
    }

    @Override
    public String orderPay(PayChannel payChannel, PayPlatform payPlatform, ReqPayRecharge reqPayRecharge) {
        Map<String, Object> requestMap = new TreeMap<>();
        requestMap.put("pay_memberid", payPlatform.getMerId());
        requestMap.put("pay_orderid", reqPayRecharge.getOrderNo());
        requestMap.put("pay_applydate", LocalDateTimeUtils.format(LocalDateTime.now()));
        requestMap.put("pay_bankcode", payChannel.getChannelCode());
        requestMap.put("pay_notifyurl", configEnvCacheUtil.getConf("payCallbackUrl") + payPlatform.getCode());
        requestMap.put("pay_callbackurl", configEnvCacheUtil.getConf("payReturnUrl"));
        requestMap.put("pay_amount", reqPayRecharge.getMoney());
        String string = this.assemblyUrl(requestMap) + "&key=" + AESCoder.decrypt(payPlatform.getSignMd5());
        log.warn("Order: {}", string);
        String sign = DigestUtils.md5Hex(string).toUpperCase();
        requestMap.put("pay_md5sign", sign);
        requestMap.put("pay_productname", "product");

        String resultStr = this.sendPostString(payPlatform.getPayUrl(), packageForm(requestMap), reqPayRecharge);

        log.warn(payPlatform.getName()
                        + "下单结果:{},支付通道:{},订单号:{}", resultStr, payChannel.getChannelCode(),
                reqPayRecharge.getOrderNo());
        if (StringUtils.isNoneBlank(resultStr)) {
            return filterSpecialStr(resultStr);
        }
        return null;
    }

    @Override
    public boolean queryPay(MemberRechargeOnline memberRechargeOnline, PayPlatform payPlatform, PayChannel payChannel) {
        Map<String, Object> bodyMap = new TreeMap<>();
        bodyMap.put("pay_memberid", payPlatform.getMerId());
        bodyMap.put("pay_orderid", memberRechargeOnline.getOrderNo());
        String sign = this.assemblyUrl(bodyMap) + "&key=" + AESCoder.decrypt(payPlatform.getSignMd5());
        log.warn("Query: {}", sign);
        bodyMap.put("pay_md5sign", DigestUtils.md5Hex(sign).toUpperCase());

        Map<String, Object> resultMap = this.sendPostMap(payPlatform.getQueryUrl(), packageForm(bodyMap), null);

        log.warn(payPlatform.getName()
                + "查询结果 - orderNo:{};result:{}", memberRechargeOnline.getOrderNo(), JsonUtil.object2Json(resultMap));
        if (!CollectionUtils.isEmpty(resultMap)) {
            String state = resultMap.getOrDefault("trade_state", "").toString();
            String code = resultMap.getOrDefault("returncode", "").toString();
            if ("SUCCESS".equals(state) && "00".equals(code)) {
                BigDecimal amount = new BigDecimal(resultMap.getOrDefault("amount", 0).toString());
                memberRechargeOnline.setRealMoney(amount);
                return true;
            }
        }
        return false;
    }

    @Override
    public String callbackPay(Map<String, Object> requestMap, String realIp) {
        String merOrderNo = requestMap.getOrDefault("orderid", "").toString();
        MemberRechargeOnline memberRechargeOnline = memberRechargeOnlineMapper.selectById(merOrderNo);

        if (memberRechargeOnline.getStatus() == 1) {
            log.warn("订单已成功，无需继续回调 - orderNo:{}", merOrderNo);
            return "OK";
        }
        PayPlatform payPlatform = payCacheUtil.getPayPlatform(memberRechargeOnline.getPlatformId());
        PayChannel payChannel = payCacheUtil.getPayChannel(memberRechargeOnline.getChannelId());
        if (this.verifyIP(requestMap, realIp, payPlatform)) {
            return "FAIL";
        }
        if (this.diffPayTime12Hour(memberRechargeOnline.getPayTime(), merOrderNo)) {
            return "FAIL";
        }
        if (!payChannel.getCanCallback()) {
            log.warn("平台已拒绝三方支付通道回调 - 三方支付平台:{};三方支付编码:{};orderNo:{}", payPlatform.getName(), payChannel.getName(), merOrderNo);
            return "FAIL";
        }

        SortedMap<String, Object> treeMap = new TreeMap<>(requestMap);
        String sign = (String) treeMap.remove("sign");
        treeMap.remove("attach");
        String tempStr = this.assemblyUrl(treeMap) + "&key=" + AESCoder.decrypt(payPlatform.getSignMd5());
        log.warn("Callback: {}", tempStr);
        String mySign = DigestUtils.md5Hex(tempStr).toUpperCase();

        log.info(payPlatform.getName() + "回调签名字符串:" + sign + "_" + mySign);
        if (StringUtils.equals(sign, mySign)) {
            String status = requestMap.getOrDefault("returncode", "-1").toString();
            if (StringUtils.equals("00", status) && this.queryPay(memberRechargeOnline, payPlatform, payChannel)) {
                String trade_no = requestMap.getOrDefault("transaction_id", "").toString();
                memberRechargeOnline.setUpperOrderNo(trade_no);
                return payService.updatePayJourStatus(memberRechargeOnline, new String[]{"OK", "FAIL"}, payChannel.getName());
            }
        }
        log.info(payPlatform.getName() + "回调验签失败");
        return "FAIL";
    }
}
