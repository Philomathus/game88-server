package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.lottery.entity.LotteryBet;
import tv.game88.core.lottery.mapper.LotteryBetMapper;
import tv.game88.core.member.cache.ConfigVipCacheUtils;
import tv.game88.core.member.entity.ConfigVip;
import tv.game88.core.member.entity.MemberBcode;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.MemberBcodeMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.quest.entity.ActivityQuestInfo;
import tv.game88.core.quest.entity.MemberQuest;
import tv.game88.core.quest.mapper.ActivityQuestInfoMapper;
import tv.game88.core.quest.mapper.MemberQuestMapper;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.dto.RspGameDataLog;
import tv.game88.game.api.dto.RspGameInfo;
import tv.game88.game.api.entity.GamePlatform;
import tv.game88.game.api.entity.MemberGameData;
import tv.game88.game.api.mapper.MemberGameDataMapper;
import tv.game88.game.api.service.GameDataService;
import tv.game88.game.api.service.GameService;
import tv.game88.game.api.type.EnumGameCategory;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
public class GameDataServiceImpl implements GameDataService {
    @Resource
    private GameService             gameService;
    @Resource
    private MemberMoneyManager      memberMoneyManager;
    @Resource
    private MemberBcodeMapper       memberBcodeMapper;
    @Resource
    private MemberInfoMapper        memberInfoMapper;
    @Resource
    private ActivityQuestInfoMapper questInfoMapper;
    @Resource
    private MemberQuestMapper       memberQuestMapper;
    @Resource
    private LotteryBetMapper        lotteryBetMapper;
    @Resource
    private GameCacheUtils          gameCacheUtils;
    @Resource
    private ConfigVipCacheUtils     configVipCacheUtils;
    @Resource
    private SqlSessionTemplate      sqlSessionTemplate;

    @Override
    public void beatGameCodeAgent( String dTime, String start, String end, String account, EnumGameCategory gameCategory ) {
        List<RspGameDataLog> rspGameDataLogs = gameService.remoteDataGrab( start, end, account,
                gameCategory != null ? EnumGameCategory.getDataRemoteByEnum( gameCategory ) : null );
        if ( CollectionUtils.isEmpty( rspGameDataLogs ) ) {
            return;
        }
        Map<EnumGameCategory, GamePlatform> gamePlatformMap = gameCacheUtils
                .getGamePlatformList()
                .stream()
                .collect( Collectors.toMap( GamePlatform::getGameCategory, Function.identity() ) );
        Map<String, BigDecimal> willCodeMap  = new HashMap<>();
        List<MemberGameData>    willCodeList = new ArrayList<>();
        SqlSession              session      = sqlSessionTemplate.getSqlSessionFactory().openSession( ExecutorType.BATCH, false );
        MemberGameDataMapper    mapper       = session.getMapper( MemberGameDataMapper.class );
        for ( RspGameDataLog dataLog : rspGameDataLogs ) {
            EnumGameCategory enumGameCategory = EnumGameCategory.getEnumByDataRemote( dataLog.getPlatform_id() );
            if ( enumGameCategory == null ) {
                continue;
            }
            GamePlatform gamePlatform = gamePlatformMap.get( enumGameCategory );
            String       memberId     = dataLog.getAccount().toLowerCase().split( "_" )[ 1 ];
            if ( mapper.findExist( memberId.substring( memberId.length() - 1 ), dataLog.getId() ) != null ) {
                continue;
            }
            MemberGameData gameDataLog = new MemberGameData();
            gameDataLog.setId( dataLog.getId() );
            gameDataLog.setGameId( dataLog.getGame_id() );
            gameDataLog.setAccount( memberId );
            gameDataLog.setKindId( dataLog.getKind_id() );
            gameDataLog.setServerId( dataLog.getServer_id() );
            gameDataLog.setCellScore( dataLog.getCell_score() );
            gameDataLog.setAllBet( dataLog.getAll_bet() );
            gameDataLog.setProfit( dataLog.getProfit() );
            gameDataLog.setGameStartTime( dataLog.getGame_start_time() );
            gameDataLog.setGameEndTime( dataLog.getGame_end_time() );
            gameDataLog.setAgent( dataLog.getAgent() );
            gameDataLog.setStatus( 0 );
            gameDataLog.setRevenue( dataLog.getRevenue() );
            gameDataLog.setGameRound( dataLog.getGame_round() );

            gameDataLog.setPlatformId( gamePlatform.getId().intValue() );

            BigDecimal beatAdd = new BigDecimal( dataLog.getCell_score() )
                    .multiply( gamePlatform.getRateBeat() )
                    .setScale( 4, RoundingMode.HALF_UP );
            willCodeMap.putIfAbsent( memberId, BigDecimal.ZERO );
            willCodeMap.put( memberId, willCodeMap.get( memberId ).add( beatAdd ) );

            willCodeList.add( gameDataLog );
        }
        insertBatch( session, mapper, willCodeList );
        doBeatCode( willCodeMap );
        deQuestCheck( willCodeList );
        log.info( "新拉单拉取条数：{},实际插入:{}", rspGameDataLogs.size(), willCodeList.size() );
    }

