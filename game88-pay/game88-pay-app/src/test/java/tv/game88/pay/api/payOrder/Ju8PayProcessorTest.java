package tv.game88.pay.api.payOrder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.AESCoder;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayPlatform;
import tv.game88.pay.api.mapper.MemberRechargeOnlineMapper;
import tv.game88.pay.api.service.PayService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tv.game88.pay.api.payOrder.PaymentEntityHelper.createMemberRechargeOnline;
import static tv.game88.pay.api.payOrder.PaymentEntityHelper.createPayChannel;
import static tv.game88.pay.api.payOrder.PaymentEntityHelper.createPayPlatform;
import static tv.game88.pay.api.payOrder.PaymentEntityHelper.createReqPayRecharge;

class Ju8PayProcessorTest {

    private static MockJu8PayProcessor mockJu8PayProcessor;
    private static final MemberRechargeOnlineMapper memberRechargeOnlineMapper = mock(MemberRechargeOnlineMapper.class);
    private static final PayService payService = mock(PayService.class);
    private static final RestTemplate restTemplate = mock(RestTemplate.class);
    private static final ConfigEnvCacheUtil configEnvCacheUtil = mock(ConfigEnvCacheUtil.class);
    private static final PayCacheUtil payCacheUtil = mock(PayCacheUtil.class);

    private static class MockJu8PayProcessor extends Ju8PayProcessor {
        public MockJu8PayProcessor(MemberRechargeOnlineMapper memberRechargeOnlineMapper, PayService payService,
                                   RestTemplate restTemplate, ConfigEnvCacheUtil configEnvCacheUtil,
                                   PayCacheUtil payCacheUtil) {
            super.memberRechargeOnlineMapper = memberRechargeOnlineMapper;
            super.payService = payService;
            super.restTemplate = restTemplate;
            super.configEnvCacheUtil = configEnvCacheUtil;
            super.payCacheUtil = payCacheUtil;
        }
    }

    @BeforeEach
    void setup() {
        mockJu8PayProcessor = new MockJu8PayProcessor(memberRechargeOnlineMapper, payService, restTemplate,
                configEnvCacheUtil, payCacheUtil);
    }

    @Test
    void getName() {
        assertEquals(mockJu8PayProcessor.getName(), "聚8支付");
    }

    @Test
    void orderPaySuccess() {
        when(restTemplate.execute(anyString(), any(HttpMethod.class), any(), any()))
                .thenReturn(Map.of("retCode", "0", "payParams",
                        Map.of("payUrl", "http://localhost:8080/success")));
        String result = mockJu8PayProcessor.orderPay(createPayChannel(), createPayPlatform(), createReqPayRecharge());
        //return success pay url
        assertEquals("http://localhost:8080/success", result);
    }

    @Test
    void orderPayFail() {
        when(restTemplate.execute(anyString(), any(HttpMethod.class), any(), any()))
                .thenReturn(Map.of("retCode", "0100", "retMsg", "商户签名异常"));
        ReqPayRecharge reqPayRecharge = createReqPayRecharge();
        mockJu8PayProcessor.orderPay(createPayChannel(), createPayPlatform(), reqPayRecharge);
        //fail reason has value
        assertEquals("商户签名异常", reqPayRecharge.getFailReason());
    }

    @Test
    void queryPaySuccess() {
        when(restTemplate.execute(anyString(), any(HttpMethod.class), any(), any()))
                .thenReturn(Map.of("retCode", "0", "amount", "12345.98"));
        MemberRechargeOnline memberRechargeOnline = createMemberRechargeOnline(1);
        mockJu8PayProcessor.queryPay(memberRechargeOnline, createPayPlatform(), createPayChannel());
        //set real money
        assertEquals(new BigDecimal("123.46"), memberRechargeOnline.getRealMoney());
    }

    @Test
    void queryPayFail() {
        when(restTemplate.execute(anyString(), any(HttpMethod.class), any(), any()))
                .thenReturn(Map.of("retCode", "0011"));
        MemberRechargeOnline memberRechargeOnline = createMemberRechargeOnline(2);
        mockJu8PayProcessor.queryPay(memberRechargeOnline, createPayPlatform(), createPayChannel());
        //no real money set
        assertNull(memberRechargeOnline.getRealMoney());
    }

    @Test
    void callbackPaySuccess() {
        Map<String, Object> requestMap = new HashMap<>(Map.of("mchOrderNo", "MERCH1", "payOrderId", "ORDER123",
                "sign", "C98F6E51E7C384CBCF2CFC8FED60D6E4", "status", "2"));
        PayPlatform platform = createPayPlatform();
        platform.setWhiteIp("192.168.0.1,127.0.0.1");
        PayChannel payChannel = createPayChannel();
        payChannel.setCanCallback(true);
        MemberRechargeOnline memberRechargeOnline = createMemberRechargeOnline(2);

        when(memberRechargeOnlineMapper.selectById(anyString())).thenReturn(memberRechargeOnline);
        when(payCacheUtil.getPayPlatform(anyLong())).thenReturn(platform);
        when(payCacheUtil.getPayChannel(anyLong())).thenReturn(payChannel);
        when(restTemplate.execute(anyString(), any(HttpMethod.class), any(), any()))
                .thenReturn(Map.of("retCode", "0", "amount", "12345.98"));
        when(payService.updatePayJourStatus(any(), any(), any())).thenReturn("success");

        String result = mockJu8PayProcessor.callbackPay(requestMap, "127.0.0.1");
        //"success" String must return
        assertEquals("success", result);
        //upperOrderNo must be equal to payOrderId
        assertEquals("ORDER123", memberRechargeOnline.getUpperOrderNo());
    }

