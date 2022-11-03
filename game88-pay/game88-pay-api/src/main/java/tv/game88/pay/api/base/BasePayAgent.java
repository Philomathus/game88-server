package tv.game88.pay.api.base;

import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;

import java.util.Map;

public interface BasePayAgent {

    String getName();

    /**
     * 代付下单
     *
     * @param withdrawLog      提现记录表
     * @param payAgentPlatform 代付通道表
     * @param reqPayAgent      下单数据，包括代付平台ID和提现记录订单号
     *
     * @return 代付下单是否成功
     */
    boolean orderPay( MemberWithdrawDetail withdrawLog, PayAgentChannel payAgentPlatform, ReqPayAgent reqPayAgent ) throws Exception;

    /**
     * 代付回调
     *
     * @param requestMap 代付方回调过来的数据
     * @param realIp     代付方回调IP
     *
     * @return 是否成功文本
     */
    String callbackPay( Map<String, Object> requestMap, String realIp ) throws Exception;

    /**
     * 代付反查
     *
     * @param requestMap 代付方反查过来的数据
     * @param realIp     代付方反查回调IP
     *
     * @return 代付方反查需要对象
     */
    Map<String, Object> reverseCheckOrderPay( Map<String, Object> requestMap, String realIp ) throws Exception;

    /**
     * 代付查询
     */
    String queryOrderPay( PayAgentLog payAgentLog ) throws Exception;
}
