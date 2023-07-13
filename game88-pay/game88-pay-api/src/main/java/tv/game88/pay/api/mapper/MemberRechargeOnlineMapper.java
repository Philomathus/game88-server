package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.pay.api.dto.ReqMemberRechargeOnline;
import tv.game88.pay.api.dto.RspRechargeOnline;
import tv.game88.pay.api.dto.RspWithdrawRechargeDetail;
import tv.game88.pay.api.entity.MemberRechargeOnline;

import java.util.List;
import java.util.Map;

public interface MemberRechargeOnlineMapper extends BaseMapper<MemberRechargeOnline> {
    public List<MemberRechargeOnline> selectMemberRechargeOnlineList( ReqMemberRechargeOnline req );

    List<Map<String, Object>> countOrder( String memberId );

    Map listCount( ReqMemberRechargeOnline req );

    List<RspWithdrawRechargeDetail> selectRspDetail( @Param ( "memberId" ) String memberId );

    MemberRechargeOnline selectMemberRechargeOnlineById( String id );

    List<RspRechargeOnline> selectRspReportList( ReqMemberRechargeOnline req );

    Map reportListCount( ReqMemberRechargeOnline req );

    int successTodayCount( @Param ( "memberId" ) String memberId, @Param ( "platformId" ) Long platformId );
}