    @Test
    void callbackPaySuccessStatusIs1() {
        Map<String, Object> requestMap = new HashMap<>(Map.of("mchOrderNo", "MERCH1", "payOrderId", "ORDER123",
                "sign", "C98F6E51E7C384CBCF2CFC8FED60D6E4", "status", "2"));

        when(memberRechargeOnlineMapper.selectById(anyString())).thenReturn(createMemberRechargeOnline(1));

        String result = mockJu8PayProcessor.callbackPay(requestMap, "127.0.0.1");
        //memberRechargeOnline.getStatus() == 1
        assertEquals("success", result);
    }

    @Test
    void callbackPayFailInvalidMd5Sign() {
        Map<String, Object> requestMap = new HashMap<>(Map.of("mchOrderNo", "MERCH1", "payOrderId", "ORDER123",
                "sign", Objects.requireNonNull(AESCoder.encrypt("test")), "status", "2"));
        PayPlatform platform = createPayPlatform();
        platform.setWhiteIp("192.168.0.1,127.0.0.1");
        PayChannel payChannel = createPayChannel();
        payChannel.setCanCallback(true);
        MemberRechargeOnline memberRechargeOnline = createMemberRechargeOnline(2);

        when(memberRechargeOnlineMapper.selectById(anyString())).thenReturn(memberRechargeOnline);
        when(payCacheUtil.getPayPlatform(anyLong())).thenReturn(platform);
        when(payCacheUtil.getPayChannel(anyLong())).thenReturn(payChannel);
        when(restTemplate.execute(anyString(), any(HttpMethod.class), any(), any()))
                .thenReturn(Map.of("retCode", "0", "amount", "12345.98"));
        when(payService.updatePayJourStatus(any(), any(), any())).thenReturn("success");

        String result = mockJu8PayProcessor.callbackPay(requestMap, "127.0.0.1");
        //md5 does not match
        assertEquals("fail", result);
    }

    @Test
    void callbackPayFailInvalidIp() {
        Map<String, Object> requestMap = new HashMap<>(Map.of("mchOrderNo", "MERCH1", "payOrderId", "ORDER123",
                "sign", "C98F6E51E7C384CBCF2CFC8FED60D6E4", "status", "2"));
        PayPlatform platform = createPayPlatform();
        platform.setWhiteIp("192.168.0.1,127.0.0.1");
        PayChannel payChannel = createPayChannel();
        payChannel.setCanCallback(true);

        when(memberRechargeOnlineMapper.selectById(anyString())).thenReturn(createMemberRechargeOnline(2));
        when(payCacheUtil.getPayPlatform(1L)).thenReturn(platform);
        when(payCacheUtil.getPayChannel(1L)).thenReturn(payChannel);

        String result = mockJu8PayProcessor.callbackPay(requestMap, "127.168.2.1");
        //invalid IP
        assertEquals("fail", result);
    }

    @Test
    void callbackPayFailInvalidPayTime() {
        Map<String, Object> requestMap = new HashMap<>(Map.of("mchOrderNo", "MERCH1", "payOrderId", "ORDER123",
                "sign", "C98F6E51E7C384CBCF2CFC8FED60D6E4", "status", "2"));
        PayPlatform platform = createPayPlatform();
        platform.setWhiteIp("192.168.0.1,127.0.0.1");
        PayChannel payChannel = createPayChannel();
        payChannel.setCanCallback(true);
        MemberRechargeOnline memberRechargeOnline = createMemberRechargeOnline(2);
        memberRechargeOnline.setPayTime(LocalDateTime.now().minusHours(49));

        when(memberRechargeOnlineMapper.selectById(anyString())).thenReturn(memberRechargeOnline);
        when(payCacheUtil.getPayPlatform(1L)).thenReturn(platform);
        when(payCacheUtil.getPayChannel(1L)).thenReturn(payChannel);

        String result = mockJu8PayProcessor.callbackPay(requestMap, "127.0.0.1");
        //more than 48 hours callback pay
        assertEquals("fail", result);
    }

    @Test
    void callbackPayFailCannotCallback() {
        Map<String, Object> requestMap = new HashMap<>(Map.of("mchOrderNo", "MERCH1", "payOrderId", "ORDER123",
                "sign", "C98F6E51E7C384CBCF2CFC8FED60D6E4", "status", "2"));
        PayPlatform platform = createPayPlatform();
        platform.setWhiteIp("192.168.0.1,127.0.0.1");
        PayChannel payChannel = createPayChannel();
        payChannel.setCanCallback(false);

        when(memberRechargeOnlineMapper.selectById(anyString())).thenReturn(createMemberRechargeOnline(2));
        when(payCacheUtil.getPayPlatform(1L)).thenReturn(platform);
        when(payCacheUtil.getPayChannel(1L)).thenReturn(payChannel);

        String result = mockJu8PayProcessor.callbackPay(requestMap, "127.0.0.1");
        //canCallBack set to false
        assertEquals("fail", result);
    }

}