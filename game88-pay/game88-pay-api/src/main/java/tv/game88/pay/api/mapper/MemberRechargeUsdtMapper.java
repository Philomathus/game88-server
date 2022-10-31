package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.MemberRechargeUsdt;

import java.util.List;


/**
 * USDT充值提交记录Mapper接口
 *
 * @author 77tv
 * @date 2021-09-14
 */
public interface MemberRechargeUsdtMapper extends BaseMapper<MemberRechargeUsdt> {
    public List<MemberRechargeUsdt> selectMemberRechargeUsdtList( MemberRechargeUsdt memberRechargeUsdt );
}
