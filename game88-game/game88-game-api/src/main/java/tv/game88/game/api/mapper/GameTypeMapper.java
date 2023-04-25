package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.game.api.dto.ReqGameType;
import tv.game88.game.api.dto.RspGameType;
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


    List<RspGameType> findList(@Param("req") ReqGameType req);
}