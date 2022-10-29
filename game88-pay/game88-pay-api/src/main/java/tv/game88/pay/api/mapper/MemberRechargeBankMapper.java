package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.MemberRechargeBank;

import java.util.List;

/**
 * 公司入款信息Mapper接口
 *
 * @author 77tv
 */
public interface MemberRechargeBankMapper extends BaseMapper<MemberRechargeBank> {
    public List<MemberRechargeBank> selectMemberRechargeBankList( MemberRechargeBank memberRechargeBank );

}
