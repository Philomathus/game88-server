package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.entity.GameInfo;
import tv.game88.game.api.entity.GameTypeWith;
import tv.game88.game.api.mapper.GameInfoMapper;
import tv.game88.game.api.mapper.GameTypeWithMapper;
import tv.game88.game.api.service.GameTypeWithService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class GameTypeWithServiceImpl extends ServiceImpl<GameTypeWithMapper, GameTypeWith> implements GameTypeWithService {
    @Resource
    private GameInfoMapper gameInfoMapper;
    @Resource
    private GameCacheUtils gameCacheUtils;

    @Override
    public List<GameTypeWith> selectGameTypeWithList( Long typeId ) {
        List<GameTypeWith> gameTypeWiths = this.baseMapper.selectGameTypeWithList( typeId );
        String             domainValue   = ConfigDomainCacheUtil.me.getDomainOssValue();
        for ( GameTypeWith type : gameTypeWiths ) {
            if ( StringUtils.isNotBlank( type.getGameInfoIcon() ) && !type.getGameInfoIcon().startsWith( "http" ) ) {
                type.setGameInfoIcon( domainValue + type.getGameInfoIcon() );
            }
        }
        return gameTypeWiths;
    }

    @Override
    public List<GameInfo> selectListNotType( Long typeId, String name ) {
        List<GameInfo> gameInfos   = gameInfoMapper.selectListNotType( typeId, name );
        String         domainValue = ConfigDomainCacheUtil.me.getDomainOssValue();
        for ( GameInfo info : gameInfos ) {
            if ( StringUtils.isNotBlank( info.getIcon() ) && !info.getIcon().startsWith( "http" ) ) {
                info.setIcon( domainValue + info.getIcon() );
            }
        }
        return gameInfos;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public RspBase<?> editTypeWith( Long typeId, List<Long> gameInfoIds ) {
        this.baseMapper.delete( new QueryWrapper<GameTypeWith>().eq( "type_id", typeId ) );
        List<GameTypeWith> gameTypeWiths = new ArrayList<>();
        for ( Long gameInfoId : gameInfoIds ) {
            GameTypeWith gameTypeWith = new GameTypeWith();
            gameTypeWith.setTypeId( typeId );
            gameTypeWith.setGameInfoId( gameInfoId );
            gameTypeWith.setSort( null );
            gameTypeWiths.add( gameTypeWith );
            if ( gameTypeWiths.size() >= 100 ) {
                this.baseMapper.insertBatch( gameTypeWiths );
                gameTypeWiths.clear();
            }
        }
        if ( gameTypeWiths.size() > 0 ) {
            this.baseMapper.insertBatch( gameTypeWiths );
        }
        gameCacheUtils.clear( GameCacheUtils.GAME_TYPE_INFO_WITH + typeId );
        return RspBase.ok();
    }
}
