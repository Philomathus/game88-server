package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.game.api.entity.GameType;
import tv.game88.game.api.mapper.GameTypeMapper;
import tv.game88.game.api.service.GameTypeService;

import java.util.List;

/**
 * 游戏类型Service业务层处理
 *
 * @author mengJun
 */
@Service
public class GameTypeServiceImpl extends ServiceImpl<GameTypeMapper, GameType> implements GameTypeService {
    /**
     * 查询游戏类型列表
     *
     * @param gameType 游戏类型
     *
     * @return 游戏类型
     */
    @Override
    public List<GameType> selectGameTypeList( GameType gameType ) {
        return this.baseMapper.selectGameTypeList( gameType );
    }
}