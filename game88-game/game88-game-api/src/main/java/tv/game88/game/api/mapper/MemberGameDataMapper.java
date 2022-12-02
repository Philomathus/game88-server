package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.game.api.dto.ReqGameData;
import tv.game88.game.api.dto.ReqMemberGameData;
import tv.game88.game.api.dto.RspCleanPlatform;
import tv.game88.game.api.dto.RspGameData;
import tv.game88.game.api.entity.MemberGameData;

import java.util.List;

/**
 * 会员游戏注单数据Mapper接口
 *
 * @author mengJun
 */
public interface MemberGameDataMapper extends BaseMapper<MemberGameData> {

    /**
     * 查询会员游戏注单数据列表
     *
     * @return 会员游戏注单数据集合
     */
    public List<MemberGameData> selectMemberGameDataList( ReqMemberGameData reqMemberGameData );

    Integer findExist( @Param( "dbNodes" ) String dbNodes, @Param( "keyId" ) String id );

    int insertMemberGameData( @Param( "req" ) MemberGameData memberGameData, @Param( "dbNodes" ) String dbNodes );

    List<RspCleanPlatform> findMemCleanPlatformLists( @Param( "dbNodes" ) String dbNodes, @Param( "memberId" ) String memberId );

    void updateByBatchClean( @Param( "dbNodes" ) String dbNodes, @Param( "memberId" ) String memberId );

    List<RspGameData> findByAccount( @Param( "dbNodes" ) String dbNodes, @Param( "memberId" ) String memberId,
                                     @Param( "req" ) ReqGameData reqGameData, @Param( "beginTime" ) String beginDay, @Param(
                                             "endTime" ) String endDay );

    MemberGameData getCountMemberGameDataList( ReqMemberGameData reqMemberGameData );
}