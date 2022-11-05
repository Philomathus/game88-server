package tv.game88.pay.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.pay.api.dto.ReqMemberWithdrawDetail;
import tv.game88.pay.api.dto.RspMemberWithdrawDetailShunWei;
import tv.game88.pay.api.dto.RspMemberWithdrawDetailInfo;
import tv.game88.pay.api.dto.RspWithdrawReport;
import tv.game88.pay.api.entity.MemberWithdrawDetail;

import java.util.List;
import java.util.Map;

public interface MemberWithdrawDetailService extends IService<MemberWithdrawDetail> {
    RspMemberWithdrawDetailInfo getRspWithdrawDetail( MemberInfo memberInfo );

    List<MemberWithdrawDetail> selectMemberWithdrawDetailList( ReqMemberWithdrawDetail reqMemberWithdrawDetail );

    List<MemberWithdrawDetail> selectMemberWithdrawDetailCount( ReqMemberWithdrawDetail reqMemberWithdrawDetail );

    List<RspMemberWithdrawDetailShunWei> selectMemberWithdrawDetailShunWeiList( ReqMemberWithdrawDetail req );

    Map<String, Object> getTotal( ReqMemberWithdrawDetail reqMemberWithdrawDetail );

    RspBase<List<RspWithdrawReport>> withdrawReport( String id );

    public void refusedUpdateProcess( MemberWithdrawDetail memberWithdrawLog, String mark );

    RspBase<?> refused( ReqMemberWithdrawDetail req, String userName );

    RspBase<?> refuseds( ReqMemberWithdrawDetail req, String userName );

    RspBase<?> back( ReqMemberWithdrawDetail req, String userName );

    RspBase<?> failBack( ReqMemberWithdrawDetail req, String userName );

    RspBase<?> queryStatus( ReqMemberWithdrawDetail req );

    RspBase<?> lock( ReqMemberWithdrawDetail req, String userName );

    RspBase<?> locks( ReqMemberWithdrawDetail req, String userName );

    RspBase<?> unlock( ReqMemberWithdrawDetail req, String userName, boolean contains );

    RspBase<?> artificial( ReqMemberWithdrawDetail req, String userName );

    RspBase<?> updateRemark( ReqMemberWithdrawDetail req, String userName );

    RspBase<?> abnormalWithdrawal( ReqMemberWithdrawDetail req, String userName );

    RspBase<?> manualWithdrawal( ReqMemberWithdrawDetail req, String userName );
}
