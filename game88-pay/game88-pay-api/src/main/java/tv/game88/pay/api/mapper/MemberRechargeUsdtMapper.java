package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.pay.api.dto.ReqMemberRechargeUsdt;
import tv.game88.pay.api.dto.RspWithdrawRechargeDetail;
import tv.game88.pay.api.entity.MemberRechargeUsdt;

import java.util.List;
import java.util.Map;


/**
 * USDT充值提交记录Mapper接口
 *
 * @author mengJun
 */
public interface MemberRechargeUsdtMapper extends BaseMapper<MemberRechargeUsdt> {
    public List<MemberRechargeUsdt> selectMemberRechargeUsdtList( ReqMemberRechargeUsdt req );

    Map listCount( ReqMemberRechargeUsdt req );

    List<RspWithdrawRechargeDetail> selectRspDetail( @Param( "memberId" ) String memberId );
}
