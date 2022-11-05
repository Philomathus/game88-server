package tv.game88.pay.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.dto.*;
import tv.game88.pay.api.entity.MemberRechargeBank;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface MemberRechargeBankService extends IService<MemberRechargeBank> {
    List<RspPayRechargeBank> selectList( String memberId, Integer vip );

    RspBase<RspWithdrawBank> getBindCardList( String memberId );

    boolean setBindCardDv( String memberId, Long cardId );

    RspBase<?> setBindCard( String memberId, ReqMemberCard reqMemberCard );

    List<RspRechargeBankReport> selectReportList( ReqMemberRechargeBank req );

    Map selectReportListCount( ReqMemberRechargeBank req );

    List<MemberRechargeBank> selectMemberRechargeBankList( ReqMemberRechargeBank req );

    RspBase<?> firstAudit( ReqMemberRechargeBank req, String userName );

    RspBase<?> finalAudit( ReqMemberRechargeBank req, String userName );

    RspBase<?> refusedAudit( ReqMemberRechargeBank req, String userName );

    RspBase<?> recoverAudit( ReqMemberRechargeBank req, String userName );
}

