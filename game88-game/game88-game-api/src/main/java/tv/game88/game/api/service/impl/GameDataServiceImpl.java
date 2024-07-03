package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.game.type.EnumGameCategory;
import tv.game88.core.lottery.entity.LotteryBet;
import tv.game88.core.lottery.mapper.LotteryBetMapper;
import tv.game88.core.member.cache.ConfigVipCacheUtils;
import tv.game88.core.member.entity.ConfigVip;
import tv.game88.core.member.entity.MemberBcode;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.MemberBcodeMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.quest.cache.ActivityCacheUtil;
import tv.game88.core.quest.entity.ActivityQuestInfo;
import tv.game88.core.quest.manager.MemberQuestManager;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.dto.RspGameInfo;
import tv.game88.game.api.entity.GameDataRecord;
import tv.game88.game.api.entity.GamePlatform;
import tv.game88.game.api.entity.MemberGameData;
import tv.game88.game.api.mapper.GameDataRecordMapper;
import tv.game88.game.api.mapper.GamePlatformMapper;
import tv.game88.game.api.mapper.MemberGameDataMapper;
import tv.game88.game.api.service.GameDataService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
public class GameDataServiceImpl implements GameDataService {
    @Resource
    private MemberMoneyManager      memberMoneyManager;
    @Resource
    private MemberQuestManager      memberQuestManager;
    @Resource
    private MemberBcodeMapper       memberBcodeMapper;
    @Resource
    private MemberInfoMapper        memberInfoMapper;
    @Resource
    private LotteryBetMapper        lotteryBetMapper;
    @Resource
    private GamePlatformMapper      gamePlatformMapper;
    @Resource
    private GameDataRecordMapper    gameDataRecordMapper;
    @Resource
    private GameCacheUtils          gameCacheUtils;
    @Resource
    private ConfigVipCacheUtils     configVipCacheUtils;
    @Resource
    private SqlSessionTemplate      sqlSessionTemplate;
    @Resource
    private ActivityCacheUtil       activityCacheUtil;

    @Value( "${spring.profiles.active}" )
    private String profile;

    private static final String TABLE_PREFIX = "game_data_record_";

    @Override
    public void beatGameCodeAgent( String start, String end, String account, Long platformId ) {
        List<GamePlatform> gamePlatforms = new QueryChainWrapper<>( gamePlatformMapper ).list();


        Map<Long, GamePlatform> gamePlatformIdMap = gamePlatforms
                .stream()
                .collect( Collectors.toMap( GamePlatform::getId, Function.identity() ) );

        String day = end.substring( 0, 10 ).replace( "-", "" );
        List<GameDataRecord> gameDataRecords = gameDataRecordMapper.selectGameDataRecordAgentList(
                TABLE_PREFIX + day, start, end, profile, account, platformId );

        if ( CollectionUtils.isEmpty( gameDataRecords ) ) {
            log.warn( "拉单条数为0, 开始时间:{} 结束时间:{}", start, end );
            return;
        }
        log.info( "拉单条数:{}, 开始时间:{} 结束时间:{}", gameDataRecords.size(), start, end );

        Map<String, BigDecimal> willCodeMap  = new HashMap<>();
        List<MemberGameData>    willCodeList = new ArrayList<>();
        SqlSession              session      = sqlSessionTemplate.getSqlSessionFactory().openSession( ExecutorType.BATCH, false );
        MemberGameDataMapper    mapper       = session.getMapper( MemberGameDataMapper.class );
        for ( GameDataRecord gameDataRecord : gameDataRecords ) {
            GamePlatform gamePlatform = gamePlatformIdMap.get( gameDataRecord.getPlatformId() );
            String       memberId     = gameDataRecord.getAccount().toUpperCase().split( "_" )[ 1 ];
            if ( mapper.findExist( memberId.substring( memberId.length() - 1 ), gameDataRecord.getId() ) > 0 ) {
                continue;
            }
            MemberGameData memberGameData = new MemberGameData();
            memberGameData.setId( gameDataRecord.getId() );
            memberGameData.setGameId( gameDataRecord.getGameId() );
            memberGameData.setAccount( memberId );
            memberGameData.setKindId( gameDataRecord.getKindId() );
            //memberGameData.setServerId( gameDataRecord.getServerId() );
            memberGameData.setCellScore( gameDataRecord.getCellScore() );
            memberGameData.setAllBet( gameDataRecord.getAllBet() );
            memberGameData.setProfit( gameDataRecord.getProfit() );
            memberGameData.setGameStartTime( gameDataRecord.getGameStartTime() );
            memberGameData.setGameEndTime( gameDataRecord.getGameEndTime() );
            memberGameData.setAgent( gameDataRecord.getGameAgent() );
            memberGameData.setStatus( 0 );
            memberGameData.setRevenue( gameDataRecord.getRevenue() );
            memberGameData.setGameRound( gameDataRecord.getGameRound() );

            memberGameData.setPlatformId( gamePlatform.getId().intValue() );
            willCodeList.add( memberGameData );

            if ( new BigDecimal( memberGameData.getProfit() ).compareTo( BigDecimal.ZERO ) == 0 ) {
                continue;
            }

            BigDecimal beatAdd = new BigDecimal( gameDataRecord.getCellScore() )
                    .multiply( gamePlatform.getRateBeat() )
                    .setScale( 4, RoundingMode.HALF_UP );
            willCodeMap.putIfAbsent( memberId, BigDecimal.ZERO );
            willCodeMap.put( memberId, willCodeMap.get( memberId ).add( beatAdd ) );


        }
        log.warn( "准备处理条数:{}, 开始时间:{} 结束时间:{}", willCodeList.size(), start, end );
        insertBatch( session, mapper, willCodeList );
        this.doBeatCode( willCodeMap );
        this.deQuestCheck( willCodeList );
        log.info( "新拉单拉取条数：{},实际插入:{}, 开始时间:{}, 结束时间:{}", gameDataRecords.size(), willCodeList.size(), start, end );
    }

