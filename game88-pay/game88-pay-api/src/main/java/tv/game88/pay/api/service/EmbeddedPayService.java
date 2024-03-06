package tv.game88.pay.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.dto.ReqVipPayDeposit;
import tv.game88.pay.api.dto.RspVipPayLogin;

public interface EmbeddedPayService {
    RspBase<RspVipPayLogin> vipPayLogin( String memberId );

    RspBase<?> vipPayDeposit( ReqVipPayDeposit reqVipPayDeposit, String memberId );

    RspBase<RspVipPayLogin> qdPayLogin( String userId );

    RspBase<?> qdPayDeposit( ReqVipPayDeposit reqVipPayDeposit, String userId );
}
