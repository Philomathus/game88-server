package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.pay.api.dto.ReqMemberWithdrawDetail;
import tv.game88.pay.api.dto.RspMemberInfoWithdraw;
import tv.game88.pay.api.dto.RspMemberWithdrawDetailShunWei;
import tv.game88.pay.api.dto.RspWithdrawRechargeDetail;
import tv.game88.pay.api.entity.MemberWithdrawDetail;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MemberWithdrawDetailMapper extends BaseMapper<MemberWithdrawDetail> {
    public List<MemberWithdrawDetail> selectMemberWithdrawDetailList( ReqMemberWithdrawDetail reqMemberWithdrawDetail );

    List<MemberWithdrawDetail> countOpNameOrder( ReqMemberWithdrawDetail reqMemberWithdrawDetail );

    List<RspMemberWithdrawDetailShunWei> selectMemberWithdrawDetailShunWeiList( @Param( "array" ) List<String> ids );

    Map<String, Object> getTotal( ReqMemberWithdrawDetail req );

    RspMemberInfoWithdraw selectMemberInfoWithdrawByIda( @Param( "userid" ) String id, @Param( "tableLast" ) String tableLast );

    RspMemberInfoWithdraw selectMemberInfoWithdrawByIdb( @Param( "userid" ) String id, @Param( "tableLast" ) String tableLast );

    RspMemberInfoWithdraw selectMemberInfoWithdrawByIdc( @Param( "userid" ) String id, @Param( "tableLast" ) String tableLast );

    RspMemberInfoWithdraw selectMemberInfoWithdrawByIde( @Param( "userId" ) String id, @Param( "tableLast" ) String tableLast );

    RspMemberInfoWithdraw selectMemberInfoWithdrawByIdf( @Param( "userId" ) String id, @Param( "tableLast" ) String tableLast );

    RspMemberInfoWithdraw selectMemberInfoWithdrawByIdg( @Param( "userid" ) String id, @Param( "tableLast" ) String tableLast );

    RspMemberInfoWithdraw selectMemberInfoWithdrawByIdh( @Param( "userid" ) String id, @Param( "tableLast" ) String tableLast );

    RspMemberInfoWithdraw selectMemberInfoWithdrawByIdi( @Param( "userid" ) String id, @Param( "tableLast" ) String tableLast );

    RspMemberInfoWithdraw selectMemberInfoWithdrawByIdj( @Param( "userid" ) String id, @Param( "tableLast" ) String tableLast );

    RspMemberInfoWithdraw selectMemberInfoWithdrawByIdk( @Param( "userid" ) String id, @Param( "tableLast" ) String tableLast );

    List<RspMemberInfoWithdraw> selectMemberInfoWithdrawByIdl( @Param( "userid" ) String id,
                                                               @Param( "tableLast" ) String tableLast );

    RspMemberInfoWithdraw selectMemberInfoWithdrawByIdz( @Param( "userid" ) String id, @Param( "tableLast" ) String tableLast );

    BigDecimal totalWithdrawMoney( String memberId );

    List<RspWithdrawRechargeDetail> selectRspDetail( @Param( "memberId" ) String memberId );
}