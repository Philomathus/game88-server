package tv.game88.pay.api.base;

import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentPlatform;

import java.util.Map;

public interface BasePayAgent {

    String getName();

    /**
     * 代付下单
     *
     * @param withdrawDetail  提现记录表
     * @param payAgentChannel 代付通道表
     * @param reqPayAgent     下单数据，包括代付平台ID和提现记录订单号
     *
     * @return 代付下单是否成功
     */
    boolean orderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel, PayAgentPlatform payAgentPlatform,
                      ReqPayAgent reqPayAgent ) throws Exception;

    /**
     * 代付回调
     *
     * @param requestMap 代付方回调过来的数据
     * @param realIp     代付方回调IP
     *
     * @return 是否成功文本
     */
    String callbackPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap, String realIp ) throws Exception;

    /**
     * 代付反查
     *
     * @param requestMap 代付方反查过来的数据
     * @param realIp     代付方反查回调IP
     *
     * @return 代付方反查需要对象
     */
    Map<String, Object> reverseCheckOrderPay( PayAgentPlatform payAgentPlatform, Map<String, Object> requestMap,
                                              String realIp ) throws Exception;

    /**
     * 代付查询
     */
    String queryOrderPay( MemberWithdrawDetail withdrawDetail, PayAgentChannel payAgentChannel,
                          PayAgentPlatform payAgentPlatform ) throws Exception;
}
