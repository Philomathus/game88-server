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