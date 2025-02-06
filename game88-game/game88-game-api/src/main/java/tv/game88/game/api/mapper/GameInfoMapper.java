package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.game.api.dto.RspGameInfo;
import tv.game88.game.api.entity.GameInfo;

import java.util.List;

/**
 * 游戏信息Mapper接口
 *
 * @author mengJun
 */
public interface GameInfoMapper extends BaseMapper<GameInfo> {

    /**
     * 查询游戏信息列表
     *
     * @param gameInfo 游戏信息
     *
     * @return 游戏信息集合
     */
    public List<GameInfo> selectGameInfoList( GameInfo gameInfo );

    List<RspGameInfo> selectRspAllList( Long typeId );

    List<RspGameInfo> selectRspList( Long typeId );

    List<RspGameInfo> selectHotRspList( Long typeId );

    List<RspGameInfo> selectRspListByPlatform( @Param( "platformId" ) Long platformId );
}