package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.dto.*;
import tv.game88.game.api.entity.GamePlatform;
import tv.game88.game.api.entity.LogCleanCode;
import tv.game88.game.api.entity.LogCleanCodeInfo;
import tv.game88.game.api.entity.MemberGameData;
import tv.game88.game.api.mapper.LogCleanCodeInfoMapper;
import tv.game88.game.api.mapper.LogCleanCodeMapper;
import tv.game88.game.api.mapper.MemberGameDataMapper;
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
    private GameCacheUtils         gameCacheUtils;
    @Resource
    private RedisUtils             redisUtils;

    private static Pattern NUM_PATTERN = Pattern.compile( "^[-\\+]?[\\d]*$" );

    /**
     * 查询会员游戏注单数据列表
     *
     * @return 会员游戏注单数据
     */
    @Override
    public List<MemberGameData> selectMemberGameDataList( ReqMemberGameData reqMemberGameData ) {
        pingjieReq( reqMemberGameData );
        return this.baseMapper.selectMemberGameDataList( reqMemberGameData );
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
        String name = "洗码金额:" + restlt.getAddCodeAmount().toString() + "存入:" + restlt
                .getAddCleanAmount()
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
        Map<EnumGameCategory, GamePlatform> gamePlatformMap = gameCacheUtils
                .getGamePlatformList()
                .stream()
                .collect( Collectors.toMap( GamePlatform::getGameCategory, Function.identity() ) );
        GamePlatform gamePlatform = gamePlatformMap.get( reqGameData.getGameCategory() );
        if ( gamePlatform == null ) {
            return new ArrayList<>();
        }
        reqGameData.setPlatformId( gamePlatform.getId() );
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
}