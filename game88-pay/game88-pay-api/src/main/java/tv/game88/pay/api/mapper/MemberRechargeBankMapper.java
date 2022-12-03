package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.pay.api.dto.ReqMemberRechargeBank;
import tv.game88.pay.api.dto.RspRechargeBankReport;
import tv.game88.pay.api.dto.RspWithdrawRechargeDetail;
import tv.game88.pay.api.entity.MemberRechargeBank;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 公司入款信息Mapper接口
 *
 * @author mengJun
 */
public interface MemberRechargeBankMapper extends BaseMapper<MemberRechargeBank> {
    public List<MemberRechargeBank> selectMemberRechargeBankList( @Param( "req" ) ReqMemberRechargeBank req );

    List<RspRechargeBankReport> selectReportList( @Param( "req" ) ReqMemberRechargeBank req );

    Map selectReportListCount( @Param( "req" ) ReqMemberRechargeBank req );

    int countRechargeDaySucess( @Param( "memberId" ) String memberId );

    BigDecimal totalRechargeAll( @Param( "memberId" ) String memberId );

    List<RspWithdrawRechargeDetail> selectRspDetail( @Param( "memberId" ) String memberId );

    Map listCount( @Param( "req" ) ReqMemberRechargeBank req );
}
