package tv.game88.pay.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.pay.api.dto.ReqPayRecharge;
import tv.game88.pay.api.dto.RspPayChannel;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import tv.game88.pay.api.entity.PayType;

import java.util.List;

public interface PayService {
    List<PayType> findPayTypeList( PlatformUser platformUser, String deviceType );

    List<RspPayChannel> findPayChannelList( Long typeId, PlatformUser platformUser );

    List<RspPayChannel> findPayChannel(Long typeId, PlatformUser platformUser );

    String payRedirect( String orderNo );

    RspBase<?> payRecharge( ReqPayRecharge reqPayRecharge, PlatformUser platformUser ) throws Exception;

    void payQuery10Min() throws Exception;

    String updatePayJourStatus( MemberRechargeOnline memberRechargeOnline, String[] notifyResultWays);
    void updatePayJourStatus( MemberRechargeOnline memberRechargeOnline);

}
