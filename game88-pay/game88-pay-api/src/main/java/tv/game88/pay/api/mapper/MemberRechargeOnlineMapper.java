package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.MemberRechargeOnline;

import java.util.List;

public interface MemberRechargeOnlineMapper extends BaseMapper<MemberRechargeOnline> {

    public List<MemberRechargeOnline> selectMemberRechargeOnlineList( MemberRechargeOnline memberRechargeOnline );

}