package tv.game88.pay.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.pay.api.dto.ReqMemberRechargeUsdt;
import tv.game88.pay.api.entity.MemberRechargeUsdt;

import java.util.List;
import java.util.Map;

public interface MemberRechargeUsdtService extends IService<MemberRechargeUsdt> {
    List<MemberRechargeUsdt> selectMemberRechargeUsdtList( ReqMemberRechargeUsdt reqMemberRechargeUsdt );

    RspBase<Map> listCount( ReqMemberRechargeUsdt req );

    RspBase<?> lock( String orderNo, String username );

    RspBase<?> unLock( String orderNo, String userName, boolean contains );

    RspBase<?> refused( String orderNo, String username, String remark );

    RspBase<?> updateMemberRechargeUsdt( MemberRechargeUsdt memberRechargeUsdt, String username );

    RspBase<?> usdtRecharge( PlatformUser platformUser, ReqMemberRechargeUsdt req );
}

