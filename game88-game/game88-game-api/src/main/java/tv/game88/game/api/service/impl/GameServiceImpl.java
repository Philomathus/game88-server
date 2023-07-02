package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.game.dto.RspGameDataLog;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.game.api.base.BaseGameDock;
import tv.game88.game.api.base.GameDockFactoryUtil;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.dto.*;
import tv.game88.game.api.entity.GameInfo;
import tv.game88.game.api.entity.GamePlatform;
import tv.game88.game.api.entity.MemberGameMoney;
import tv.game88.game.api.exception.GameTransferException;
import tv.game88.game.api.mapper.GameInfoMapper;
import tv.game88.game.api.mapper.GamePlatformMapper;
import tv.game88.game.api.mapper.MemberGameMoneyMapper;
import tv.game88.game.api.service.GameService;
import tv.game88.game.api.service.MemberGameMoneyService;
import tv.game88.core.game.type.EnumGameCategory;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_OK;

@Log4j2
@Service
public class GameServiceImpl implements GameService {

    public static final BigDecimal ONE_HUNDRED = new BigDecimal( 100 );

    @Resource
    private RestTemplate           restTemplate;
    @Resource
    private RedisUtils             redisUtils;
    @Resource
    private GameCacheUtils         gameCacheUtils;
    @Resource
    private GameDockFactoryUtil    gameDockFactoryUtil;
    @Resource
    private MemberInfoMapper       memberInfoMapper;
    @Resource
    private GamePlatformMapper     gamePlatformMapper;
    @Resource
    private GameInfoMapper         gameInfoMapper;
    @Resource
    private MemberGameMoneyMapper  memberGameMoneyMapper;
    @Resource
    private MemberGameMoneyService memberGameMoneyService;


    @Value( "${spring.profiles.active}" )
    private String profile;
    @Value( "${gameOrderPrefix:0}" )
    private int    gameOrderPrefix;

    @Resource
    private ForkJoinPool forkJoinPool;

    @Override
    public RspGameTypes getGameTypes( String version ) {
        List<RspGameType> gameTypeList  = gameCacheUtils.getEffectTypeList();
        boolean           hasNewVersion = AppVersionUtils.hasNewVersion( "2.1.13.0", version );
        gameTypeList.removeIf( rspGameType -> hasNewVersion ? Arrays.asList( 2L, 4L ).contains( rspGameType.getId() ) : Arrays
                .asList( 8L, 9L ).contains( rspGameType.getId() ) );
        for ( RspGameType rspGameType : gameTypeList ) {
            if ( StringUtils.isNotBlank( rspGameType.getIcon() ) && !rspGameType.getIcon().startsWith( "http" ) ) {
                rspGameType.setIcon( ConfigDomainCacheUtil.me.getDomainOssValue() + rspGameType.getIcon() );
            }
            if ( StringUtils.isNotBlank( rspGameType.getName() ) && rspGameType.getName().contains( "-" ) ) {
                rspGameType.setName( rspGameType.getName().replaceAll( "-", "" ) );
            }
        }
        RspGameTypes rspGameTypes = new RspGameTypes();
        rspGameTypes.setRspGameTypes( gameTypeList );
        if ( !CollectionUtils.isEmpty( gameTypeList ) ) {
            Long typeId = gameTypeList.get( 0 ).getId();
            rspGameTypes.setRspGameInfos( hasNewVersion ? null : gameCacheUtils.getEffectInfoList( typeId ) );
        }
        return rspGameTypes;
    }

    @Override
    public List<RspGameInfo> getGameInfoList( Long typeId ) {
        return gameCacheUtils.getEffectInfoList( typeId );
    }

    @Override
    public List<RspGameInfo> getGameInfos( Long typeId, Long platformId ) {
        if ( Objects.equals( platformId, -1L ) ) {
            return gameInfoMapper.selectHotRspList( typeId );
        }
        return gameCacheUtils.getEffectInfoList( typeId, platformId );
    }

