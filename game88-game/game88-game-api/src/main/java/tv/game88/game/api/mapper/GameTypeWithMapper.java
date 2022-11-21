package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.game.api.entity.GameTypeWith;

import java.util.List;

/**
 * 游戏类型与游戏信息关联Mapper接口
 *
 * @author mengJun
 */
public interface GameTypeWithMapper extends BaseMapper<GameTypeWith> {
    int insertBatch( List<GameTypeWith> gameTypeWiths );

    List<Long> selectTypeIdByPlatformId( Long platformId );

    List<GameTypeWith> selectGameTypeWithList( Long typeId );
}