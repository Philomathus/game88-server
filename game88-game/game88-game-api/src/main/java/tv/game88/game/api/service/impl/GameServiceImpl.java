package tv.game88.game.api.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.dto.RspGameInfo;
import tv.game88.game.api.dto.RspGameType;
import tv.game88.game.api.dto.RspGameTypes;
import tv.game88.game.api.service.GameService;

import javax.annotation.Resource;
import java.util.List;

@Log4j2
@Service
public class GameServiceImpl implements GameService {
    @Resource
    private GameCacheUtils gameCacheUtils;

    @Override
    public RspGameTypes getGameTypes() {
        List<RspGameType> gameTypeList = gameCacheUtils.getEffectTypeList();
        RspGameTypes      rspGameTypes = new RspGameTypes();
        rspGameTypes.setRspGameTypes( gameTypeList );
        if ( !CollectionUtils.isEmpty( gameTypeList ) ) {
            Long typeId = gameTypeList.get( 0 ).getId();
            rspGameTypes.setRspGameInfos( gameCacheUtils.getEffectInfoList( typeId ) );
        }
        return rspGameTypes;
    }

    @Override
    public List<RspGameInfo> getGameInfoList( Long typeId ) {
        return gameCacheUtils.getEffectInfoList( typeId );
    }
}
