package tv.game88.game.api.cache;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import tv.game88.game.api.entity.GameTypeWith;
import tv.game88.game.api.mapper.GameInfoMapper;
import tv.game88.game.api.mapper.GamePlatformMapper;
import tv.game88.game.api.mapper.GameTypeMapper;
import tv.game88.game.api.mapper.GameTypeWithMapper;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GameCacheUtils {

    public static final String GAME_TYPE_KEY     = Constants.GAME_PREX + "type:effect";
    public static final String GAME_PLATFORM_KEY = Constants.GAME_PREX + "platform:";
    public static final String GAME_INFO_KEY     = Constants.GAME_PREX + "info:";

    public static final String GAME_TYPE_INFO_WITH = Constants.GAME_PREX + "typeWithInfo:";

    @Resource
    private RedisUtils         redisUtils;
    @Resource
    private GameTypeMapper     gameTypeMapper;
    @Resource
    private GamePlatformMapper gamePlatformMapper;
    @Resource
    private GameInfoMapper     gameInfoMapper;
    @Resource
    private GameTypeWithMapper gameTypeWithMapper;

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
        if ( !redisUtils.exists( GAME_TYPE_INFO_WITH + typeId ) ) {
            List<Long> infoIds = gameTypeWithMapper
                    .selectObjs( new QueryWrapper<GameTypeWith>()
                            .eq( "type_id", typeId )
                            .orderByAsc( "sort" )
                            .select( "game_info_id" ) )
                    .stream()
                    .map( o -> ( Long ) o )
                    .toList();
            if ( CollectionUtils.isEmpty( infoIds ) ) {
                return null;
            }
            List<RspGameInfo> rspGameInfoList = gameInfoMapper.selectRspList( infoIds );
            if ( !CollectionUtils.isEmpty( rspGameInfoList ) ) {
                redisUtils.strSet( GAME_TYPE_INFO_WITH + typeId, JsonUtil.object2Json( rspGameInfoList ) );
            }
            return rspGameInfoList;
        }
        String s = redisUtils.strGet( GAME_TYPE_INFO_WITH + typeId );
        return StringUtils.isBlank( s ) ? null : JsonUtil.json2Array( s, new TypeReference<>() {} );
    }

    public void clear( String key ) {
        redisUtils.unlink( key );
    }

    public void clearByInfoId( Long gameInfoId ) {
        this.clear( GAME_INFO_KEY + gameInfoId );
        gameTypeWithMapper
                .selectObjs( new QueryWrapper<GameTypeWith>().eq( "game_info_id", gameInfoId ).select( "type_id" ) )
                .stream()
                .map( o -> ( Long ) o )
                .forEach( typeId -> this.clear( GAME_TYPE_INFO_WITH + typeId ) );
    }

    public void clearTypeWithByPlatformId( Long platformId ) {
        gameTypeWithMapper.selectTypeIdByPlatformId( platformId ).forEach( typeId -> this.clear( GAME_TYPE_INFO_WITH + typeId ) );
    }
}
