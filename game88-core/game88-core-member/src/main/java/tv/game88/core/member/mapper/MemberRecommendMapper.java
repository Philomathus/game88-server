package tv.game88.core.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.core.member.dto.RspMemberRecommend;
import tv.game88.core.member.dto.RspMyRecommend;
import tv.game88.core.member.entity.MemberRecommend;

import java.util.List;

public interface MemberRecommendMapper extends BaseMapper<MemberRecommend> {
    List<RspMemberRecommend> getRecommendDetailList( @Param( "code" ) String code, @Param( "memberId" ) String memberId );

    RspMyRecommend getMyRecommend( @Param( "inviterId" ) String memberId );

    int updateCommissionByBatch( List<MemberRecommend> listRecommend );
}