    @Override
    public RspBase<List<RspGamePlatform>> getGameInfoGroup( Long infoTypeId ) {
        List<RspGameType> gameTypeList = gameCacheUtils.getEffectTypeList();
        RspGameType       rspGameType_ = null;
        for ( RspGameType rspGameType : gameTypeList ) {
            if ( Objects.equals( rspGameType.getId(), infoTypeId ) ) {
                rspGameType_ = rspGameType;
            }
        }
        if ( rspGameType_ == null ) {
            return RspBase.businessError( "未知的游戏分类" );
        }
        if ( !Arrays.asList( 3, 4 ).contains( rspGameType_.getType() ) ) {
            return RspBase.businessError( "游戏显示类型错误" );
        }

        List<RspGamePlatform> rspGamePlatformList = new ArrayList<>();
        if ( rspGameType_.getType() == 4 ) {
            RspGamePlatform hotRsp = new RspGamePlatform();
            hotRsp.setId( -1L );
            hotRsp.setName( "热门电子" );
            hotRsp.setCardIcon( "/8800/default/c88c2b13b480ff521d78ac3ca81a2908.png" );
            hotRsp.setIcon( "/8800/default/c88c2b13b480ff521d78ac3ca81a2908.png" );
            rspGamePlatformList.add( hotRsp );
        }
        rspGamePlatformList.addAll( gamePlatformMapper.selectRspList( infoTypeId ) );
        if ( !CollectionUtils.isEmpty( rspGamePlatformList ) ) {
            for ( RspGamePlatform rspGamePlatform : rspGamePlatformList ) {
                if ( StringUtils.isNotBlank( rspGamePlatform.getIcon() ) && !rspGamePlatform.getIcon().startsWith( "http" ) ) {
                    rspGamePlatform.setIcon( ConfigDomainCacheUtil.me.getDomainOssValue() + rspGamePlatform.getIcon() );
                }
                if ( StringUtils.isNotBlank( rspGamePlatform.getCardIcon() ) && !rspGamePlatform.getCardIcon()
                                                                                                .startsWith( "http" ) ) {
                    rspGamePlatform.setCardIcon( ConfigDomainCacheUtil.me.getDomainOssValue() + rspGamePlatform.getCardIcon() );
                }
            }
        }
        return RspBase.ok( rspGamePlatformList );
    }

    @Override
    public RspBase<?> joinGame( Long infoId, PlatformUser platformUser, Integer dev ) {
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

        if ( !redisUtils.lock( "joinGame" + platformUser.getId(), 20 ) ) {
            return RspBase.businessError( "正在进入游戏中.." );
        }

        BigDecimal userBalance = memberInfoMapper.getUserBalance( platformUser.getId() );
        BigDecimal changeMoney = userBalance.setScale( 0, RoundingMode.DOWN );
        // 测试号最多上分100块
        if ( platformUser.getStatus() == 2 && changeMoney.compareTo( ONE_HUNDRED ) > 0 ) {
            changeMoney = ONE_HUNDRED;
        }
        if ( changeMoney.compareTo( BigDecimal.ZERO ) < 0 ) {
            changeMoney = BigDecimal.ZERO;
        }

        ReqJoinGame  reqJoinGame  = this.createReqJoinGame( gamePlatform, gameInfo, platformUser.getId(), changeMoney, dev );
        BaseGameDock baseGameDock = gameDockFactoryUtil.createGameDockProcessor( gamePlatform.getGameCategory() );
        try {
            if ( gamePlatform.getGameCategory() == EnumGameCategory.CQ9 ) {
                // 创建账号
                baseGameDock.createAccount( reqJoinGame );
            }
            // 获取token
            baseGameDock.getToken( reqJoinGame );
            if ( gamePlatform.getGameCategory() != EnumGameCategory.CQ9 ) {
                // 创建账号
                baseGameDock.createAccount( reqJoinGame );
            }
            // 获取游戏链接
            baseGameDock.getJoinGameUrl( reqJoinGame );
            if ( changeMoney.compareTo( BigDecimal.ZERO ) > 0 ) {
                // 扣除会员金额
                memberGameMoneyService.beginGameEnter( reqJoinGame );
                // 设置为上分操作
                reqJoinGame.setMoneyType( 1 );
                // 上分
                baseGameDock.transferMoney( reqJoinGame );
            }
            memberGameMoneyService.enterGameSuccess( reqJoinGame );
            redisUtils.unLock( "joinGame" + platformUser.getId() );
            return RspBase.ok( "获取游戏链接成功", reqJoinGame.getGameUrl() );
        } catch ( Exception e ) {
            // 如果发生转账异常
            if ( e instanceof GameTransferException ) {
                // 查询转账记录
                if ( baseGameDock.queryTransfer( reqJoinGame ) ) {
                    memberGameMoneyService.enterGameSuccess( reqJoinGame );
                    redisUtils.unLock( "joinGame" + platformUser.getId() );
                    return RspBase.ok( "获取游戏链接成功", reqJoinGame.getGameUrl() );
                } else {
                    // 如果转账记录不存在则回退会员上分金额
                    memberGameMoneyService.enterGameFail( reqJoinGame );
                }
            }
            log.error( "进入游戏失败,失败原因:" + e.getMessage(), e );
            redisUtils.unLock( "joinGame" + platformUser.getId() );
            return RspBase.businessError( "进入游戏失败,请重试" );
        }

    }

