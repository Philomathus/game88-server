package tv.game88.core.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.core.member.dto.RspCodeFlow;
import tv.game88.core.member.entity.MemberBcode;

import java.util.List;

/**
 * 打码Mapper接口
 *
 * @author mengJun
 */
public interface MemberBcodeMapper extends BaseMapper<MemberBcode> {

    /**
     * 查询打码列表
     *
     * @param memberBcode 打码
     *
     * @return 打码集合
     */
    public List<MemberBcode> selectMemberBcodeList( MemberBcode memberBcode );

    List<RspCodeFlow> findByMemberId( @Param( "userId" ) String userId );

    /**
     * 查询MemberBcode列表
     *
     * @param memberBcode MemberBcode
     *
     * @return MemberBcode集合
     */
    public List<MemberBcode> selectWillBcodeList( MemberBcode memberBcode );

    int updateMemberBcodeStatus( @Param( "memberId" ) String memberId );

    int repairMemberInfo( @Param( "memberId" ) String memberId );

    MemberBcode getTotalData( MemberBcode memberBcode );
}
