package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
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
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.game.api.base.BaseGameButt;
import tv.game88.game.api.base.GameButtFactoryUtil;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.dto.*;
import tv.game88.game.api.entity.*;
import tv.game88.game.api.exception.GameTransferException;
import tv.game88.game.api.mapper.GameInfoMapper;
import tv.game88.game.api.mapper.GamePlatformMapper;
import tv.game88.game.api.mapper.MemberGameDataMapper;
import tv.game88.game.api.mapper.MemberGameMoneyMapper;
import tv.game88.game.api.service.GameService;
import tv.game88.game.api.service.MemberGameDataService;
import tv.game88.game.api.service.MemberGameMoneyService;
import tv.game88.game.api.type.EnumGameCategory;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.SC_OK;

@Log4j2
@Service
public class GameServiceImpl extends ServiceImpl<MemberGameDataMapper, MemberGameData> implements GameService {

    public static final BigDecimal ONE_HUNDRED = new BigDecimal( 100 );

    @Resource
    private RestTemplate           restTemplate;
    @Resource
    private RedisUtils             redisUtils;
    @Resource
    private GameCacheUtils         gameCacheUtils;
    @Resource
    private GameButtFactoryUtil    gameButtFactoryUtil;
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
        BaseGameButt baseGameButt = gameButtFactoryUtil.createGameButtProcessor( gamePlatform.getGameCategory() );
        try {
            if ( gamePlatform.getGameCategory() == EnumGameCategory.CQ9 ) {
                // 创建账号
                baseGameButt.createAccount( reqJoinGame );
            }
            // 获取token
            baseGameButt.getToken( reqJoinGame );
            if ( gamePlatform.getGameCategory() != EnumGameCategory.CQ9 ) {
                // 创建账号
                baseGameButt.createAccount( reqJoinGame );
            }
            // 获取游戏链接
            baseGameButt.getJoinGameUrl( reqJoinGame );
            if ( changeMoney.compareTo( BigDecimal.ZERO ) > 0 ) {
                // 扣除会员金额
                memberGameMoneyService.beginGameEnter( reqJoinGame );
                // 上分
                baseGameButt.transferMoney( reqJoinGame );
            }
            memberGameMoneyService.enterGameSuccess( reqJoinGame );
            redisUtils.unLock( "joinGame" + platformUser.getId() );
            return RspBase.ok( "获取游戏链接成功", reqJoinGame.getGameUrl() );
        } catch ( Exception e ) {
            // 如果发生转账异常
            if ( e instanceof GameTransferException ) {
                reqJoinGame.setMoneyType( 1 );
                // 查询转账记录
                if ( baseGameButt.queryTransfer( reqJoinGame ) ) {
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

    @NotNull
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
        BaseGameButt baseGameButt = gameButtFactoryUtil.createGameButtProcessor( gamePlatform.getGameCategory() );
        try {
            // 获取token
            if ( gamePlatform.getGameCategory() != EnumGameCategory.BBIN
                    && gamePlatform.getGameCategory() != EnumGameCategory.GAMING_365 ) {
                baseGameButt.getToken( reqJoinGame );
            }
            BigDecimal balance = baseGameButt.queryBalance( reqJoinGame );
            // 金额高于0元才下分
            if ( balance.compareTo( BigDecimal.ZERO ) <= 0 ) {
                return RspBase.ok( "游戏余额为0，无需下分" );
            }
            reqJoinGame.setTransferMoney( balance );
            baseGameButt.withdrawal( reqJoinGame );
            memberGameMoneyService.outGameSuccess( reqJoinGame );
            return RspBase.ok( "下分成功" );
        } catch ( Exception e ) {
            // 如果发生提现异常
            if ( e instanceof GameTransferException ) {
                reqJoinGame.setMoneyType( 2 );
                if ( baseGameButt.queryTransfer( reqJoinGame ) ) {
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
            BaseGameButt baseGameButt = gameButtFactoryUtil.createGameButtProcessor( gamePlatform.getGameCategory() );
            forkJoinTasks.add( () -> {
                BigDecimal balance = null;
                try {
                    // 获取token
                    if ( gamePlatform.getGameCategory() != EnumGameCategory.BBIN
                            && gamePlatform.getGameCategory() != EnumGameCategory.GAMING_365 ) {
                        baseGameButt.getToken( reqJoinGame );
                    }
                    balance = baseGameButt.queryBalance( reqJoinGame );
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
            BaseGameButt baseGameButt = gameButtFactoryUtil.createGameButtProcessor( gamePlatform.getGameCategory() );
            baseGameButt.getToken( reqJoinGame );
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
                          .platformId( gamePlatform.getId() )
                          .orderId( this.getGameOrderId( gameMemberId, gamePlatform.getAgent(), gamePlatform ) )
                          .ip( ServletUtil.getIp() ).gameCategory( gamePlatform.getGameCategory() ).dev( dev ).build();
    }

    private String getGameOrderId( String gameMemberId, String agent, GamePlatform gamePlatform ) {
        return switch ( gamePlatform.getGameCategory() ) {
            case AG, BBIN, BG, XINGYUN, JDB -> this.getGameAtomicId( gamePlatform.getId() );
            case MEITIAN -> agent
                    .concat( LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER ) )
                    .concat( gameMemberId.replaceAll( "_", "" ) );
            case HG -> AESCoder.decrypt( gamePlatform.getDes() )
                               .concat( LocalDateTimeUtils.format( LocalDateTime.now(),
                                       LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER ) )
                               .concat( RandomStringUtils.randomAlphabetic( 5 ) );
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

        List<String> hosts = Arrays.asList( "http://47.57.4.185:42000", "http://47.57.230.133:42000" );

        List<Map<String, Object>> resultList = restTemplate.postForObject(
                hosts.get( RandomUtils.randomIntWithMax( 0, 1 ) ) + "/game-data-log/getDataByAgent", httpEntity, List.class );
        if ( !CollectionUtils.isEmpty( resultList ) ) {
            List<RspGameDataLog> rspGameDataLogs = new ArrayList<>();
            for ( Map<String, Object> resultMap : resultList ) {
                rspGameDataLogs.add( JsonUtil.map2Object( resultMap, RspGameDataLog.class ) );
            }
            return rspGameDataLogs;
        }
        return new ArrayList<>();
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

    @Override
    public RspBase<RspCleanCodeInfo> cleanCodeDetail( String memberId ) {
        RspCleanCodeInfo info = new RspCleanCodeInfo();
        info.setCleanAmountTotal( BigDecimal.ZERO );
        info.setCodeAmountTotal( BigDecimal.ZERO );
        List<RspGameCategory> gameCategorys = EnumGameCategory.getGameCategorys();
        info.setRspGameCategoryList( gameCategorys );
        MemberInfo memberInfo = new QueryChainWrapper<>( memberInfoMapper )
                .eq( "id", memberId )
                .select( "id", "clean_time" )
                .one();
        if ( memberInfo.getCleanTime() != null ) {
            info.setCleanTime( LocalDateTimeUtils.format( memberInfo.getCleanTime() ) );
        }
        Map<Long, RspCleanPlatform> map = this.baseMapper
                .findMemCleanPlatformLists( memberId.substring( memberId.length() - 1 ), memberId )
                .stream()
                .collect( Collectors.toMap( RspCleanPlatform::getId, Function.identity() ) );
        Map<String, RspGameCategory> gameCategoryMap = gameCategorys
                .stream()
                .collect( Collectors.toMap( RspGameCategory::getName, Function.identity() ) );
        for ( GamePlatform pl : gameCacheUtils.getGamePlatformList() ) {
            if ( map.containsKey( pl.getId() ) ) {
                map.get( pl.getId() ).setRateClean( pl.getRateClean() );
                map.get( pl.getId() ).setName( pl.getName() );
            } else {
                RspCleanPlatform cp = new RspCleanPlatform();
                cp.setId( pl.getId() );
                cp.setName( pl.getName() );
                cp.setRateClean( pl.getRateClean() );
                map.put( pl.getId(), cp );
            }
            if ( gameCategoryMap.containsKey( pl.getGameCategory().name() ) ) {
                gameCategoryMap.get( pl.getGameCategory().name() ).getPlatforms().add( map.get( pl.getId() ) );
            }
        }
        for ( RspCleanPlatform e : map.values() ) {
            //此游戏平台无注单可洗码
            if ( e.getCodeAmount().compareTo( BigDecimal.ZERO ) == 0 ) {
                continue;
            }
            e.setCleanAmount( e.getCodeAmount().multiply( e.getRateClean() ) );
            info.setCodeAmountTotal( info.getCodeAmountTotal().add( e.getCodeAmount() ) );
            info.setCleanAmountTotal( info.getCleanAmountTotal().add( e.getCleanAmount() ) );
        }
        info.setCodeAmountTotal( info.getCodeAmountTotal().setScale( 2, RoundingMode.HALF_UP ) );
        info.setCleanAmountTotal( info.getCleanAmountTotal().setScale( 2, RoundingMode.HALF_UP ) );
        return RspBase.ok( info );
    }

    @Override
    public RspBase<RspCleanCodeInfo> cleanCode( String memberId ) {
        if ( !redisUtils.lock( "cleanCode" + memberId, 5 ) ) {
            return RspBase.businessError( "请勿重复请求" );
        }

        this.toCleanCode( memberId );

        redisUtils.unLock( "cleanCode" + memberId );
        RspBase<RspCleanCodeInfo> rspBase = this.cleanCodeDetail( memberId );
        rspBase.getData().setMoney( memberInfoMapper.getUserBalance( memberId ) );
        return rspBase;
    }

    private void toCleanCode( String memberId ) {
        Map<Long, RspCleanPlatform> willCleanPlatforms = this.baseMapper
                .findMemCleanPlatformLists( memberId.substring( memberId.length() - 1 ), memberId )
                .stream()
                .collect( Collectors.toMap( RspCleanPlatform::getId, Function.identity() ) );
        // 无可洗码注单 返回
        if ( willCleanPlatforms.size() == 0 ) {
            return;
        }
        // 本次洗码日志容器
        Map<Long, LogCleanCodeInfo> logsMap = new HashMap<>();
        // 查询可洗码游戏平台
        Map<Long, GamePlatform> map = gameCacheUtils
                .getGamePlatformList()
                .stream()
                .collect( Collectors.toMap( GamePlatform::getId, Function.identity() ) );

        String             cleanId = IdWorker.get32UUID();
        LocalDateTime      ntime   = LocalDateTime.now();
        RspCleanCodeResult restlt  = new RspCleanCodeResult();
        for ( Long platformId : willCleanPlatforms.keySet() ) {
            if ( !map.containsKey( platformId ) ) {
                continue;
            }
            //单个洗码比例
            BigDecimal rate = map.get( platformId ).getRateClean();
            if ( rate == null || rate.compareTo( BigDecimal.ZERO ) <= 0 ) {
                continue;
            }
            //单个洗码量
            BigDecimal singleCodeAmount = willCleanPlatforms.get( platformId ).getCodeAmount();
            //单个洗码金额
            BigDecimal singleCleanAmount = singleCodeAmount.multiply( rate );

            if ( logsMap.containsKey( platformId ) ) {
                logsMap.get( platformId ).setCodeAmount( logsMap.get( platformId ).getCodeAmount().add( singleCodeAmount ) );
                logsMap.get( platformId ).setCleanAmount( logsMap.get( platformId ).getCleanAmount().add( singleCleanAmount ) );
            } else {
                LogCleanCodeInfo cleanlog = new LogCleanCodeInfo();
                cleanlog.setId( IdWorker.get32UUID() );
                cleanlog.setMemberId( memberId );
                cleanlog.setCodeAmount( singleCodeAmount );
                cleanlog.setCleanAmount( singleCleanAmount );
                cleanlog.setName( map.get( platformId ).getName() );
                cleanlog.setCleanId( cleanId );
                cleanlog.setRateClean( rate );
                cleanlog.setCleanTime( ntime );
                logsMap.put( platformId, cleanlog );
            }
            restlt.setAddCodeAmount( restlt.getAddCodeAmount().add( singleCodeAmount ) );
            restlt.setAddCleanAmount( restlt.getAddCleanAmount().add( singleCleanAmount ) );
        }
        if ( restlt.getAddCleanAmount().compareTo( new BigDecimal( 1 ) ) < 0 ) {
            return;
        }
        SpringUtils.getBean( MemberGameDataService.class ).opCleanCode( memberId, restlt, logsMap.values(), cleanId, ntime );
    }

}
