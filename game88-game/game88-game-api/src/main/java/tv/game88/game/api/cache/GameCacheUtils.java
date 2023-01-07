package tv.game88.game.api.cache;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.game.api.dto.RspGameInfo;
import tv.game88.game.api.dto.RspGameType;
import tv.game88.game.api.entity.GameInfo;
import tv.game88.game.api.entity.GamePlatform;
import tv.game88.game.api.mapper.GameInfoMapper;
import tv.game88.game.api.mapper.GamePlatformMapper;
import tv.game88.game.api.mapper.GameTypeMapper;
import tv.game88.game.api.type.EnumGameCategory;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GameCacheUtils {

    public static final String GAME_TYPE_KEY          = Constants.GAME_PREX + "type:effect";
    public static final String GAME_PLATFORM_KEY      = Constants.GAME_PREX + "platform:";
    public static final String GAME_PLATFORM_LIST_KEY = Constants.GAME_PREX + "platformList";
    public static final String GAME_INFO_KEY          = Constants.GAME_PREX + "info:";
    public static final String GAME_INFO_LIST_KEY     = Constants.GAME_PREX + "infoList:";
    public static final String GAME_INFO_S_KEY        = Constants.GAME_PREX + "infos:";

    @Resource
    private RedisUtils         redisUtils;
    @Resource
    private GameTypeMapper     gameTypeMapper;
    @Resource
    private GamePlatformMapper gamePlatformMapper;
    @Resource
    private GameInfoMapper     gameInfoMapper;

    public List<RspGameType> getEffectTypeList() {
        if ( !redisUtils.exists( GAME_TYPE_KEY ) ) {
            List<RspGameType> gameTypes = new QueryChainWrapper<>( gameTypeMapper )
                    .eq( "effect", 1 )
                    .orderByAsc( "sort" )
                    .list()
                    .stream()
                    .map( gameType -> {
                        RspGameType rspGameType = new RspGameType();
                        BeanUtils.copyProperties( gameType, rspGameType );
                        return rspGameType;
                    } )
                    .collect( Collectors.toList() );
            if ( !CollectionUtils.isEmpty( gameTypes ) ) {
                redisUtils.strSet( GAME_TYPE_KEY, JsonUtil.object2Json( gameTypes ) );
            }
            return gameTypes;
        }
        String s = redisUtils.strGet( GAME_TYPE_KEY );
        return StringUtils.isBlank( s ) ? null : JsonUtil.json2Array( s, new TypeReference<>() {} );
    }

    public GamePlatform getGamePlatform( Long platformId ) {
        if ( !redisUtils.exists( GAME_PLATFORM_KEY + platformId ) ) {
            GamePlatform gamePlatform = new QueryChainWrapper<>( gamePlatformMapper ).eq( "id", platformId ).one();
            if ( gamePlatform != null ) {
                redisUtils.strSet( GAME_PLATFORM_KEY + platformId, JsonUtil.object2Json( gamePlatform ) );
            }
            return gamePlatform;
        }
        String s = redisUtils.strGet( GAME_PLATFORM_KEY + platformId );
        return StringUtils.isBlank( s ) ? null : JsonUtil.json2Object( s, GamePlatform.class );
    }

    public List<GamePlatform> getGamePlatformList() {
        if ( !redisUtils.exists( GAME_PLATFORM_LIST_KEY ) ) {
            List<GamePlatform> gamePlatforms = new QueryChainWrapper<>( gamePlatformMapper ).list();
            if ( !CollectionUtils.isEmpty( gamePlatforms ) ) {
                redisUtils.strSet( GAME_PLATFORM_LIST_KEY, JsonUtil.object2Json( gamePlatforms ) );
            }
            return gamePlatforms;
        }
        String s = redisUtils.strGet( GAME_PLATFORM_LIST_KEY );
        return StringUtils.isBlank( s ) ? null : JsonUtil.json2Array( s, new TypeReference<>() {} );
    }

    public GameInfo getGameInfo( Long infoId ) {
        if ( !redisUtils.exists( GAME_INFO_KEY + infoId ) ) {
            GameInfo gameInfo = new QueryChainWrapper<>( gameInfoMapper ).eq( "id", infoId ).one();
            if ( gameInfo != null ) {
                redisUtils.strSet( GAME_INFO_KEY + infoId, JsonUtil.object2Json( gameInfo ) );
            }
            return gameInfo;
        }
        String s = redisUtils.strGet( GAME_INFO_KEY + infoId );
        return StringUtils.isBlank( s ) ? null : JsonUtil.json2Object( s, GameInfo.class );
    }

    public List<RspGameInfo> getEffectInfoList( Long typeId ) {
        if ( !redisUtils.exists( GAME_INFO_LIST_KEY + typeId ) ) {
            List<RspGameInfo> rspGameInfoList = gameInfoMapper.selectRspList( typeId );
            if ( !CollectionUtils.isEmpty( rspGameInfoList ) ) {
                for ( RspGameInfo rspGameInfo : rspGameInfoList ) {
                    if ( rspGameInfo.getGameCategory() == EnumGameCategory.LOTTERY
                            && StringUtils.isNotBlank( rspGameInfo.getKindId() ) ) {
                        rspGameInfo.setLotteryId( Long.parseLong( rspGameInfo.getKindId() ) );
                    }
                }
                redisUtils.strSet( GAME_INFO_LIST_KEY + typeId, JsonUtil.object2Json( rspGameInfoList ) );
            }
            return rspGameInfoList;
        }
        String s = redisUtils.strGet( GAME_INFO_LIST_KEY + typeId );
        return StringUtils.isBlank( s ) ? null : JsonUtil.json2Array( s, new TypeReference<>() {} );
    }

    public List<RspGameInfo> getEffectInfoList( Long typeId, Long platformId ) {
        List<RspGameInfo> rspGameInfoList;
        if ( !redisUtils.exists( GAME_INFO_S_KEY + typeId ) ) {
            if ( typeId == 1 ) {
                rspGameInfoList = gameInfoMapper.selectRspListByRecommend();
            } else {
                rspGameInfoList = gameInfoMapper.selectRspList( typeId );
            }
            if ( !CollectionUtils.isEmpty( rspGameInfoList ) ) {
                for ( RspGameInfo rspGameInfo : rspGameInfoList ) {
                    if ( rspGameInfo.getGameCategory() == EnumGameCategory.LOTTERY
                            && StringUtils.isNotBlank( rspGameInfo.getKindId() ) ) {
                        rspGameInfo.setLotteryId( Long.parseLong( rspGameInfo.getKindId() ) );
                    }
                }
                redisUtils.strSet( GAME_INFO_S_KEY + typeId, JsonUtil.object2Json( rspGameInfoList ) );
            }
        } else {
            String s = redisUtils.strGet( GAME_INFO_S_KEY + typeId );
            rspGameInfoList = StringUtils.isBlank( s ) ? null : JsonUtil.json2Array( s, new TypeReference<>() {} );
        }
        if ( platformId != null && !CollectionUtils.isEmpty( rspGameInfoList ) ) {
            rspGameInfoList.removeIf( rspGameInfo -> rspGameInfo.getPlatformId() != platformId.intValue() );
        }
        return rspGameInfoList;
    }

    public void clear( String key ) {
        if ( key.startsWith( GAME_PLATFORM_KEY ) ) {
            redisUtils.unlink( GAME_PLATFORM_LIST_KEY );
        }
        redisUtils.unlink( key );
    }

    public void clearByInfoId( Long gameInfoId ) {
        GameInfo gameInfo = getGameInfo( gameInfoId );
        if ( gameInfo != null ) {
            this.clear( GAME_INFO_KEY + gameInfoId );
        }
        List<RspGameType> effectTypeList = getEffectTypeList();
        for ( RspGameType rspGameType : effectTypeList ) {
            this.clear( GAME_INFO_LIST_KEY + rspGameType.getId() );
            this.clear( GAME_INFO_S_KEY + rspGameType.getId() );
        }
    }
}
