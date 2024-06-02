package tv.game88.pay.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.dto.ReqVipPayDeposit;
import tv.game88.pay.api.dto.RspVipPayLogin;

public interface EmbeddedPayService {
    RspBase<RspVipPayLogin> vipPayLogin( String memberId ) throws Exception;

    RspBase<?> vipPayDeposit( ReqVipPayDeposit reqVipPayDeposit, String memberId ) throws Exception;

    RspBase<RspVipPayLogin> qdPayLogin( String userId ) throws Exception;

    RspBase<?> qdPayDeposit( ReqVipPayDeposit reqVipPayDeposit, String userId ) throws Exception;
}