    @Override
    public RspBase<?> escGame( Long infoId, String memberId ) {
        GameInfo gameInfo = gameCacheUtils.getGameInfo( infoId );
        if ( gameInfo == null ) {
            return RspBase.businessError( "该游戏不存在" );
        }
        return this.gameWithdrawal( memberId, gameInfo.getPlatformId() );
    }

    private RspBase<Object> gameWithdrawal( String memberId, Long platformId ) {
        GamePlatform gamePlatform = gameCacheUtils.getGamePlatform( platformId );
        if ( gamePlatform == null ) {
            return RspBase.businessError( "该游戏不存在" );
        }
        if ( gamePlatform.getMaintain() ) {
            return RspBase.businessError( "该游戏正在维护,暂停游戏转账功能" );
        }

        if ( !redisUtils.lock( "escGame" + memberId, 10 ) ) {
            return RspBase.businessError( "操作频繁,请稍后再试" );
        }
        ReqJoinGame  reqJoinGame  = this.createReqJoinGame( gamePlatform, null, memberId, null, null );
        BaseGameDock baseGameDock = gameDockFactoryUtil.createGameDockProcessor( gamePlatform.getGameCategory() );
        try {
            // 获取token
            if ( gamePlatform.getGameCategory() != EnumGameCategory.BBIN
                    && gamePlatform.getGameCategory() != EnumGameCategory.GAMING_365 ) {
                baseGameDock.getToken( reqJoinGame );
            }
            // 设置为下分操作
            reqJoinGame.setMoneyType( 2 );
            BigDecimal balance = baseGameDock.queryBalance( reqJoinGame );
            // 金额高于0元才下分
            if ( balance.compareTo( BigDecimal.ZERO ) <= 0 ) {
                return RspBase.ok( "游戏余额为0，无需下分" );
            }
            reqJoinGame.setTransferMoney( balance );
            baseGameDock.withdrawal( reqJoinGame );
            memberGameMoneyService.outGameSuccess( reqJoinGame );
            return RspBase.ok( "下分成功" );
        } catch ( Exception e ) {
            // 如果发生提现异常
            if ( e instanceof GameTransferException ) {
                if ( baseGameDock.queryTransfer( reqJoinGame ) ) {
                    memberGameMoneyService.outGameSuccess( reqJoinGame );
                    return RspBase.ok( "下分成功" );
                } else {
                    memberGameMoneyService.outGameFail( reqJoinGame );
                }
            }
            log.error( "人工下分失败,失败原因:" + e.getMessage(), e );
            redisUtils.unLock( "escGame" + memberId );
            return RspBase.businessError( "下分失败,请重试" );
        }
    }

