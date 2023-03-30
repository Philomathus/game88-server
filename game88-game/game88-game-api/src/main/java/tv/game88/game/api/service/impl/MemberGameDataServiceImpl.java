package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.dto.*;
import tv.game88.game.api.entity.*;
import tv.game88.game.api.mapper.*;
import tv.game88.game.api.service.MemberGameDataService;
import tv.game88.game.api.type.EnumGameCategory;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 会员游戏注单数据Service业务层处理
 *
 * @author mengJun
 */
@Log4j2
@Service
public class MemberGameDataServiceImpl extends ServiceImpl<MemberGameDataMapper, MemberGameData> implements MemberGameDataService {
    @Resource
    private MemberInfoMapper       memberInfoMapper;
    @Resource
    private LogCleanCodeMapper     logCleanCodeMapper;
    @Resource
    private LogCleanCodeInfoMapper logCleanCodeInfoMapper;
    @Resource
    private MemberMoneyManager     memberMoneyManager;
    @Resource
    private GamePlatformMapper     gamePlatformMapper;
    @Resource
    private GameInfoMapper         gameInfoMapper;
    @Resource
    private RedisUtils             redisUtils;
    @Resource
    private GameCacheUtils         gameCacheUtils;
    @Resource
    private GameWashCodeLogMapper  gameWashCodeLogMapper;

    private static Pattern NUM_PATTERN = Pattern.compile( "^[-\\+]?[\\d]*$" );

    /**
     * 查询会员游戏注单数据列表
     *
     * @return 会员游戏注单数据
     */
    @Override
    public List<MemberGameData> selectMemberGameDataList( ReqMemberGameData reqMemberGameData ) {
        pingjieReq( reqMemberGameData );
        List<MemberGameData> memberGameData = this.baseMapper.selectMemberGameDataList( reqMemberGameData );
        if ( CollectionUtils.isNotEmpty( memberGameData ) ) {
            Set<Integer> platformIds = memberGameData.stream().map( MemberGameData::getPlatformId ).collect( Collectors.toSet() );
            Set<String>  kindIds     = memberGameData.stream().map( MemberGameData::getKindId ).collect( Collectors.toSet() );
            // 排除 热门游戏/老棋牌游戏/老电子游戏
            List<GameInfo> gameInfos = new QueryChainWrapper<>( gameInfoMapper ).in( "platform_id", platformIds )
                                                                                .in( "kind_id", kindIds ).list();

            for ( MemberGameData memberGameDatum : memberGameData ) {
                GamePlatform gamePlatform = gameCacheUtils.getGamePlatform( memberGameDatum.getPlatformId().longValue() );
                memberGameDatum.setPlatformName( gamePlatform != null ? gamePlatform.getName() : "" );
                for ( GameInfo gameInfo : gameInfos ) {
                    if ( gameInfo.getPlatformId().intValue() == memberGameDatum.getPlatformId() && memberGameDatum.getKindId()
                                                                                                                  .equals( gameInfo.getKindId() ) ) {
                        memberGameDatum.setSonPlatformName( gameInfo.getName() );
                    }
                }
            }
        }
        return memberGameData;
    }

    private void pingjieReq( ReqMemberGameData reqMemberGameData ) {
        if ( reqMemberGameData.getSelectDate() != null ) {
            reqMemberGameData.setStartTime( reqMemberGameData.getSelectDate()[ 0 ] + " 00:00:00" );
            reqMemberGameData.setEndTime( reqMemberGameData.getSelectDate()[ 1 ] + " 23:59:59" );
        }
        if ( reqMemberGameData.getPlatformId() != null && reqMemberGameData.getPlatformId() == 15 ) {
            reqMemberGameData.setAgent( "-1" );
            reqMemberGameData.setPlatformId( null );
        }
        if ( reqMemberGameData.getPlatformIds() != null && reqMemberGameData.getPlatformIds().contains( "15" ) ) {
            reqMemberGameData.getAgents().add( "-1" );
            reqMemberGameData.getPlatformIds().remove( "15" );
        }
        if ( StringUtils.isNotBlank( reqMemberGameData.getAccount() ) ) {
            String tableLast = reqMemberGameData.getAccount().substring( reqMemberGameData.getAccount().length() - 1 );
            if ( NUM_PATTERN.matcher( tableLast ).matches() ) {
                reqMemberGameData.setTableLast( tableLast );
            } else {
                reqMemberGameData.setTableLast( "0" );
            }
        } else {
            reqMemberGameData.setTableLast( "0" );
        }
    }

