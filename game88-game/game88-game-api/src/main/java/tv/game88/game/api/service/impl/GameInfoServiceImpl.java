package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.game.api.dto.ReqGameTypeWith;
import tv.game88.game.api.entity.GameInfo;
import tv.game88.game.api.entity.GameType;
import tv.game88.game.api.entity.GameTypeWith;
import tv.game88.game.api.mapper.GameInfoMapper;
import tv.game88.game.api.mapper.GameTypeWithMapper;
import tv.game88.game.api.service.GameInfoService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 游戏信息Service业务层处理
 *
 * @author mengJun
 */
@Service
public class GameInfoServiceImpl extends ServiceImpl<GameInfoMapper, GameInfo> implements GameInfoService {

    @Resource
    private GameTypeWithMapper gameTypeWithMapper;

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

    @Override
    public List<GameInfo> selectListByType( Long typeId ) {
        return this.baseMapper.selectListByType( typeId );
    }

    @Override
    public List<GameInfo> selectListNotType( Long typeId ) {
        return this.baseMapper.selectListNotType( typeId );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public RspBase<?> editTypeWith( Long typeId, List<ReqGameTypeWith> reqGameTypeWiths ) {
        gameTypeWithMapper.delete( new QueryWrapper<GameTypeWith>().eq( "type_id", typeId ) );
        List<GameTypeWith> gameTypeWiths = new ArrayList<>();
        for ( ReqGameTypeWith req : reqGameTypeWiths ) {
            GameTypeWith gameTypeWith = new GameTypeWith();
            gameTypeWith.setTypeId( typeId );
            gameTypeWith.setGameInfoId( req.getGameInfoId() );
            gameTypeWith.setSort( req.getSort() );
            gameTypeWiths.add( gameTypeWith );
            if ( gameTypeWiths.size() >= 100 ) {
                gameTypeWithMapper.insertBatch( gameTypeWiths );
                gameTypeWiths.clear();
            }
        }
        if ( gameTypeWiths.size() > 0 ) {
            gameTypeWithMapper.insertBatch( gameTypeWiths );
        }
        return RspBase.ok();
    }
}