    @Override
    public RspBase<List<RspGameMoney>> getGameBalance( String memberId ) {
        Set<Long> platformIds = new QueryChainWrapper<>( memberGameMoneyMapper ).eq( "member_id", memberId )
                                                                                .ge( "create_time", LocalDateTime.now()
                                                                                                                 .minusMonths( 1 ) )
                                                                                .select( "platform_id", "order_id" ).list()
                                                                                .stream().map( MemberGameMoney::getPlatformId )
                                                                                .collect( Collectors.toSet() );
        if ( CollectionUtils.isEmpty( platformIds ) ) {
            return RspBase.ok( new ArrayList<>() );
        }
        List<GamePlatform>          gamePlatforms = gamePlatformMapper.selectBatchIds( platformIds );
        Set<Callable<RspGameMoney>> forkJoinTasks = new HashSet<>();
        for ( GamePlatform gamePlatform : gamePlatforms ) {
            ReqJoinGame  reqJoinGame  = this.createReqJoinGame( gamePlatform, null, memberId, null, null );
            BaseGameDock baseGameDock = gameDockFactoryUtil.createGameDockProcessor( gamePlatform.getGameCategory() );
            forkJoinTasks.add( () -> {
                BigDecimal balance = null;
                try {
                    // 获取token
                    if ( gamePlatform.getGameCategory() != EnumGameCategory.BBIN
                            && gamePlatform.getGameCategory() != EnumGameCategory.GAMING_365 ) {
                        baseGameDock.getToken( reqJoinGame );
                    }
                    balance = baseGameDock.queryBalance( reqJoinGame );
                } catch ( Exception e ) {
                    log.error( "查询游戏余额异常:" + e.getMessage(), e );
                    balance = new BigDecimal( -1 );
                }
                RspGameMoney rspGameMoney = new RspGameMoney();
                rspGameMoney.setMoney( balance );
                rspGameMoney.setPlatformId( gamePlatform.getId() );
                rspGameMoney.setPlatformName( gamePlatform.getName() );
                return rspGameMoney;
            } );
        }
        List<Future<RspGameMoney>> futureList = forkJoinPool.invokeAll( forkJoinTasks );
        List<RspGameMoney> resultList = futureList.stream().map( t -> {
            try {
                return t.get();
            } catch ( InterruptedException | ExecutionException e ) {
                throw new IllegalStateException( e );
            }
        } ).filter( Objects::nonNull ).collect( Collectors.toList() );
        return RspBase.ok( resultList );
    }

    @Override
    public RspBase<?> gameWithdrawal( Long platformId, String memberId ) {
        GamePlatform gamePlatform = gamePlatformMapper.selectById( platformId );
        if ( gamePlatform == null ) {
            return RspBase.businessError( "游戏平台不存在" );
        }
        return this.gameWithdrawal( memberId, platformId );
    }

    @Override
    public RspBase<String> getGameTokenByAgent( String agent, String gameCategory ) {
        GamePlatform gamePlatform = new QueryChainWrapper<>( gamePlatformMapper ).eq( "agent", agent )
                                                                                 .eq( "game_category", gameCategory ).one();
        if ( gamePlatform == null ) {
            return RspBase.businessError( "游戏平台不存在" );
        }
        if ( !redisUtils.exists( Constants.GAME_TOKEN_PREX + gamePlatform.getId() ) ) {
            ReqJoinGame  reqJoinGame  = this.createReqJoinGame( gamePlatform, null, null, null, null );
            BaseGameDock baseGameDock = gameDockFactoryUtil.createGameDockProcessor( gamePlatform.getGameCategory() );
            baseGameDock.getToken( reqJoinGame );
            return RspBase.ok( "", reqJoinGame.getToken() );
        }
        return RspBase.ok( "", redisUtils.strGet( Constants.GAME_TOKEN_PREX + gamePlatform.getId() ) );
    }

    private ReqJoinGame createReqJoinGame( GamePlatform gamePlatform, GameInfo gameInfo, String memberId,
                                           BigDecimal changeMoney, Integer dev ) {
        // BBIN会员ID只能是英文加数字
        String gameMemberId = switch ( gamePlatform.getGameCategory() ) {
            case BBIN -> profile + "BBIN" + memberId;
            case GAMING_365 -> ( profile + "_" + memberId ).toLowerCase();
            case BOLE, JDB -> ( profile + memberId ).toLowerCase();
            case HG -> AESCoder.decrypt( gamePlatform.getDes() ) + gamePlatform.getAgent() + "_" + profile + "_" + memberId;
            default -> profile + "_" + memberId;
        };
        return ReqJoinGame.builder().des( AESCoder.decrypt( gamePlatform.getDes() ) )
                          .md5( AESCoder.decrypt( gamePlatform.getMd5() ) ).agent( gamePlatform.getAgent() )
                          .apiUrl( gamePlatform.getApiUrl() ).recordUrl( gamePlatform.getRecordUrl() )
                          .linecode( gamePlatform.getLinecode() ).kindId( gameInfo == null ? null : gameInfo.getKindId() )
                          .gameMemberId( gameMemberId ).memberId( memberId ).transferMoney( changeMoney )
                          .platformId( gamePlatform.getId() ).platformName( gamePlatform.getName() )
                          .orderId( this.getGameOrderId( gameMemberId, gamePlatform.getAgent(), gamePlatform ) )
                          .ip( ServletUtil.getIp() ).gameCategory( gamePlatform.getGameCategory() ).dev( dev ).build();
    }

