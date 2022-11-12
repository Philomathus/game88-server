package tv.game88.game.api.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.dto.RspGameInfo;
import tv.game88.game.api.dto.RspGameType;
import tv.game88.game.api.dto.RspGameTypes;
import tv.game88.game.api.entity.GameInfo;
import tv.game88.game.api.entity.GamePlatform;
import tv.game88.game.api.service.GameService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Log4j2
@Service
public class GameServiceImpl implements GameService {

    public static final BigDecimal ONE_HUNDRED = new BigDecimal( 100 );

    @Resource
    private GameCacheUtils   gameCacheUtils;
    @Resource
    private MemberInfoMapper memberInfoMapper;

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

    @Override
    public RspBase<?> joinGame( Long infoId, PlatformUser platformUser ) {
        GameInfo gameInfo = gameCacheUtils.getGameInfo( infoId );
        if ( gameInfo == null || !gameInfo.getEffect() ) {
            return RspBase.businessError( "该游戏不存在或已关闭" );
        }
        if ( gameInfo.getMaintain() && platformUser.getStatus() != 2 ) {
            return RspBase.businessError( "该游戏正在维护,请选择其他游戏" );
        }
        GamePlatform gamePlatform = gameCacheUtils.getGamePlatform( gameInfo.getPlatformId() );
        if ( gamePlatform == null || !gamePlatform.getEffect() ) {
            return RspBase.businessError( "该游戏不存在或已关闭" );
        }
        if ( gamePlatform.getMaintain() && platformUser.getStatus() != 2 ) {
            return RspBase.businessError( "该游戏正在维护,请选择其他游戏" );
        }
        BigDecimal userBalance = memberInfoMapper.getUserBalance( platformUser.getId() );
        BigDecimal changeMoney = userBalance.setScale( 0, RoundingMode.DOWN );
        // 测试号最多上分100块
        if ( platformUser.getStatus() == 2 && changeMoney.compareTo( ONE_HUNDRED ) > 0 ) {
            changeMoney = ONE_HUNDRED;
        }
        if (changeMoney.compareTo( BigDecimal.ZERO ) < 0) {
            changeMoney = BigDecimal.ZERO;
        }

        return null;
    }
}
