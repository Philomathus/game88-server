package tv.game88.pay.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.dto.ReqMemberCard;
import tv.game88.pay.api.dto.RspConfigBank;
import tv.game88.pay.api.dto.RspWithdrawBank;
import tv.game88.pay.api.entity.MemberRechargeBank;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface MemberRechargeBankService extends IService<MemberRechargeBank> {
    List<RspConfigBank> selectList( String memberId, Integer vip );

    RspBase<RspWithdrawBank> getBindCardList( String memberId );

    boolean setBindCardDv( String memberId, Long cardId );

    RspBase<?> setBindCard( String memberId, ReqMemberCard reqMemberCard );
}