    @Override
    public void beatLotteryCode( String start, String end ) {
        List<LotteryBet> list = lotteryBetMapper.selectListByTime( start, end );
        if ( CollectionUtils.isEmpty( list ) ) {
            return;
        }
        log.warn( "彩票拉取注单数量" + list.size() );
        List<GamePlatform> gamePlatforms = new QueryChainWrapper<>( gamePlatformMapper ).list();
        GamePlatform gamePlatform = gamePlatforms
                .stream()
                .filter( p -> p.getGameCategory() == EnumGameCategory.LOTTERY )
                .findFirst()
                .get();

        Map<String, BigDecimal> willCodeMap  = new HashMap<>();
        List<MemberGameData>    willCodeList = new ArrayList<>();
        SqlSession              session      = sqlSessionTemplate.getSqlSessionFactory().openSession( ExecutorType.BATCH, false );
        MemberGameDataMapper    mapper       = session.getMapper( MemberGameDataMapper.class );
        for ( LotteryBet og : list ) {
            if ( mapper.findExist( og.getMemberId().substring( og.getMemberId().length() - 1 ), og.getId() ) > 0 ) {
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
        this.doBeatCode( willCodeMap );
        this.deQuestCheck( willCodeList );
    }

    public void insertBatch( SqlSession session, MemberGameDataMapper mapper, List<MemberGameData> willCodeList ) {
        int count = 0;
        for ( MemberGameData in : willCodeList ) {
            String dbNodes = in.getAccount().substring( in.getAccount().length() - 1 );
            try {
                if ( mapper.findExist( dbNodes, in.getId() ) > 0 ) {
                    continue;
                }
                mapper.insertMemberGameData( in, dbNodes );
                count += 1;
                if ( count >= 500 ) {
                    session.commit();
                    count = 0;
                }

            } catch ( Exception e ) {
                log.error( e.getMessage(), e );
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
        List<ActivityQuestInfo> listConfQuest = activityCacheUtil
                .getQuestInfos()
                .stream()
                .filter( activityQuestInfo -> activityQuestInfo.getGameTypeId() > 0 )
                .toList();
        for ( MemberGameData data : list ) {
            // 过滤百家乐和局庄闲下注，不计入打码和任务
            if ( new BigDecimal( data.getProfit() ).compareTo( BigDecimal.ZERO ) == 0 && data.getKindId().equals( "2001" ) ) {
                continue;
            }

            BigDecimal add = new BigDecimal( data.getCellScore() );
            for ( ActivityQuestInfo confQuest : listConfQuest ) {
                Long              gameTypeId     = confQuest.getGameTypeId();
                List<RspGameInfo> effectInfoList = gameCacheUtils.getInfoAllList( gameTypeId );
                boolean           y              = false;
                for ( RspGameInfo rspGameInfo : effectInfoList ) {
                    if ( rspGameInfo.getKindId() == null ) {
                        continue;
                    }
                    if ( Objects.equals( data.getPlatformId(), rspGameInfo.getPlatformId() ) && (
                            data.getKindId().equals( rspGameInfo.getKindId() ) || rspGameInfo
                                    .getKindId()
                                    .endsWith( "-" + data.getKindId() ) ) ) {
                        y = true;
                        break;
                    }
                }
                if ( y ) {
                    memberQuestManager.memberQuestProcess( data.getAccount(), add, confQuest );
                }
            }
        }
    }
}
