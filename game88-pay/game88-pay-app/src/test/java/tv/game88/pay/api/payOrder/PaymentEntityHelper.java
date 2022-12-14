package tv.game88.pay.api.payOrder;

import tv.game88.common.utils.AESCoder;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayPlatform;

import java.math.BigDecimal;
import java.time.LocalDateTime;

final public class PaymentEntityHelper {

    public static PayChannel createPayChannel() {
        PayChannel channel = new PayChannel();
        channel.setChannelCode("1");
        return channel;
    }

    public static PayPlatform createPayPlatform()  {
        PayPlatform platform = new PayPlatform();
        platform.setMerId("MER123");
        platform.setAppId("APP123");
        platform.setCode("CODE123");
        platform.setSignMd5(AESCoder.encrypt("test"));
        platform.setPayUrl("http://localhost:8080/pay");
        platform.setQueryUrl("http://localhost:8080/query");
        platform.setName("聚8支付");
        return platform;
    }

    public static ReqPayRecharge createReqPayRecharge() {
        ReqPayRecharge reqPayRecharge = new ReqPayRecharge();
        reqPayRecharge.setOrderNo("ORDER123");
        reqPayRecharge.setMoney(new BigDecimal("12345.678"));
        return reqPayRecharge;
    }

    public static MemberRechargeOnline createMemberRechargeOnline(Integer status) {
        MemberRechargeOnline memberRechargeOnline = new MemberRechargeOnline();
        memberRechargeOnline.setOrderNo("MEMBERORDER123");
        memberRechargeOnline.setPlatformId(1L);
        memberRechargeOnline.setChannelId(1L);
        memberRechargeOnline.setPayTime(LocalDateTime.now());
        memberRechargeOnline.setStatus(status);
        return memberRechargeOnline;
    }
}
