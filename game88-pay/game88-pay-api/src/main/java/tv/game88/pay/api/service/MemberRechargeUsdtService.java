package tv.game88.pay.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.pay.api.dto.ReqMemberRechargeUsdt;
import tv.game88.pay.api.entity.MemberRechargeUsdt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MemberRechargeUsdtService extends IService<MemberRechargeUsdt> {
    List<MemberRechargeUsdt> selectMemberRechargeUsdtList( ReqMemberRechargeUsdt reqMemberRechargeUsdt );

    RspBase<Map> listCount( ReqMemberRechargeUsdt req );

    RspBase<?> lock( String orderNo, String username );

    RspBase<?> unLock( String orderNo, String userName, boolean contains );

    RspBase<?> refused( String orderNo, String username );

    RspBase<?> updateMemberRechargeUsdt( MemberRechargeUsdt memberRechargeUsdt, String username );

    public void updateMemberRechargeUsdtLogic( MemberInfo memberInfo, MemberRechargeUsdt update, BigDecimal rechargeMoney,
                                                  BigDecimal discountBill );

    RspBase<?> usdtRecharge( String memberId, ReqMemberRechargeUsdt req );
}