    @Override
    public void beatLotteryCode( String start, String end ) {
        List<LotteryBet> list = lotteryBetMapper.selectListByTime( start, end );
        if ( CollectionUtils.isEmpty( list ) ) {
            return;
        }
        log.warn( "彩票拉取注单数量" + list.size() );
        GamePlatform gamePlatform = gameCacheUtils
                .getGamePlatformList()
                .stream()
                .filter( p -> p.getGameCategory() == EnumGameCategory.LOTTERY )
                .findFirst()
                .get();

        Map<String, BigDecimal> willCodeMap  = new HashMap<>();
        List<MemberGameData>    willCodeList = new ArrayList<>();
        SqlSession              session      = sqlSessionTemplate.getSqlSessionFactory().openSession( ExecutorType.BATCH, false );
        MemberGameDataMapper    mapper       = session.getMapper( MemberGameDataMapper.class );
        for ( LotteryBet og : list ) {
            if ( mapper.findExist( og.getMemberId().substring( og.getMemberId().length() - 1 ), og.getId() ) != null ) {
                continue;
            }
            MemberGameData gameDataLog = new MemberGameData();
            gameDataLog.setId( og.getId() );
            gameDataLog.setGameId( og.getId() );
            gameDataLog.setAccount( og.getMemberId() );
            gameDataLog.setKindId( og.getLotteryId().toString() );
            gameDataLog.setCellScore( String.valueOf( og.getCost() ) );
            gameDataLog.setAllBet( gameDataLog.getCellScore() );
            gameDataLog.setProfit( String.valueOf( og.getPrize().subtract( og.getCost() ) ) );
            gameDataLog.setGameStartTime( LocalDateTimeUtils.format( og.getBetTime() ) );
            gameDataLog.setGameEndTime( LocalDateTimeUtils.format( og.getUpdateTime() ) );
            gameDataLog.setAgent( "-1" );
            gameDataLog.setStatus( 0 );
            gameDataLog.setPlatformId( gamePlatform.getId().intValue() );

            // 百家乐和局中庄闲下注退款不计打码
            if ( !( og.getLotteryId() == 2001 && new BigDecimal( gameDataLog.getProfit() ).compareTo( BigDecimal.ZERO ) == 0 ) ) {
                BigDecimal beatAdd = og.getCost().multiply( gamePlatform.getRateBeat() ).setScale( 4, RoundingMode.HALF_UP );
                willCodeMap.putIfAbsent( og.getMemberId(), BigDecimal.ZERO );
                willCodeMap.put( og.getMemberId(), willCodeMap.get( og.getMemberId() ).add( beatAdd ) );
            }

            willCodeList.add( gameDataLog );
        }

        insertBatch( session, mapper, willCodeList );

        doBeatCode( willCodeMap );

        deQuestCheck( willCodeList );
    }

    public void insertBatch( SqlSession session, MemberGameDataMapper mapper, List<MemberGameData> willCodeList ) {
        int count = 0;
        for ( MemberGameData in : willCodeList ) {
            try {
                mapper.insertMemberGameData( in, in.getAccount().substring( in.getAccount().length() - 1 ) );
                count += 1;
                if ( count >= 500 ) {
                    session.commit();
                    count = 0;
                }

            } catch ( Exception e ) {
                e.printStackTrace();
            }

        }
        if ( count > 0 ) {
            session.commit();

        }
        session.close();
    }

