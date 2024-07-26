package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.game.type.EnumGameCategory;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.core.utils.TelegramBotMessage;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class GameServiceImpl implements GameService {

    public static final BigDecimal ONE_HUNDRED = new BigDecimal( 100 );

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
    @Resource
    private TelegramBotMessage     telegramBotMessage;
    @Resource
    private ConfigEnvCacheUtil     configEnvCacheUtil;


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
        gameTypeList.removeIf( rspGameType -> hasNewVersion ? Arrays.asList( 2L, 4L )
                .contains( rspGameType.getId() ) : Arrays.asList( 8L, 9L )
                .contains( rspGameType.getId() ) );
        for ( RspGameType rspGameType : gameTypeList ) {
            if ( StringUtils.isNotBlank( rspGameType.getIcon() ) && !rspGameType.getIcon()
                    .startsWith( "http" ) ) {
                rspGameType.setIcon( ConfigDomainCacheUtil.me.getDomainOssValue() + rspGameType.getIcon() );
            }
            if ( StringUtils.isNotBlank( rspGameType.getName() ) && rspGameType.getName()
                    .contains( "-" ) ) {
                rspGameType.setName( rspGameType.getName()
                        .replaceAll( "-", "" ) );
            }
        }
        RspGameTypes rspGameTypes = new RspGameTypes();
        rspGameTypes.setRspGameTypes( gameTypeList );
        if ( !CollectionUtils.isEmpty( gameTypeList ) ) {
            Long typeId = gameTypeList.getFirst()
                    .getId();
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
        if ( !Arrays.asList( 3, 4 )
                .contains( rspGameType_.getType() ) ) {
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
                if ( StringUtils.isNotBlank( rspGamePlatform.getIcon() ) && !rspGamePlatform.getIcon()
                        .startsWith( "http" ) ) {
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

        if ( !redisUtils.lock( "joinGame" + platformUser.getId(), 100 ) ) {
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

        try {
            ReqJoinGame reqJoinGame = this.createReqJoinGame( gamePlatform, gameInfo, platformUser.getId(), changeMoney, dev,
                    ServletUtil.getIp() );
            BaseGameDock baseGameDock = gameDockFactoryUtil.createGameDockProcessor( gamePlatform.getGameCategory() );
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

            if ( gamePlatform.getGameCategory() == EnumGameCategory.PG_SOFT ) {
                this.topUpGame( reqJoinGame, baseGameDock );// PG执行上分任务
            } else {
                Thread.ofVirtual()
                        .start( () -> this.topUpGame( reqJoinGame, baseGameDock ) );
            }

            log.info( reqJoinGame.getGameCategory()
                    .getDes() + "获取游戏链接成功:{}; userId:{}", reqJoinGame.getGameUrl(), reqJoinGame.getGameMemberId() );

            return RspBase.ok( "获取游戏链接成功", reqJoinGame.getGameUrl() );
        } catch ( Exception e ) {
            log.error( "gameId:{}, userId:{}, 进入游戏失败,失败原因:{}", infoId, platformUser.getId(), e.getMessage(), e );
            redisUtils.unLock( "joinGame" + platformUser.getId() );
            return RspBase.businessError( "进入游戏失败,请重试" );
        }
    }

    public void topUpGame( ReqJoinGame reqJoinGame, BaseGameDock baseGameDock ) {
        // 设置为上分操作
        reqJoinGame.setMoneyType( 1 );
        if ( reqJoinGame.getTransferMoney()
                .compareTo( BigDecimal.ZERO ) > 0 ) {
            // 扣除会员金额
            memberGameMoneyService.beginGameEnter( reqJoinGame );
            boolean success = false;
            try {
                // 上分
                baseGameDock.transferMoney( reqJoinGame );
                success = true;
            } catch ( Exception e ) {
                if ( e instanceof GameTransferException ) {
                    // 查询转账记录
                    if ( this.queryTransfer( reqJoinGame, baseGameDock ) ) {
                        success = true;
                    } else {
                        // 如果转账记录不存在则回退会员上分金额
                        memberGameMoneyService.enterGameFail( reqJoinGame );
                    }
                } else {
                    log.error( "会员{}上分失败,失败原因:{}", reqJoinGame.getMemberId(), e.getMessage(), e );
                    this.processTopUpNotice( reqJoinGame );
                }
            }
            if ( success ) {
                memberGameMoneyService.enterGameSuccess( reqJoinGame );
            }
        }
        redisUtils.unLock( "joinGame" + reqJoinGame.getMemberId() );
    }

    private boolean queryTransfer( ReqJoinGame reqJoinGame, BaseGameDock baseGameDock ) {
        boolean success;
        try {
            success = baseGameDock.queryTransfer( reqJoinGame );
        } catch ( Exception e ) {
            log.error( "会员{}查询转账失败 - 失败原因: {}", reqJoinGame.getMemberId(), e.getMessage(), e );
            success = false;

            if ( reqJoinGame.getMoneyType() == 1 ) {
                this.processTopUpNotice( reqJoinGame );
            }
        }
        return success;
    }

    private void processTopUpNotice( ReqJoinGame reqJoinGame ) {
        String gameDes = reqJoinGame.getGameCategory()
                .getDes();
        String msg = String.format( "There was an error transferring money for the member's game. Please attend to the "
                + "member's money loss promptly! ::: gamePlatform: %s ; gameId: %s ; transferOrderId: %s ; "
                + "memberId: %s ; money: %s ; IP: %s", gameDes, reqJoinGame.getGameInfoId(), reqJoinGame.getOrderId(),
                reqJoinGame.getMemberId(), reqJoinGame.getTransferMoney(), reqJoinGame.getIp() );
        telegramBotMessage.sendByChatId( msg, configEnvCacheUtil.getConf( "gametransfer_error_telegram" ) );
    }

    public void cashOutGame( ReqJoinGame reqJoinGame, BaseGameDock baseGameDock ) {
        boolean success = false;
        try {
            baseGameDock.withdrawal( reqJoinGame );
            success = true;
        } catch ( Exception e ) {
            // 如果发生提现异常
            if ( e instanceof GameTransferException ) {
                if ( this.queryTransfer( reqJoinGame, baseGameDock ) ) {
                    success = true;
                } else {
                    memberGameMoneyService.outGameFail( reqJoinGame );
                }
            } else {
                log.error( "会员{}下分失败,失败原因:{}", reqJoinGame.getMemberId(), e.getMessage(), e );
            }
        }
        if ( success ) {
            memberGameMoneyService.outGameSuccess( reqJoinGame );
        }

        redisUtils.unLock( "escGame" + reqJoinGame.getMemberId() );
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
        try {
            ReqJoinGame  reqJoinGame  = this.createReqJoinGame( gamePlatform, null, memberId, null, null, ServletUtil.getIp() );
            BaseGameDock baseGameDock = gameDockFactoryUtil.createGameDockProcessor( gamePlatform.getGameCategory() );
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
            // 异步下分
            Thread.ofVirtual()
                    .start( () -> this.cashOutGame( reqJoinGame, baseGameDock ) );
            return RspBase.ok( "下分成功" );
        } catch ( Exception e ) {
            log.error( "人工下分失败,失败原因:" + e.getMessage(), e );
            redisUtils.unLock( "escGame" + memberId );
            return RspBase.businessError( "下分失败,请重试" );
        }
    }

    @Override
    public RspBase<List<RspGameMoney>> getGameBalance( final String memberId ) {
        Set<Long> platformIds = new QueryChainWrapper<>( memberGameMoneyMapper ).eq( "member_id", memberId )
                .ge( "create_time", LocalDateTime.now()
                        .minusMonths( 1 ) )
                .select( "platform_id", "order_id" )
                .list()
                .stream()
                .map( MemberGameMoney::getPlatformId )
                .collect( Collectors.toSet() );
        if ( CollectionUtils.isEmpty( platformIds ) ) {
            return RspBase.ok( new ArrayList<>() );
        }
        List<GamePlatform>          gamePlatforms = gamePlatformMapper.selectBatchIds( platformIds );
        String                      ip            = ServletUtil.getIp();
        Set<Callable<RspGameMoney>> forkJoinTasks = new HashSet<>();
        for ( final GamePlatform gamePlatform : gamePlatforms ) {
            forkJoinTasks.add( () -> {
                BigDecimal balance = null;
                if ( gamePlatform.getMaintain() ) {
                    RspGameMoney rspGameMoney = new RspGameMoney();
                    rspGameMoney.setMoney( new BigDecimal( -1 ) );
                    rspGameMoney.setPlatformId( gamePlatform.getId() );
                    rspGameMoney.setPlatformName( gamePlatform.getName() );
                    return rspGameMoney;
                }
                try {
                    ReqJoinGame  reqJoinGame  = this.createReqJoinGame( gamePlatform, null, memberId, null, null, ip );
                    BaseGameDock baseGameDock = gameDockFactoryUtil.createGameDockProcessor( gamePlatform.getGameCategory() );
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
        List<RspGameMoney> resultList = futureList.stream()
                .map( t -> {
                    try {
                        return t.get();
                    } catch ( InterruptedException | ExecutionException e ) {
                        throw new IllegalStateException( e );
                    }
                } )
                .filter( Objects::nonNull )
                .collect( Collectors.toList() );
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
    public RspBase<String> getGameTokenByAgent( String agent, String gameCategory ) throws Exception {
        GamePlatform gamePlatform = new QueryChainWrapper<>( gamePlatformMapper ).eq( "agent", agent )
                .eq( "game_category", gameCategory )
                .one();
        if ( gamePlatform == null ) {
            return RspBase.businessError( "游戏平台不存在" );
        }
        if ( !redisUtils.exists( Constants.GAME_TOKEN_PREX + gamePlatform.getId() ) ) {
            ReqJoinGame  reqJoinGame  = this.createReqJoinGame( gamePlatform, null, null, null, null, ServletUtil.getIp() );
            BaseGameDock baseGameDock = gameDockFactoryUtil.createGameDockProcessor( gamePlatform.getGameCategory() );
            baseGameDock.getToken( reqJoinGame );
            return RspBase.ok( "", reqJoinGame.getToken() );
        }
        return RspBase.ok( "", redisUtils.strGet( Constants.GAME_TOKEN_PREX + gamePlatform.getId() ) );
    }

    private ReqJoinGame createReqJoinGame( GamePlatform gamePlatform, GameInfo gameInfo, String memberId,
                                           BigDecimal changeMoney, Integer dev, String ip ) throws Exception {
        // BBIN会员ID只能是英文加数字
        String gameMemberId = switch ( gamePlatform.getGameCategory() ) {
            case BBIN -> profile + "BBIN" + memberId;
            case GAMING_365 -> ( profile + "_" + memberId ).toLowerCase();
            case BOLE, JDB -> ( profile + memberId ).toLowerCase();
            case HG -> AESCoder.decrypt( gamePlatform.getDes() ) + gamePlatform.getAgent() + "_" + profile + "_" + memberId;
            default -> profile + "_" + memberId;
        };
        return ReqJoinGame.builder()
                .des( AESCoder.decrypt( gamePlatform.getDes() ) )
                .md5( AESCoder.decrypt( gamePlatform.getMd5() ) )
                .agent( gamePlatform.getAgent() )
                .apiUrl( gamePlatform.getApiUrl() )
                .recordUrl( gamePlatform.getRecordUrl() )
                .linecode( gamePlatform.getLinecode() )
                .kindId( gameInfo == null ? null : gameInfo.getKindId() )
                .gameInfoId( gameInfo == null ? null : gameInfo.getId() )
                .gameMemberId( gameMemberId )
                .memberId( memberId )
                .transferMoney( changeMoney )
                .platformId( gamePlatform.getId() )
                .platformName( gamePlatform.getName() )
                .orderId( this.getGameOrderId( gameMemberId, gamePlatform.getAgent(), gamePlatform ) )
                .ip( ip )
                .gameCategory( gamePlatform.getGameCategory() )
                .dev( dev )
                .build();
    }

    private String getGameOrderId( String gameMemberId, String agent, GamePlatform gamePlatform ) throws Exception {
        String orderId = switch ( gamePlatform.getGameCategory() ) {
            case AG, BBIN, BG, XINGYUN, JDB, FG, RICH88 -> this.getGameAtomicId( gamePlatform.getId() );
            case MEITIAN -> agent.concat( LocalDateTimeUtils.format( LocalDateTime.now(),
                            LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER ) )
                    .concat( gameMemberId.replaceAll( "_", "" ) );
            case HG -> AESCoder.decrypt( gamePlatform.getDes() )
                    .concat( LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER ) )
                    .concat( RandomStringUtils.randomAlphabetic( 5 ) );
            case WALI -> String.join( "_", agent, LocalDateTimeUtils.format( LocalDateTime.now(),
                    LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER ), gameMemberId );
            default -> agent.concat( LocalDateTimeUtils.format( LocalDateTime.now(),
                            LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER ) )
                    .concat( gameMemberId );
        };
        if ( !redisUtils.strSetIfAbsent( Constants.CONFIG_PREX + "orderId:" + orderId, "", Duration.ofSeconds( 10 ) ) ) {
            return getGameOrderId( gameMemberId, agent, gamePlatform );
        }
        return orderId;
    }

    private String getGameAtomicId( Long platformId ) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(
                Constants.GAME_ATOMIC_PREX + platformId, redisUtils.getConnectionFactory() );
        return gameOrderPrefix + ( platformId <= 9 ? "0" + platformId : platformId + "" ) + ( Constants.GAME_ATOMIC_INIT
                                                                                                      + entityIdCounter.getAndIncrement() );
    }

    @Override
    public String verifyPGSession( ReqPGSoftGameData reqPGSoftGameData ) throws Exception {
        // 45 是PG的游戏平台ID
        GamePlatform gamePlatform = gameCacheUtils.getGamePlatform( 35L );

        Map<String, String> dataMap  = new HashMap<>();
        Map<String, String> errorMap = new HashMap<>();
        if ( Objects.equals( AESCoder.decrypt( gamePlatform.getDes() ), reqPGSoftGameData.getOperatorToken() )
                && Objects.equals( AESCoder.decrypt( gamePlatform.getMd5() ), reqPGSoftGameData.getSecretKey() ) ) {
            String decrypt = AESCoder.decryptByKeyHex( reqPGSoftGameData.getOperatorPlayerSession(), AESCoder.secretKey );
            if ( StringUtils.isNotBlank( decrypt ) ) {
                String[] split        = decrypt.split( "-" );
                String   gameMemberId = split[ 0 ];
                String   time         = split[ 1 ];
                String   currency     = split[ 2 ];

                /*if ( Math.abs( ( Long.parseLong( time ) - System.currentTimeMillis() ) / 1000 ) < 20 ) {
                    errorMap.put( "code", "1034" );
                    errorMap.put( "message", "The interval between request and verification exceeds 20 seconds" );
                }*/

                dataMap.put( "player_name", gameMemberId );
                dataMap.put( "nickname", gameMemberId );
                dataMap.put( "currency", currency );
            } else {
                errorMap.put( "code", "1034" );
                errorMap.put( "message", "Unable to decrypt operatorPlayerSession" );
            }
        } else {
            errorMap.put( "code", "1034" );
            errorMap.put( "message", "operatorToken or secretKey is inconsistent with the server" );
        }
        Map<String, Map<String, String>> resultMap = new HashMap<>();
        if ( errorMap.isEmpty() ) {
            resultMap.put( "data", dataMap );
        } else {
            resultMap.put( "data", null );
        }
        resultMap.put( "error", errorMap.isEmpty() ? null : errorMap );
        String s = JsonUtil.object2Json( resultMap );
        log.warn( "PG请求验证 - 返回结果:{}", s );
        return s;
    }
}