    private String getGameOrderId( String gameMemberId, String agent, GamePlatform gamePlatform ) {
        return switch ( gamePlatform.getGameCategory() ) {
            case AG, BBIN, BG, XINGYUN, JDB, FG, RICH88 -> this.getGameAtomicId( gamePlatform.getId() );
            case MEITIAN -> agent
                    .concat( LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER ) )
                    .concat( gameMemberId.replaceAll( "_", "" ) );
            case HG -> AESCoder.decrypt( gamePlatform.getDes() )
                               .concat( LocalDateTimeUtils.format( LocalDateTime.now(),
                                       LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER ) )
                               .concat( RandomStringUtils.randomAlphabetic( 5 ) );
            case WALI -> String.join( "_", agent, LocalDateTimeUtils.format( LocalDateTime.now(),
                    LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER ), gameMemberId );
            default -> agent
                    .concat( LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER ) )
                    .concat( gameMemberId );
        };
    }

    private String getGameAtomicId( Long platformId ) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(
                Constants.GAME_ATOMIC_PREX + platformId, redisUtils.getConnectionFactory() );
        return gameOrderPrefix + ( platformId <= 9 ? "0" + platformId : platformId + "" ) + ( Constants.GAME_ATOMIC_INIT
                                                                                                      + entityIdCounter.getAndIncrement() );
    }

    public List<RspGameDataLog> remoteDataGrab( String start, String end, String account, List<Integer> platformIds ) {
        Map<String, Object> map = new HashMap<>();
        map.put( "agent", profile );
        map.put( "account", account );
        map.put( "platformIds", platformIds );
        map.put( "startTime", start );
        map.put( "endTime", end );
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>( map, httpHeaders );

        List<RspGameDataLog> rspGameDataLogs = new ArrayList<>();
        List<String> hosts2 = Arrays.asList( "http://18.167.242.177:18850", "http://16.163.247.190:18850" );

        List<Map<String, Object>> resultList2 = restTemplate.postForObject(
                hosts2.get( RandomUtils.randomIntWithMax( 0, 1 ) ) + "/gameDataRecord/getList", httpEntity, List.class );
        if ( !CollectionUtils.isEmpty( resultList2 ) ) {
            for ( Map<String, Object> resultMap : resultList2 ) {
                rspGameDataLogs.add( JsonUtil.map2Object( resultMap, RspGameDataLog.class ) );
            }
        }
        return rspGameDataLogs;
    }

    @Override
    public RspBase<?> verify( String traceId, ReqPGSoftGameData data ) {
        String ot  = redisUtils.strGet( Constants.GAME_PGSOFT_OT + data.getCustom_parameter() );
        String key = redisUtils.strGet( Constants.GAME_PGSOFT_KEY + data.getCustom_parameter() );
        String ops = redisUtils.strGet( Constants.GAME_PGSOFT_OPS + data.getCustom_parameter() );
        log.info( "PGSoft Verify: trace_id - {}, data - {}, system - {}", traceId, data.toString(), String.format(
                "Ot: %s, " + "Key: %s, Ops: %s", ot, key, ops ) );
        if ( StringUtils.isEmpty( ot ) || StringUtils.isEmpty( key ) || StringUtils.isEmpty( ops ) ) {
            return createResponse( 1200, null, Map.of( "code", "500", "message", "Required field missing" ) );
        } else {
            boolean isOt  = ot.equals( data.getOperator_token() );
            boolean isKey = key.equals( data.getSecret_key() );
            boolean isOps = ops.equals( data.getOperator_player_session() );
            if ( isOt && isKey && isOps ) {
                Map<String, String> successMap = Map.of( "player_name", data.getCustom_parameter(), "currency", "CNY" );
                return createResponse( SC_OK, successMap, null );
            } else {
                return createResponse( 1034, null, Map.of( "code", "400", "message", "One of required fields not equal" ) );
            }
        }
    }

    private static RspBase<RspPGSoftGameData> createResponse( int rspCode, Map<String, String> data, Map<String, String> error ) {
        RspPGSoftGameData          rspData  = RspPGSoftGameData.builder().data( data ).error( error ).build();
        RspBase<RspPGSoftGameData> response = new RspBase<>();
        response.setCode( rspCode );
        response.setData( rspData );
        return response;
    }
}
