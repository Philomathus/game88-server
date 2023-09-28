package tv.game88.pay.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.dto.ReqPayAgent;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.entity.PayAgentChannel;
import tv.game88.pay.api.entity.PayAgentLog;

import java.time.LocalDateTime;

public interface PayAgentService {
    void queryAgent4Status5Min();

    RspBase<?> payAgentOrder( ReqPayAgent reqPayAgent, String userName ) throws Exception;

    RspBase<?> payAgentOrders( ReqPayAgent reqPayAgent, String userName );

    /**
     * 代付订单记录处理
     *
     * @param withdrawDetail   代付日志
     * @param payAgentLog
     * @param orderNo          三方订单号
     * @param payAgentPlatform
     * @param isSuccess
     */
    void processOrderPay( MemberWithdrawDetail withdrawDetail, PayAgentLog payAgentLog, String orderNo,
                          PayAgentChannel payAgentPlatform, boolean isSuccess );

    void processOrder( PayAgentChannel payAgentPlatform, MemberWithdrawDetail withdrawDetail, LocalDateTime now, int status );

    void callBackOrder( MemberWithdrawDetail withdrawDetail, String channelName );
}
