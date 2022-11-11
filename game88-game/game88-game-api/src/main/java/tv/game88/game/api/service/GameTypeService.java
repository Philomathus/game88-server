package tv.game88.game.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.game.api.entity.GameType;

import java.util.List;

/**
 * 游戏类型Service接口
 *
 * @author mengJun
 */
public interface GameTypeService extends IService<GameType> {
    /**
     * 查询游戏类型列表
     *
     * @param gameType 游戏类型
     *
     * @return 游戏类型集合
     */
    public List<GameType> selectGameTypeList( GameType gameType );
}