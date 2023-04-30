package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.game.api.entity.GameType;

import java.util.List;

/**
 * 游戏类型Mapper接口
 *
 * @author mengJun
 */
public interface GameTypeMapper extends BaseMapper<GameType> {

    /**
     * 查询游戏类型列表
     *
     * @param gameType 游戏类型
     *
     * @return 游戏类型集合
     */
    public List<GameType> selectGameTypeList( GameType gameType );
}