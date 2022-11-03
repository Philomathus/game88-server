package tv.game88.pay.api.service;

import tv.game88.core.member.entity.MemberInfo;
import tv.game88.pay.api.dto.RspMemberWithdrawLogInfo;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import com.baomidou.mybatisplus.extension.service.IService;

public interface MemberWithdrawDetailService extends IService<MemberWithdrawDetail> {
    RspMemberWithdrawLogInfo getRspWithdrawInfo( MemberInfo memberInfo );
}

