package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.MemberWithdrawDetail;

import java.util.List;

public interface MemberWithdrawDetailMapper extends BaseMapper<MemberWithdrawDetail> {
    public List<MemberWithdrawDetail> selectMemberWithdrawDetailList( MemberWithdrawDetail memberWithdrawDetail );
}