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

    @Override
    public RspBase<?> updateTypeWith( GameTypeWith gameTypeWith ) {
        GameTypeWith update = new GameTypeWith();
        update.setSort( gameTypeWith.getSort() );
        int updateNum = this.baseMapper.update( update, new QueryWrapper<GameTypeWith>()
                .eq( "type_id", gameTypeWith.getTypeId() )
                .eq( "game_info_id", gameTypeWith.getGameInfoId() ) );
        if ( updateNum > 0 ) {
            gameCacheUtils.clear( GameCacheUtils.GAME_TYPE_INFO_WITH + gameTypeWith.getTypeId() );
        }
        return updateNum > 0 ? RspBase.ok( "更新成功" ) : RspBase.businessError( "更新失败" );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public RspBase<?> insertTypeWith( Long typeId, List<Long> gameInfoIds ) {
        this.baseMapper.delete( new QueryWrapper<GameTypeWith>().eq( "type_id", typeId ) );
        List<GameTypeWith> gameTypeWiths = new ArrayList<>();
        for ( Long gameInfoId : gameInfoIds ) {
            GameTypeWith gameTypeWith = new GameTypeWith();
            gameTypeWith.setTypeId( typeId );
            gameTypeWith.setGameInfoId( gameInfoId );
            gameTypeWith.setSort( 99L );
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
        return RspBase.ok( "新增成功" );
    }

    @Override
    public RspBase<?> deleteTypeWith( Long typeId, List<Long> gameInfoIds ) {
        int delete = this.baseMapper.delete( new QueryWrapper<GameTypeWith>()
                .eq( "type_id", typeId )
                .in( "game_info_id", gameInfoIds ) );
        if ( delete > 0 ) {
            gameCacheUtils.clear( GameCacheUtils.GAME_TYPE_INFO_WITH + typeId );
        }
        return delete > 0 ? RspBase.ok( "剔除成功" ) : RspBase.businessError( "剔除失败" );
    }
}
