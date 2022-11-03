package tv.game88.core.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.core.member.dto.RspMemberCard;
import tv.game88.core.member.entity.MemberCard;

import java.util.List;

/**
 * 会员银行卡Mapper接口
 *
 * @author mengJun
 */
public interface MemberCardMapper extends BaseMapper<MemberCard> {

    /**
     * 查询会员银行卡列表
     *
     * @param memberCard 会员银行卡
     *
     * @return 会员银行卡集合
     */
    public List<MemberCard> selectMemberCardList( MemberCard memberCard );

    List<MemberCard> selectMemberCard( @Param( "memberId" ) String memberId );

    List<RspMemberCard> selectRspList( @Param( "memberId" ) String memberId );
}
