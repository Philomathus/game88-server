package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
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
     * @param memberGameData 会员游戏注单数据
     *
     * @return 会员游戏注单数据集合
     */
    public List<MemberGameData> selectMemberGameDataList( MemberGameData memberGameData );

    Integer findExist( @Param( "dbNodes" ) String dbNodes, @Param("keyId") String id );
}