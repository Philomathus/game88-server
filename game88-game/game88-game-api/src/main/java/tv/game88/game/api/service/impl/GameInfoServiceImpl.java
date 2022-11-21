package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.game.api.entity.GameInfo;
import tv.game88.game.api.mapper.GameInfoMapper;
import tv.game88.game.api.service.GameInfoService;

import java.util.List;

/**
 * 游戏信息Service业务层处理
 *
 * @author mengJun
 */
@Service
public class GameInfoServiceImpl extends ServiceImpl<GameInfoMapper, GameInfo> implements GameInfoService {

    /**
     * 查询游戏信息列表
     *
     * @param gameInfo 游戏信息
     *
     * @return 游戏信息
     */
    @Override
    public List<GameInfo> selectGameInfoList( GameInfo gameInfo ) {
        List<GameInfo> gameInfos = this.baseMapper.selectGameInfoList( gameInfo );
        String         domainValue = ConfigDomainCacheUtil.me.getDomainOssValue();
        for ( GameInfo info : gameInfos ) {
            if ( StringUtils.isNotBlank( info.getIcon() ) && !info.getIcon().startsWith( "http" ) ) {
                info.setIcon( domainValue + info.getIcon() );
            }
        }
        return gameInfos;
    }
}