    public void doBeatCode( Map<String, BigDecimal> willCodeMap ) {
        Map<String, BigDecimal> codeAccountMap = new HashMap<>();
        MemberBcode             query          = new MemberBcode();
        query.setStatus( 0 );
        //遍历有注单的会员
        for ( String user_id : willCodeMap.keySet() ) {
            //记录此会员新的打码量
            BigDecimal beatVal = BigDecimal.ZERO;
            BigDecimal codeVal = willCodeMap.get( user_id );
            //查询到此人需要打码的充值记录
            query.setUserId( user_id );
            List<MemberBcode> codeFlowlist = memberBcodeMapper.selectWillBcodeList( query );
            for ( MemberBcode codeFlow : codeFlowlist ) {
                if ( codeVal.compareTo( BigDecimal.ZERO ) <= 0 ) {
                    continue;
                }
                //此纪录最初打码量
                BigDecimal oldCur  = codeFlow.getCur();
                BigDecimal addCode = codeVal.add( oldCur );
                if ( addCode.compareTo( codeFlow.getIncome() ) > 0 ) {
                    codeFlow.setCur( codeFlow.getIncome() );
                    codeFlow.setStatus( 1 );
                    //codeFlow.setCreate_time(new Date());
                } else if ( addCode.compareTo( codeFlow.getIncome() ) == 0 ) {
                    codeFlow.setCur( codeFlow.getIncome() );
                    codeFlow.setStatus( 1 );
                    //codeFlow.setCreate_time(new Date());
                } else {
                    codeFlow.setCur( addCode );
                }
                beatVal = beatVal.add( codeFlow.getCur().subtract( oldCur ) );

                codeVal = codeVal.subtract( codeFlow.getCur().subtract( oldCur ) );
                memberBcodeMapper.updateMemberBcode( codeFlow );
            }
            if ( codeAccountMap.containsKey( user_id ) ) {
                codeAccountMap.put( user_id, codeAccountMap.get( user_id ).add( beatVal ).setScale( 4, RoundingMode.HALF_UP ) );
            } else {
                codeAccountMap.put( user_id, beatVal );
            }
        }

        for ( String userId : willCodeMap.keySet() ) {
            BigDecimal c = codeAccountMap.get( userId );
            if ( c == null ) {
                c = BigDecimal.ZERO;
            } else {
                c = c.setScale( 2, RoundingMode.DOWN );
            }
            BigDecimal w = willCodeMap.get( userId );
            if ( w == null ) {
                w = BigDecimal.ZERO;
            } else {
                w = w.setScale( 2, RoundingMode.DOWN );
            }
            try {
                memberInfoMapper.updateBeatCode( userId, c, w );
            } catch ( Exception e ) {
                log.error( "打码异常userId:{},code_account：{},code_total:{}", userId, c, w, e );
            }

        }

        List<ConfigVip> configVips = configVipCacheUtils
                .getConfigVipMap()
                .values()
                .stream()
                .sorted( Comparator.comparing( ConfigVip::getBcode ) )
                .toList();
        for ( String userId : willCodeMap.keySet() ) {
            memberMoneyManager.checkAndUpdateVip( userId, configVips );
        }
    }

    public void deQuestCheck( final List<MemberGameData> list ) {
        //查找全部任务
        List<ActivityQuestInfo> listConfQuest = questInfoMapper.selectList( new QueryWrapper<ActivityQuestInfo>().eq( "effect",
                1 ) );

        for ( MemberGameData data : list ) {
            // 过滤百家乐和局庄闲下注，不计入打码和任务
            if ( new BigDecimal( data.getProfit() ).compareTo( BigDecimal.ZERO ) == 0 && data.getKindId().equals( "2001" ) ) {
                continue;
            }
            int add = new BigDecimal( data.getCellScore() ).intValue();
            for ( ActivityQuestInfo confQuest : listConfQuest ) {
                Long              gameTypeId     = confQuest.getGameTypeId();
                List<RspGameInfo> effectInfoList = gameCacheUtils.getEffectInfoList( gameTypeId );
                for ( RspGameInfo rspGameInfo : effectInfoList ) {
                    if ( !rspGameInfo.getPlatformId().equals( data.getPlatformId() ) ) {
                        continue;
                    }
                    MemberQuest memberQuest = memberQuestMapper.selectById( data
                            .getAccount()
                            .concat( "_" )
                            .concat( confQuest.getId().toString() ) );
                    if ( memberQuest == null ) {
                        memberQuest = new MemberQuest();
                        memberQuest.setMemberId( data.getAccount() );
                        memberQuest.setQuestId( confQuest.getId() );
                        memberQuest.setId( data.getAccount().concat( "_" ).concat( confQuest.getId().toString() ) );
                        memberQuest.setStatus( 0 );
                        memberQuest.setCurNum( add );
                        if ( memberQuest.getCurNum() >= confQuest.getTarget() ) {
                            memberQuest.setCurNum( confQuest.getTarget() );
                            memberQuest.setStatus( 1 );
                        }
                        memberQuest.setTaskMode( confQuest.getTaskMode() );
                        memberQuestMapper.insert( memberQuest );
                    } else if ( memberQuest.getStatus() == 0 ) {
                        memberQuest.setCurNum( memberQuest.getCurNum() + add );
                        if ( memberQuest.getCurNum() >= confQuest.getTarget() ) {
                            memberQuest.setCurNum( confQuest.getTarget() );
                            memberQuest.setStatus( 1 );
                        }
                        memberQuestMapper.updateById( memberQuest );
                    }
                }
            }
        }
    }
}
