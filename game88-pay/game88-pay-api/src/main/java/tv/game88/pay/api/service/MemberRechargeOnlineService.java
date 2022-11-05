package tv.game88.pay.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.dto.ReqMemberRechargeOnline;
import tv.game88.pay.api.entity.MemberRechargeOnline;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface MemberRechargeOnlineService extends IService<MemberRechargeOnline> {
    List<MemberRechargeOnline> selectMemberRechargeOnlineList( ReqMemberRechargeOnline req );

    Map listCount( ReqMemberRechargeOnline req );

    RspBase<?> payPatchOrder( MemberRechargeOnline memberRechargeOnline );
}