    @Override
    public MemberGameData getCount( ReqMemberGameData reqMemberGameData ) {
        pingjieReq( reqMemberGameData );
        return this.baseMapper.getCountMemberGameDataList( reqMemberGameData );
    }

    @Override
    public RspBase<RspCleanCodeInfo> cleanCodeDetail( String memberId ) {
        RspCleanCodeInfo info = new RspCleanCodeInfo();
        info.setCleanAmountTotal( BigDecimal.ZERO );
        info.setCodeAmountTotal( BigDecimal.ZERO );
        List<RspGameCategory> gameCategorys = EnumGameCategory.getGameCategorys();
        info.setRspGameCategoryList( gameCategorys );
        MemberInfo memberInfo = new QueryChainWrapper<>( memberInfoMapper ).eq( "id", memberId ).select( "id", "clean_time" )
                                                                           .one();
        if ( memberInfo.getCleanTime() != null ) {
            info.setCleanTime( LocalDateTimeUtils.format( memberInfo.getCleanTime() ) );
        }
        Map<Long, RspCleanPlatform> map = this.baseMapper
                .findMemCleanPlatformLists( memberId.substring( memberId.length() - 1 ), memberId ).stream()
                .collect( Collectors.toMap( RspCleanPlatform::getId, Function.identity() ) );
        Map<String, RspGameCategory> gameCategoryMap = gameCategorys.stream()
                                                                    .collect( Collectors.toMap( RspGameCategory::getName,
                                                                            Function.identity() ) );
        List<GamePlatform> gamePlatforms = new QueryChainWrapper<>( gamePlatformMapper ).list();
        for ( GamePlatform pl : gamePlatforms ) {
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
                .findMemCleanPlatformLists( memberId.substring( memberId.length() - 1 ), memberId ).stream()
                .collect( Collectors.toMap( RspCleanPlatform::getId, Function.identity() ) );
        // 无可洗码注单 返回
        if ( willCleanPlatforms.size() == 0 ) {
            return;
        }
        // 本次洗码日志容器
        Map<Long, LogCleanCodeInfo> logsMap = new HashMap<>();

        List<GamePlatform> gamePlatforms = new QueryChainWrapper<>( gamePlatformMapper ).list();
        // 查询可洗码游戏平台
        Map<Long, GamePlatform> map = gamePlatforms.stream()
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

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void opCleanCode( String memberId, RspCleanCodeResult restlt, Collection<LogCleanCodeInfo> logCleanCodeInfos,
                             String cleanId, LocalDateTime ntime ) {
        this.baseMapper.updateByBatchClean( memberId.substring( memberId.length() - 1 ), memberId );
        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setCleanTime( ntime );
        memberInfoMapper.updateById( update );

        LogCleanCode logCleancode = new LogCleanCode();
        logCleancode.setId( cleanId );
        logCleancode.setMemberId( memberId );
        logCleancode.setCleanTime( ntime );
        logCleancode.setCleanAmount( restlt.getAddCleanAmount() );
        logCleancode.setCodeAmount( restlt.getAddCodeAmount() );
        logCleanCodeMapper.insert( logCleancode );
        for ( LogCleanCodeInfo t : logCleanCodeInfos ) {
            logCleanCodeInfoMapper.insert( t );
        }
        String name = "洗码金额:" + restlt.getAddCodeAmount().toString() + "存入:" + restlt.getAddCleanAmount()
                                                                                           .setScale( 2, RoundingMode.HALF_UP );
        memberMoneyManager.addMemberMoney( memberId, restlt.getAddCleanAmount(), EnumMoney.CODE_CLEAN, BigDecimal.ZERO, name,
                cleanId, cleanId );
    }

    @Override
    public List<RspCleanCodeLog> cleanCodeLogs( String memberId ) {
        return logCleanCodeMapper.selectRspByMemberId( memberId );
    }

    @Override
    public List<RspGameData> getGameDataList( String memberId, ReqGameData reqGameData ) {
        String beginDay = reqGameData.getEnumReqTime().getBeginDayTime();
        String endDay   = reqGameData.getEnumReqTime().getEndDayTime();
        return this.baseMapper.findByAccount( memberId.substring(
                memberId.length() - 1 ), memberId, reqGameData, beginDay, endDay );
    }

    @Override
    public RspBase<?> getGameBetRecordData( MemberGameData memberGameData ) {
        return null;
    }

    @Override
    public RspBase<?> getGameBetDetailData( MemberGameData memberGameData ) {
        return null;
    }

    @Override
    public List<RspWashCodeRate> getWashCodeRateList() {
        List<RspGameType>    gameTypeList    = gameCacheUtils.getEffectTypeList();
        List<ConfigWashCode> configWashCodes = gameCacheUtils.getEffectWashCodeConfigList();

        Map<Long, String> gameTypeMap = new LinkedHashMap<>();
        for ( ConfigWashCode washCode : configWashCodes ) {
            for ( RspGameType rspGameType : gameTypeList ) {
                if ( Objects.equals( washCode.getGameTypeId(), rspGameType.getId() ) ) {
                    gameTypeMap.put( rspGameType.getId(), rspGameType.getName() );
                }
            }
        }
        List<RspWashCodeRate> rspWashCodeRates = new ArrayList<>();
        for ( Map.Entry<Long, String> gameTypeEntry : gameTypeMap.entrySet() ) {
            RspWashCodeRate rspWashCodeRate = new RspWashCodeRate();
            rspWashCodeRate.setId( gameTypeEntry.getKey() );
            rspWashCodeRate.setName( gameTypeEntry.getValue().replace( "-", "" ) );
            List<RspWashCodeDesc> rspWashCodeDescList = new ArrayList<>();
            for ( ConfigWashCode washCode : configWashCodes ) {
                if ( Objects.equals( gameTypeEntry.getKey(), washCode.getGameTypeId() ) ) {
                    RspWashCodeDesc rspWashCodeDesc = new RspWashCodeDesc();
                    rspWashCodeDesc.setWashRate( Convert.rateConversion( washCode.getWashCodeRate() ) );
                    rspWashCodeDesc.setCodeInterval( Convert.amountConversion( washCode.getCodeMin() ) + "+" );
                    rspWashCodeDesc.setBeat( washCode.getBeat().stripTrailingZeros().toPlainString() );
                    rspWashCodeDescList.add( rspWashCodeDesc );
                }
            }
            rspWashCodeRate.setWashCodeDescList( rspWashCodeDescList );
            rspWashCodeRates.add( rspWashCodeRate );
        }
        return rspWashCodeRates;
    }

    @Override
    public RspBase<RspWashCodeInfo> getWashCodeDetail( String memberId ) {
        RspWashCodeInfo info = new RspWashCodeInfo();
        info.setWashCodeAmount( BigDecimal.ZERO );
        MemberInfo memberInfo = new QueryChainWrapper<>( memberInfoMapper ).eq( "id", memberId ).select( "id", "clean_time" )
                                                                           .one();
        if ( memberInfo.getCleanTime() != null ) {
            info.setWashCodeTime( LocalDateTimeUtils.format( memberInfo.getCleanTime() ) );
        }

        Map<Long, BigDecimal> sumGameTypeCodeMap = new HashMap<>();

        List<MemberGameData> sumProfitKinds = this.baseMapper.findMemWashPlatformKindLists( memberId.substring(
                memberId.length() - 1 ), memberId );

        this.processTypeCodeMap( sumGameTypeCodeMap, sumProfitKinds );

        List<RspGameType>    gameTypeList    = gameCacheUtils.getEffectTypeList();
        List<ConfigWashCode> configWashCodes = gameCacheUtils.getEffectWashCodeConfigList();

        List<RspGameTypeWashCode> rspGameTypeWashCodes = new ArrayList<>();
        for ( RspGameType rspGameType : gameTypeList ) {
            if ( Arrays.asList( 1L, 2L, 4L ).contains( rspGameType.getId() ) ) {
                continue;
            }
            RspGameTypeWashCode rspGameTypeWashCode = new RspGameTypeWashCode();
            rspGameTypeWashCode.setGameTypeId( rspGameType.getId() );
            rspGameTypeWashCode.setGameTypeName( rspGameType.getName().replace( "-", "" ) );
            rspGameTypeWashCode.setWashCodeAmount( BigDecimal.ZERO );
            rspGameTypeWashCode.setCodeAmountTotal( sumGameTypeCodeMap.getOrDefault( rspGameType.getId(), BigDecimal.ZERO ) );
            for ( ConfigWashCode washCode : configWashCodes ) {
                if ( Objects.equals( rspGameType.getId(), washCode.getGameTypeId() ) ) {
                    // 如果打码为0 则取第一条洗码配置
                    if ( Objects.equals( rspGameTypeWashCode.getCodeAmountTotal(), BigDecimal.ZERO )
                            && washCode.getCodeMin().compareTo( BigDecimal.ONE ) <= 0 ) {
                        rspGameTypeWashCode.setWashCodeRate( Convert.rateConversion( washCode.getWashCodeRate() ) );
                    }
                    if ( rspGameTypeWashCode.getCodeAmountTotal().compareTo( washCode.getCodeMin() ) > 0
                            && rspGameTypeWashCode.getCodeAmountTotal().compareTo( washCode.getCodeMax() ) < 0 ) {
                        rspGameTypeWashCode.setWashCodeRate( Convert.rateConversion( washCode.getWashCodeRate() ) );
                        rspGameTypeWashCode.setWashCodeAmount( washCode.getWashCodeRate()
                                                                       .multiply( rspGameTypeWashCode.getCodeAmountTotal() ) );
                    }
                }
            }
            info.setWashCodeAmount( info.getWashCodeAmount().add( rspGameTypeWashCode.getWashCodeAmount() ) );
            rspGameTypeWashCodes.add( rspGameTypeWashCode );
        }
        info.setRspGameTypeWashCodes( rspGameTypeWashCodes );
        return RspBase.ok( info );
    }

    private void processTypeCodeMap( Map<Long, BigDecimal> sumGameTypeCodeMap, List<MemberGameData> sumProfitKinds ) {
        if ( CollectionUtils.isNotEmpty( sumProfitKinds ) ) {
            Set<Integer> platformIds = sumProfitKinds.stream().map( MemberGameData::getPlatformId ).collect( Collectors.toSet() );
            Set<String>  kindIds     = sumProfitKinds.stream().map( MemberGameData::getKindId ).collect( Collectors.toSet() );
            // 排除 热门游戏/老棋牌游戏/老电子游戏
            List<GameInfo> gameInfos = new QueryChainWrapper<>( gameInfoMapper ).in( "platform_id", platformIds )
                                                                                .in( "kind_id", kindIds )
                                                                                .notIn( "type_id", 1, 2, 4 ).list();

            for ( MemberGameData memberGameData : sumProfitKinds ) {
                for ( GameInfo gameInfo : gameInfos ) {
                    if ( memberGameData.getPlatformId() == gameInfo.getPlatformId().intValue() && (
                            memberGameData.getKindId().equals( gameInfo.getKindId() ) || gameInfo.getKindId().endsWith(
                                    "-" + memberGameData.getKindId() ) ) ) {
                        Long       gameTypeId = gameInfo.getTypeId();
                        BigDecimal profit     = new BigDecimal( memberGameData.getProfit() );
                        if ( gameTypeId != null ) {
                            BigDecimal value = sumGameTypeCodeMap.get( gameTypeId );
                            sumGameTypeCodeMap.put( gameTypeId, value == null ? profit : value.add( profit ) );
                        } else {
                            GamePlatform gamePlatform = gameCacheUtils.getGamePlatform( memberGameData.getPlatformId()
                                                                                                      .longValue() );
                            if ( gamePlatform.getName().contains( "棋牌" ) ) {
                                gameTypeId = 8L;
                            } else if ( gamePlatform.getName().contains( "电子" ) ) {
                                gameTypeId = 9L;
                            } else if ( gamePlatform.getName().contains( "视讯" ) ) {
                                gameTypeId = 7L;
                            } else {
                                log.error( "未知的游戏信息 platformId:{};kindId:{}", memberGameData.getPlatformId(),
                                        memberGameData.getKindId() );
                            }
                            if ( gameTypeId != null ) {
                                BigDecimal value = sumGameTypeCodeMap.get( gameTypeId );
                                sumGameTypeCodeMap.put( gameTypeId, value == null ? profit : value.add( profit ) );
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<RspGameWashCodeLog> getWashCodeLogs( String memberId ) {
        return gameWashCodeLogMapper.selectRspList( memberId );
    }

    @Override
    public RspBase<RspWashCodeInfo> toWashCode( String memberId ) {
        if ( !redisUtils.lock( "cleanCode" + memberId, 5 ) ) {
            return RspBase.businessError( "请勿重复请求" );
        }

        this.toWashCodeProcess( memberId );

        redisUtils.unLock( "cleanCode" + memberId );
        RspBase<RspWashCodeInfo> rspBase = this.getWashCodeDetail( memberId );
        rspBase.getData().setMoney( memberInfoMapper.getUserBalance( memberId ) );
        return rspBase;
    }

    private void toWashCodeProcess( String memberId ) {
        List<MemberGameData> sumProfitKinds = this.baseMapper.findMemWashPlatformKindLists( memberId.substring(
                memberId.length() - 1 ), memberId );
        if ( CollectionUtils.isEmpty( sumProfitKinds ) ) {
            return;
        }
        Map<Long, BigDecimal> sumGameTypeCodeMap = new HashMap<>();

        this.processTypeCodeMap( sumGameTypeCodeMap, sumProfitKinds );

        List<GameWashCodeLog> gameWashCodeLogs = new ArrayList<>();
        LocalDateTime         now              = LocalDateTime.now();
        List<ConfigWashCode>  configWashCodes  = gameCacheUtils.getEffectWashCodeConfigList();
        for ( ConfigWashCode washCode : configWashCodes ) {
            BigDecimal value = sumGameTypeCodeMap.get( washCode.getGameTypeId() );
            if ( value != null && value.compareTo( washCode.getCodeMin() ) > 0 && value.compareTo( washCode.getCodeMax() ) < 0 ) {
                BigDecimal      singeWashCode   = washCode.getWashCodeRate().multiply( value );
                GameWashCodeLog gameWashCodeLog = new GameWashCodeLog();
                gameWashCodeLog.setWashCodeTime( now );
                gameWashCodeLog.setMemberId( memberId );
                gameWashCodeLog.setGameTypeId( washCode.getGameTypeId() );
                gameWashCodeLog.setWashCodeRate( washCode.getWashCodeRate() );
                gameWashCodeLog.setWashCodeAmount(
                        singeWashCode.compareTo( new BigDecimal( "0.01" ) ) < 0 ? BigDecimal.ZERO : singeWashCode );
                gameWashCodeLog.setCodeAmount( value );
                gameWashCodeLog.setBeat( washCode.getBeat() );
                gameWashCodeLogs.add( gameWashCodeLog );
            }
        }
        String washId = IdWorker.get32UUID();
        SpringUtils.getBean( MemberGameDataService.class ).opWashCode( memberId, gameWashCodeLogs, now, washId );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void opWashCode( String memberId, List<GameWashCodeLog> gameWashCodeLogs, LocalDateTime now, String washId ) {
        this.baseMapper.updateByBatchClean( memberId.substring( memberId.length() - 1 ), memberId );

        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setCleanTime( now );
        memberInfoMapper.updateById( update );

        int i = 0;
        for ( GameWashCodeLog gameWashCodeLog : gameWashCodeLogs ) {
            String washId_ = washId + "-" + i++;
            gameWashCodeLog.setWashId( washId_ );
            gameWashCodeLogMapper.insert( gameWashCodeLog );

            BigDecimal value = gameWashCodeLog.getWashCodeAmount().setScale( 2, RoundingMode.HALF_UP );
            String     name  = "洗码金额:" + gameWashCodeLog.getCodeAmount() + "存入:" + value;
            memberMoneyManager.addMemberMoney( memberId, value, EnumMoney.CODE_CLEAN, gameWashCodeLog.getBeat(), name, washId_,
                    washId_ );
        }
    }
}