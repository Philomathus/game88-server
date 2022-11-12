package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
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
        List<GameType> gameTypes   = this.baseMapper.selectGameTypeList( gameType );
        String         domainValue = ConfigDomainCacheUtil.me.getDomainOssValue();
        for ( GameType type : gameTypes ) {
            if ( StringUtils.isNotBlank( type.getIcon() ) && !type.getIcon().startsWith( "http" ) ) {
                type.setIcon( domainValue + type.getIcon() );
            }
        }
        return gameTypes;
    }
}