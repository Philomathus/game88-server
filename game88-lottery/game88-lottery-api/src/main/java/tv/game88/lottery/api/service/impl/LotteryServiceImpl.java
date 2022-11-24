package tv.game88.lottery.api.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.lottery.dto.RspBetRecord;
import tv.game88.core.lottery.dto.RspLotteryHistory;
import tv.game88.core.lottery.entity.LotteryBet;
import tv.game88.core.lottery.mapper.LotteryBetMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.lottery.api.base.ExLottery;
import tv.game88.lottery.api.base.ExLotteryFactoryUtil;
import tv.game88.lottery.api.cache.LotteryCacheUtils;
import tv.game88.lottery.api.dto.*;
import tv.game88.lottery.api.entity.*;
import tv.game88.lottery.api.extents.Ex6HeCai;
import tv.game88.lottery.api.extents.ExBaccarat;
import tv.game88.lottery.api.extents.ExKuai3;
import tv.game88.lottery.api.mapper.*;
import tv.game88.lottery.api.service.LotteryBetService;
import tv.game88.lottery.api.service.LotteryHistoryService;
import tv.game88.lottery.api.service.LotteryService;
import tv.game88.lottery.api.utils.LotteryUtils;
import tv.game88.lottery.api.utils.imserver.ImServerUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
public class LotteryServiceImpl implements LotteryService {
    @Resource
    private LotteryInfoMapper        lotteryInfoMapper;
    @Resource
    private LotteryBetMapper         lotteryBetMapper;
    @Resource
    private LotteryHistoryMapper     lotteryHistoryMapper;
    @Resource
    private LotteryCountMapper       lotteryCountMapper;
    @Resource
    private LotteryTempMapper        lotteryTempMapper;
    @Resource
    private LotteryPrizeconfigMapper lotteryPrizeconfigMapper;
    @Resource
    private LotteryPrizepoolMapper   lotteryPrizepoolMapper;
    @Resource
    private MemberInfoMapper         memberInfoMapper;

    @Resource
    private LotteryBetService     lotteryBetService;
    @Resource
    private LotteryHistoryService lotteryHistoryService;

    @Resource
    private SqlSessionTemplate sqlSessionTemplate;

    @Resource
    private RedisUtils           redisUtils;
    @Resource
    private ImServerUtils        imServerUtils;
    @Resource
    private ConfigEnvCacheUtil   configEnvCacheUtil;
    @Resource
    private ExLotteryFactoryUtil exLotteryFactoryUtil;

    @Value( "${spring.profiles.active}" )
    private String profile;
    @Value( "${lotteryCenter:7701}" )
    private String lotteryAgent;

    /**
     * 是否是开奖中心
     **/
    public boolean isLotteryCenter() {
        return lotteryAgent.equals( profile );
    }

    @Override
    public RspLotteryInit getRspLotteryInit( Integer lotteryId ) {
        RspLotteryInit init = new RspLotteryInit();
        init.setBase( LotteryCacheUtils.me.getLotteryBase( lotteryId ) );
        int kindId = LotteryUtils.getKindId( lotteryId );
        init.setMethods( LotteryCacheUtils.me.getKindMethods( kindId ) );
        for ( RspMethod m : init.getMethods() ) {
            m.setGames( LotteryCacheUtils.me.getMethodGames( m.getId() ) );
        }
        init.setIssuevo( getIssueVo( lotteryId ) );
        return init;
    }

    public IssueVo getIssueVo( Integer lotteryId ) {
        IssueVo vo = new IssueVo();
        try {
            LotteryTemp lotteryTemp = LotteryCacheUtils.me.getLotteryTemp( lotteryId );
            long        countdown   = Math.abs( lotteryTemp.getKtime().until( LocalDateTime.now(), ChronoUnit.SECONDS ) );
            if ( countdown <= 0 ) {
                countdown = 0;
            }
            vo.setIssue( lotteryTemp.getIssue() );
            if ( lotteryId == 2001 && lotteryTemp.getCodeJust() != null ) {
                vo.setCodeJust( ExBaccarat.getBaccaratResults( lotteryTemp.getCodeJust() ).get( "result" ) );
            } else {
                vo.setCodeJust( lotteryTemp.getCodeJust() );
            }
            vo.setCountdown( countdown );
        } catch ( Exception e ) {
            log.error( "更新期数异常id：{}", lotteryId, e );
        }
        if ( vo.getCountdown() == 0 ) {
            vo.setCountdown( ( long ) ( LotteryCacheUtils.me.getLotteryBase( lotteryId ).getCycle() * 60 ) );
        }
        return vo;
    }

    @Override
    public List<RspBetRecord> getBetRecordList( Integer lotteryId, String memberId ) {
        List<RspBetRecord> betRecords = lotteryBetMapper.getBetRecordList( memberId.substring(
                memberId.length() - 1 ), lotteryId, memberId );
        for ( RspBetRecord betRecord : betRecords ) {
            if ( StringUtils.isBlank( betRecord.getCode() ) ) {
                betRecord.setWinOrLoseResult( "待开奖" );
            } else {
                if ( betRecord.getPrize().compareTo( betRecord.getCost() ) > 0 ) {
                    betRecord.setWinOrLoseResult( "赢了" + betRecord.getPrize().subtract( betRecord.getCost() ) );
                } else if ( betRecord.getPrize().compareTo( betRecord.getCost() ) < 0 ) {
                    betRecord.setWinOrLoseResult( "输了" );
                }
            }
            if ( lotteryId == 2001 && betRecord.getCode() != null ) {
                Map<String, String> map = ExBaccarat.getBaccaratResults( betRecord.getCode() );
                if ( !CollectionUtils.isEmpty( map ) ) {
                    betRecord.setPlayOrBank( map.get( "PlayOrBank" ) );
                    betRecord.setAnalyse( map.get( "result" ) );

                    if ( "和".equals( betRecord.getPlayOrBank() )
                            && betRecord.getPrize().compareTo( betRecord.getCost() ) == 0 ) {
                        betRecord.setStatus( 3 );
                    }
                }
            }
            this.extracted( lotteryId, betRecord );
        }
        return betRecords;
    }

    public void extracted( Integer lotteryId, RspLotteryHistory betRecord ) {
        if ( betRecord.getCode() != null ) {
            int kindId = LotteryUtils.getKindId( lotteryId );
            switch ( kindId ) {
            case 1:
                //betRecord.setAnalyse( Ex11xuan5.concatBetString(betMap));
                break;
            case 4:
                String[] s = betRecord.getCode().split( " " );
                String tarCode = s[ s.length - 1 ];
                betRecord.setAnalyse( Ex6HeCai.getShengXiao( tarCode ) );
                break;
            case 2:
                int total = Arrays.stream( betRecord.getCode().split( " " ) ).mapToInt( Integer::parseInt ).sum();
                betRecord.setAnalyse( ExKuai3.getZongDaXiao( total ) + "" + ExKuai3.getZongDanShuang( total ) );
                break;
            case 3:
                //betRecord.setAnalyse( ExSanChe.concatBetString(betMap));
                break;
            case 0:
                //betRecord.setAnalyse( ExShiShiCai.concatBetString(betMap));
                break;
            }
        }
    }

    @Override
    public List<RspLotteryHistory> getLotteryHistory( Integer lotteryId ) {
        List<RspLotteryHistory> rspLotteryHistories = new QueryChainWrapper<>( lotteryHistoryMapper )
                .eq( "lottery_id", lotteryId )
                .ne( "status", 0 )
                .orderByDesc( "id" )
                .select( "issue", "code", "analyse" )
                .list()
                .stream()
                .map( lotteryHistory -> {
                    RspLotteryHistory rspLotteryHistory = new RspLotteryHistory();
                    BeanUtils.copyProperties( lotteryHistory, rspLotteryHistory );
                    return rspLotteryHistory;
                } )
                .toList();
        for ( RspLotteryHistory rspLotteryHistory : rspLotteryHistories ) {
            this.extracted( lotteryId, rspLotteryHistory );
        }
        return rspLotteryHistories;
    }

    @Override
    public RspBase<RspBet> bet( ReqBet reqBet, PlatformUser platformUser ) {
        LocalDateTime now = LocalDateTime.now();
        if ( reqBet.getLotteryId() == 2001 && now.withSecond( 60 - Constants.BACCARAT_BEGIN_BET_TIME ).compareTo( now ) < 0 ) {
            return RspBase.businessError( "此时百家乐停止下注时间" );
        }
        List<LotteryGameVo> games = LotteryCacheUtils.me.getMethodGames( reqBet.getMethodId() );
        if ( games == null || games.size() == 0 ) {
            log.error( "投注信息有误methodId:{}", reqBet.getMethodId() );
            return RspBase.businessError( "投注信息有误!" );
        }
        if ( StringUtils.isBlank( reqBet.getBetIds() ) ) {
            return RspBase.businessError( "请选择投注信息" );
        }
        if ( !this.checkOkMethod( reqBet.getLotteryId(), reqBet.getMethodId() ) ) {
            return RspBase.businessError( "非法投注" );
        }
        LotteryBase lotteryBase = LotteryCacheUtils.me.getLotteryBase( reqBet.getLotteryId() );
        if ( lotteryBase == null ) {
            return RspBase.businessError( "彩票不存在" );
        }
        String[] betIds     = reqBet.getBetIds().split( "&" );
        String[] bet_select = new String[ betIds.length ];
        if ( reqBet.getChip() < 1 || reqBet.getChip() > 10000000 ) {
            log.error( "会员恶意投注:userid:{},chip:{}", platformUser.getId(), reqBet.getChip() );
            return RspBase.businessError( "非法筹码" );
        }
        BigDecimal cost = new BigDecimal( reqBet.getChip() * bet_select.length );
        if ( cost.compareTo( new BigDecimal( "10000000" ) ) > 0 ) {
            return RspBase.businessError( "最大投注金额不能超过1000万元" );
        }
        if ( cost.compareTo( new BigDecimal( "0" ) ) <= 0 ) {
            log.error( "会员恶意投注:userid:{},cost:{}", platformUser.getId(), cost );
            return RspBase.businessError( "异常投注" );
        }
        reqBet.setAnchor( -1 );
        Map<String, LotteryGameVo> gamesMap = games
                .stream()
                .collect( Collectors.toMap( game -> game.getId().toString(), Function.identity() ) );
        for ( int i = 0; i < betIds.length; i++ ) {
            if ( !reqBet.getMethodId().equals( gamesMap.get( betIds[ i ] ).getMethodId() ) ) {
                log.error( "会员投注信息有误methodId:{},betId:{}", reqBet.getMethodId(), betIds[ i ] );
                return RspBase.businessError( "投注信息有误" );
            }
            bet_select[ i ] = gamesMap.get( betIds[ i ] ).getInfo();
        }
        String          issue       = LotteryUtils.getLotteryIssue( lotteryBase.getCycle(), LocalDateTime.now() );
        String          lotteryName = lotteryBase.getName();
        RspBase<RspBet> rspBase     = lotteryBetService.userBet( platformUser, reqBet, bet_select, lotteryName, issue, cost );
        if ( rspBase.getCode() == 0 ) {
            this.pushLotteryCenter( platformUser, reqBet, bet_select, issue, lotteryName, ServletUtil.getIp() );
            if ( reqBet.getLotteryId() == 2001 ) {
                RspBet data = rspBase.getData();
                data.setTotalData( lotteryCountMapper.selectCountTotal( reqBet.getLotteryId(), issue, platformUser.getId() ) );
            }
        }
        return rspBase;
    }

    @Override
    public void computeResult( Integer lotteryId ) {
        for ( String historyId : lotteryHistoryMapper.selectIssueWaite( LocalDateTime.now().plusSeconds( 5 ), lotteryId ) ) {
            HistoryResult historyResult = null;
            try {
                historyResult = this.countLotteryResult( historyId, Integer.parseInt( historyId.split( "-" )[ 2 ] ),
                        LotteryCacheUtils.me.getOddsMap( LotteryUtils.getKindId( lotteryId ) ) );
            } catch ( Exception e ) {
                log.error( e.getMessage(), e );
            }
            if ( historyResult != null ) {
                LotteryTemp temp = LotteryCacheUtils.me.getLotteryTemp( lotteryId );
                if ( historyId.substring( 0, 13 ).compareTo( temp.getIssueJust() ) >= 0 ) {
                    temp.setCodeJust( historyResult.getCode() );
                    LotteryCacheUtils.me.setLotteryTemp( temp );

                    LotteryTemp lotteryTemp = new LotteryTemp();
                    lotteryTemp.setId( lotteryId );
                    lotteryTemp.setCodeJust( historyResult.getCode() );
                    lotteryTempMapper.updateById( lotteryTemp );

                    this.sendNewLotteryIssueMsg( Integer.parseInt( historyResult
                            .getId()
                            .split( "-" )[ 2 ] ), historyResult.getCode(), historyResult
                            .getId()
                            .substring( 0, 13 ), historyResult.getAnalyse(), historyResult.getAnalyseGameId() );
                }
            }
        }
    }

    private HistoryResult countLotteryResult( String historyId, int lotteryId, Map<String, BigDecimal> rateMap ) {
        LotteryInfo info      = lotteryInfoMapper.selectById( lotteryId );
        int         kindId    = LotteryUtils.getKindId( lotteryId );
        String      issueJust = historyId.substring( 0, 13 );

        HistoryResult h = new HistoryResult();
        h.setId( historyId );

        LotteryHistory update = new LotteryHistory();
        update.setId( historyId );
        update.setStatus( 1 );

        List<BetCount> betCountList = lotteryCountMapper.countBet( issueJust, lotteryId );
        Map<String, BigDecimal> betMap = betCountList
                .stream()
                .collect( Collectors.toMap( BetCount::getBetinfo, BetCount::getTotalbet ) );
        //派奖分布图
        Map<String, BigDecimal> peiMap      = new HashMap<>();
        long                    totalBet    = this.toPeiMap( rateMap, betMap, peiMap, lotteryId );
        BigDecimal              totalBetBig = new BigDecimal( totalBet );

        update.setTotalBet( totalBet );

        String toKillProfile = lotteryBetService.procherckQuzhiImport( lotteryId );

        if ( StringUtils.isNotBlank( toKillProfile ) && totalBet > 0 ) {
            Map<String, BigDecimal> betMapProfile = betCountList
                    .stream()
                    .filter( betCount -> toKillProfile.equals( betCount.getAgent() ) )
                    .collect( Collectors.toMap( BetCount::getBetinfo, BetCount::getTotalbet ) );
            //派奖分布图
            Map<String, BigDecimal> peiMapProfile      = new HashMap<>();
            long                    totalBetProfile    = this.toPeiMap( rateMap, betMapProfile, peiMapProfile, lotteryId );
            BigDecimal              totalBetBigProfile = new BigDecimal( totalBetProfile );

            List<String> resultList = null;
            if ( kindId >= 11 ) {
                // 杀率计算
                ExLottery           exLottery = exLotteryFactoryUtil.createExProcessor( kindId );
                Map<String, Object> resultMap = exLottery.killResult( peiMapProfile, totalBetBigProfile );

                resultList = ( List<String> ) resultMap.get( "resultsList" );
            } else if ( totalBetProfile > 0 ) {
                String result = countLotteryResultById( lotteryId, totalBetProfile, info.getKillRate(), peiMapProfile );
                if ( result != null ) {
                    log.info( "存储过程返回开奖结果:{}", result );
                    String[] re = result.split( "," );

                    resultList = Arrays.asList( re[ 0 ].split( "-" ) );
                }
            }
            if ( resultList != null ) {
                BigDecimal prize    = this.coutPrize( lotteryId, resultList, peiMap );
                BigDecimal killNeed = totalBetBig.subtract( prize ).divide( totalBetBig, 2, RoundingMode.HALF_UP );
                return getHistoryResult( h, resultList, update, prize, killNeed, 2, lotteryId, historyId );
            } else {
                log.warn( "单平台计算杀率开奖异常，投注金额{}, historyId：{}", totalBetBig, historyId );
            }
        } else if ( !isNoKill( lotteryId ) && info.getMinCost().compareTo( totalBetBig ) < 0 ) {
            List<String> resultList = null;
            BigDecimal   totalPrize = BigDecimal.ZERO;
            BigDecimal   killRate   = BigDecimal.ZERO;
            if ( lotteryId == 2001 ) {
                // 杀率计算
                ExLottery           exLottery = exLotteryFactoryUtil.createExProcessor( kindId );
                Map<String, Object> resultMap = exLottery.killResult( peiMap, totalBetBig );

                resultList = ( List<String> ) resultMap.get( "resultsList" );

                totalPrize = ( BigDecimal ) resultMap.get( "totalPrize" );
                killRate   = ( BigDecimal ) resultMap.get( "killRate" );
            } else {
                String result = countLotteryResultById( lotteryId, totalBet, info.getKillRate(), peiMap );
                if ( result != null ) {
                    log.info( "存储过程返回开奖结果:{}", result );
                    String[] re = result.split( "," );

                    resultList = Arrays.asList( re[ 0 ].split( "-" ) );
                    totalPrize = new BigDecimal( re[ 2 ] );
                    killRate   = new BigDecimal( re[ 1 ] );
                }
            }
            if ( resultList != null ) {
                return getHistoryResult( h, resultList, update, totalPrize, killRate, 1, lotteryId, historyId );
            } else {
                log.error( "计算杀率开奖异常，走默认程序开奖，投注金额{}, historyId：{}", totalBetBig, historyId );
            }
        }

        List<String> listTem = this.randomResult( kindId );

        BigDecimal killNeed = BigDecimal.ZERO;
        BigDecimal prize    = BigDecimal.ZERO;
        if ( totalBet > 0 ) {
            prize    = this.coutPrize( kindId, listTem, peiMap );
            killNeed = totalBetBig.subtract( prize ).divide( totalBetBig, 2, RoundingMode.HALF_UP );
        }
        log.info( "投注金额{}小于免杀金额{},无杀派奖 historyId：{}", totalBetBig, info.getMinCost(), historyId );
        return getHistoryResult( h, listTem, update, prize, killNeed, 0, lotteryId, historyId );
    }

    private String countLotteryResultById( int lotteryId, long totalBet, BigDecimal killRate, Map<String, BigDecimal> betMap ) {
        ExLottery exLottery       = exLotteryFactoryUtil.createExProcessor( LotteryUtils.getKindId( lotteryId ) );
        String    concatBetString = exLottery.concatBetString( betMap );
        try {
            return lotteryHistoryMapper.countLotteryResult( lotteryId, totalBet, killRate, concatBetString, new HashMap<>() );
        } catch ( Exception e ) {
            log.error( "$$$###### {}调用存储过程出错,totalBet:{},killRate：{},betString:{}", lotteryId, totalBet, killRate,
                    concatBetString );
            log.error( e.getMessage(), e );
            return null;
        }
    }

    private long toPeiMap( Map<String, BigDecimal> rateMap, Map<String, BigDecimal> betMap, Map<String, BigDecimal> peiMap,
                           int lotteryId ) {
        long totalBet = 0;
        for ( String bt : rateMap.keySet() ) {
            BigDecimal temBet = betMap.get( bt );
            if ( temBet == null ) {
                peiMap.put( bt, BigDecimal.ZERO );
            } else {
                totalBet += temBet.intValue();
                peiMap.put( bt, temBet.multiply( rateMap.get( bt ) ).setScale( 2, RoundingMode.DOWN ) );
            }
        }
        if ( lotteryId == 2001 ) {
            peiMap.put( "庄和", betMap.get( "庄" ) == null ? BigDecimal.ZERO : betMap.get( "庄" ) );
            peiMap.put( "闲和", betMap.get( "闲" ) == null ? BigDecimal.ZERO : betMap.get( "闲" ) );
        }
        return totalBet;
    }

    public List<String> randomResult( Integer kindId ) {
        ExLottery exLottery = exLotteryFactoryUtil.createExProcessor( kindId );
        return exLottery.randomResult();
    }

    public BigDecimal coutPrize( int kindId, List<String> list, Map<String, BigDecimal> peiMap ) {
        ExLottery exLottery = exLotteryFactoryUtil.createExProcessor( kindId );
        return exLottery.coutPrize( list, peiMap );
    }

    private HistoryResult getHistoryResult( HistoryResult h, List<String> resultList, LotteryHistory update, BigDecimal prize,
                                            BigDecimal killNeed, int ctl, Integer lotteryId, String historyId ) {
        h.setCode( String.join( " ", resultList ) );

        update.setCode( String.join( " ", resultList ) );
        update.setTotalPrize( prize );
        update.setKillRate( killNeed );
        update.setCtl( ctl );
        if ( lotteryId == 2001 ) {
            update.setAnalyse( ExBaccarat.getBaccaratAnalyse( resultList ) );
            h.setAnalyse( ExBaccarat.getBaccaratWinBrand( resultList ) );
            h.setAnalyseGameId( this.getGameIdByMethodName( lotteryId, update.getAnalyse() ) );
        }
        if ( lotteryHistoryMapper.updateAlreadyPrize( historyId, update.getStatus(), update.getCtl(), update.getTotalBet(),
                update.getCode(), update.getTotalPrize(), update.getKillRate(), update.getAnalyse() )
                > 0 ) {
            h.setCode( update.getCode() );
            return h;
        } else {
            log.error( "重复开奖了historyId：{}", historyId );
            return null;
        }
    }

    public String getGameIdByMethodName( Integer lotteryId, String methodName ) {
        for ( RspMethod m : LotteryCacheUtils.me.getKindMethods( LotteryUtils.getKindId( lotteryId ) ) ) {
            for ( LotteryGameVo gameVo : LotteryCacheUtils.me.getMethodGames( m.getId() ) ) {
                if ( gameVo.getInfo().equals( methodName ) ) {
                    return gameVo.getId().toString();
                }
            }
        }
        return null;
    }

    private boolean isNoKill( Integer lotteryId ) {
        boolean            noKill             = false;
        LotteryPrizeconfig lotteryPrizeConfig = lotteryPrizeconfigMapper.selectById( lotteryId );

        if ( lotteryPrizeConfig != null ) {
            LotteryPrizepool lotteryPrizePool = lotteryPrizepoolMapper.selectById(
                    lotteryPrizeConfig.getLotteryId() + ":" + LocalDateTimeUtils.format( LocalDate.now() ) );

            if ( lotteryPrizePool != null ) {
                if ( ArrayUtils.contains( lotteryPrizeConfig
                        .getLotteryNokillratehour()
                        .split( "," ), String.valueOf( lotteryPrizePool.getLotteryHour() ) ) ) {
                    noKill = true;
                    //增加杀不确定性
                    if ( lotteryPrizeConfig.getLotteryRandom() != null ) {
                        int random = RandomUtils.randomIntWithMax( 1, 100 );
                        if ( random < lotteryPrizeConfig.getLotteryRandom() ) {
                            noKill = false;
                        }
                    }
                }
                if ( lotteryPrizePool.getKillRate() == null ) {
                    lotteryPrizePool.setKillRate( BigDecimal.ZERO );
                }
                if ( lotteryPrizePool.getKillRate().compareTo( lotteryPrizeConfig.getLotteryKillrate() ) > 0 ) {
                    noKill = true;
                }
            }

            //增加不杀不确定性
            if ( !noKill && lotteryPrizeConfig.getLotteryRandom() != null ) {
                int random = RandomUtils.randomIntWithMax( 1, 100 );
                if ( random < lotteryPrizeConfig.getLotteryRandom() ) {
                    noKill = true;
                }
            }
        }
        return noKill;
    }

    private void sendNewLotteryIssueMsg( int id, String code, String issue, String analyse, String analyseGameId ) {
        LotteryBase info = LotteryCacheUtils.me.getLotteryBase( id );
        if ( info == null ) {
            log.error( "彩票信息为空" + id );
            return;
        }
        if ( info.getCycle() != 1 ) {
            return;
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put( "id", id );
        data.put( "name", info.getName() );
        data.put( "lotteryType", info.getName().substring( 2 ) );
        data.put( "lotteryIssue", issue );
        data.put( "code", code );
        data.put( "analyse", analyse );
        data.put( "analyseGameId", analyseGameId );

        HashMap<String, Object> ext = new HashMap<>();
        ext.put( "type", 101 );
        ext.put( "data", data );
        // 4=开奖公告
        ext.put( "act", 4 );

        imServerUtils.sendOnlineGroupMessage( ext );
    }

    private void pushLotteryCenter( PlatformUser platformUser, ReqBet reqBet, String[] bet_select, String issue,
                                    String lotteryName, String ip ) {
        if ( platformUser.getStatus() != 2 ) {
            if ( ip != null && ip.length() > 99 ) {
                ip = ip.substring( 0, 80 );
            }
            List<LotteryCount> list = new ArrayList<>();
            for ( String bet : bet_select ) {
                LotteryCount lotteryCount = new LotteryCount();
                lotteryCount.setAgent( profile );
                lotteryCount.setIssue( issue );
                lotteryCount.setChip( new BigDecimal( reqBet.getChip() ) );
                lotteryCount.setLotteryId( reqBet.getLotteryId() );
                lotteryCount.setBetInfo( bet );
                lotteryCount.setMemberId( platformUser.getId() );
                lotteryCount.setIp( ip );
                list.add( lotteryCount );
            }
            if ( isLotteryCenter() ) {
                lotteryCountMapper.insertBatch( list );
            } else {
                lotteryCountMapper.insertBatchCenter( lotteryAgent, list );
            }
        }

        if ( reqBet.getLotteryId() == 2001 ) {
            int cost = reqBet.getChip() * bet_select.length;
            this.lotteryBetBroadcast( platformUser, reqBet.getLotteryId(), reqBet.getMethodId(), reqBet.getBetIds(),
                    lotteryName, cost, reqBet.getChip() );
        }
    }

    private void lotteryBetBroadcast( PlatformUser platformUser, Integer lotteryId, Integer methodId, String bet_select,
                                      String lotteryName, int cost, Integer per_price ) {
        String                  msg  = platformUser.getNickName() + "在" + lotteryName + "中," + "下注了" + cost + "元";
        HashMap<String, Object> data = new HashMap<>();
        data.put( "userId", platformUser.getId() );
        data.put( "id", lotteryId );
        data.put( "msg", msg );
        data.put( "at", 2 );//1= 开新期2=投注  3=中奖

        HashMap<String, Object> betMap = new HashMap<>();
        betMap.put( "KindID", lotteryId );
        betMap.put( "method_id", methodId );
        betMap.put( "bet_select", bet_select.replace( "&", "_" ) );
        betMap.put( "per_price", per_price );
        data.put( "betInfo", betMap );

        HashMap<String, Object> ext = new HashMap<>();
        ext.put( "type", 101 );
        ext.put( "sender", platformUser.toUserInfoMap() );
        ext.put( "data", data );
        // 1 =新开启 2 = 投注, 中奖
        ext.put( "act", 2 );
        ext.put( "groupId", profile );

        imServerUtils.sendOnlineGroupMessage( ext );
    }

    public boolean checkOkMethod( Integer lotteryId, Integer methodId ) {
        for ( RspMethod m : LotteryCacheUtils.me.getKindMethods( LotteryUtils.getKindId( lotteryId ) ) ) {
            if ( m.getId().equals( methodId ) ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void awardLottery( Integer lotteryId ) {
        for ( HistoryResult result : lotteryHistoryMapper.selectHistoryResult( lotteryId, 1 ) ) {
            LotteryHistory updateResult = new LotteryHistory();
            updateResult.setId( result.getId() );
            updateResult.setStatus( 2 );
            lotteryHistoryMapper.updateById( updateResult );

            try {
                this.awardByLotteryResult( result );
            } catch ( Exception e ) {
                log.error( "批量派奖异常-尝试在下个周期重新派奖historyId:{}", result.getId(), e );
                awardRepair( result.getId() );
            }
        }
    }

    private void awardByLotteryResult( HistoryResult result ) {
        Integer  lotteryId    = Integer.parseInt( result.getId().split( "-" )[ 2 ] );
        String   issue        = result.getId().substring( 0, 13 );
        String[] officialCode = result.getCode().split( " " );
        //查询所有下注
        List<LotteryBet> betList = lotteryBetMapper.selectLotteryWaiteList( issue, lotteryId );
        if ( betList.size() == 0 ) {
            return;
        }

        LocalDateTime           updateTime = LocalDateTime.now();
        List<LotteryBet>        updateList = new ArrayList<>();
        Map<String, BigDecimal> prizeMap   = new HashMap<>();
        Map<String, BigDecimal> nowMoney   = new HashMap<>();
        BigDecimal              totalBet   = BigDecimal.ZERO;
        BigDecimal              totalPrize = BigDecimal.ZERO;
        for ( LotteryBet bet : betList ) {
            BigDecimal prize = handlePrize( lotteryId, bet.getMethodId(), officialCode, bet.getChip(), bet.getBetSelect() );
            bet.setPrize( prize );

            LotteryBet updateBet = new LotteryBet();
            updateBet.setId( bet.getId() );
            updateBet.setPrize( prize );
            updateBet.setMemberId( bet.getMemberId() );
            updateBet.setCode( result.getCode() );
            updateBet.setUpdateTime( updateTime );
            if ( prize.compareTo( BigDecimal.ZERO ) > 0 ) {
                updateBet.setStatus( 1 );
                if ( prizeMap.containsKey( bet.getMemberId() ) ) {
                    prizeMap.put( bet.getMemberId(), prizeMap.get( bet.getMemberId() ).add( prize ) );
                } else {
                    prizeMap.put( bet.getMemberId(), prize );
                }
            } else {
                updateBet.setStatus( 2 );
            }
            updateList.add( updateBet );

            totalPrize = totalPrize.add( prize );
            totalBet   = totalBet.add( bet.getCost() );
        }
        LotteryBase lotteryBase = LotteryCacheUtils.me.getLotteryBase( lotteryId );
        lotteryHistoryService.awardByLotteryResult( updateList, prizeMap, result.getId(), nowMoney, lotteryBase.getName() );

        Integer count = lotteryBetMapper.selectLotteryWaiteCount( issue, lotteryId );
        if ( count != null && count > 0 ) {
            awardRepair( result.getId() );
            log.error( "派奖结束后检查发现有未派奖-尝试在下个周期重新派奖historyId:{}", result.getId() );
        }

        if ( !isLotteryCenter() ) {
            LotteryHistory db = lotteryHistoryMapper.selectById( result.getId() );

            if ( db.getLotteryId() == 1004 ) {
                log.error( "更新彩票历史记录 - 1:{};2:{}", db.getTotalPrize(), totalPrize );
            }

            totalPrize = totalPrize.add( db.getTotalPrize() );
            totalBet   = totalBet.add( new BigDecimal( db.getTotalBet() ) );
            BigDecimal killRate = BigDecimal.ZERO;
            if ( totalBet.intValue() > 0 ) {
                killRate = totalBet.subtract( totalPrize );
                killRate = killRate.divide( totalBet, 4, RoundingMode.DOWN );
            }

            LotteryHistory up = new LotteryHistory();
            up.setKillRate( killRate );
            up.setId( result.getId() );
            up.setTotalPrize( totalPrize );
            up.setTotalBet( totalBet.longValue() );
            lotteryHistoryService.updateById( up );
        }

        /*BigDecimal boardCast = configEnvCacheUtil.getConfBd( "lottery_boardcast_mony" );
        for ( LotteryBet bet : betList ) {
            if ( bet.getPrize().compareTo( boardCast ) >= 0 ) {
                bet.setLotteryId( lotteryId );
                this.sendLottertAwardMsg( bet, nowMoney.get( bet.getMemberId() ), boardCast );
            }
        }*/
    }

    private void sendLottertAwardMsg( LotteryBet bet, BigDecimal nowMoney, BigDecimal boardCast ) {
        String token    = redisUtils.strGet( Constants.MEMBER_LOGIN_USER + bet.getMemberId() );
        String nickName = null;
        if ( StringUtils.isNotBlank( token ) ) {
            Object platformUserStr = redisUtils.hGet( Constants.MEMBER_LOGIN_TOKEN + token, "platformUserStr" );
            if ( platformUserStr != null ) {
                PlatformUser platformUser = JsonUtil.json2Object( platformUserStr.toString(), PlatformUser.class );
                nickName = platformUser.getNickName();
            }
        }
        if ( StringUtils.isBlank( nickName ) ) {
            PlatformUser platformUser = memberInfoMapper.selectPlatformUserByUserId( bet.getMemberId() );
            if ( platformUser != null ) {
                nickName = platformUser.getNickName();
            }
        }
        if ( StringUtils.isBlank( nickName ) ) {
            return;
        }
        LotteryBase lotteryInfo = LotteryCacheUtils.me.getLotteryBase( bet.getLotteryId() );

        String                  prize = bet.getPrize().setScale( 2, RoundingMode.HALF_UP ).toString();
        String                  msg   = "恭喜会员:" + nickName + "在" + lotteryInfo.getName() + "中奖" + bet.getPrize() + "元";
        HashMap<String, Object> data  = new HashMap<>();
        data.put( "msg", msg );
        data.put( "nickName", nickName );
        data.put( "lotteryName", lotteryInfo.getName() );
        data.put( "prize", prize );

        HashMap<String, Object> ext = new HashMap<>();
        ext.put( "type", 103 );
        ext.put( "data", data );
        imServerUtils.sendOnlineGroupMessage( ext );
    }

    private void awardRepair( String id ) {
        LotteryHistory update = new LotteryHistory();
        update.setId( id );
        update.setStatus( 1 );
        lotteryHistoryMapper.updateById( update );
    }

    private BigDecimal handlePrize( Integer lotteryId, Integer methodId, String[] officialCode, BigDecimal chip,
                                    String betSelect ) {
        ExLottery exLottery = exLotteryFactoryUtil.createExProcessor( LotteryUtils.getKindId( lotteryId ) );
        return exLottery.handle( methodId, officialCode, chip, betSelect );
    }

    @Override
    public List<RuleVo> getLotteryRule( Integer lotteryId ) {
        return LotteryCacheUtils.me.getLotteryRule( LotteryUtils.getKindId( lotteryId ) );
    }

    @Override
    public void catchResult( Integer lotteryId ) {
        List<LotteryTemp>   temList        = new ArrayList<>();
        List<HistoryResult> historyResults = lotteryHistoryService.selectResultWaite( lotteryAgent, lotteryId );
        for ( HistoryResult historyResult : historyResults ) {
            LotteryTemp temp = LotteryCacheUtils.me.getLotteryTemp( lotteryId );
            if ( historyResult.getId().substring( 0, 13 ).compareTo( temp.getIssueJust() ) >= 0 ) {
                temp.setCodeJust( historyResult.getCode() );
                LotteryCacheUtils.me.setLotteryTemp( temp );

                LotteryTemp up = new LotteryTemp();
                up.setId( lotteryId );
                up.setCodeJust( historyResult.getCode() );
                temList.add( up );
            }

            historyResult.setAnalyseGameId( this.getGameIdByMethodName( lotteryId, historyResult.getAnalyse() ) );
        }
        for ( LotteryTemp lotteryTemp : temList ) {
            lotteryTempMapper.updateById( lotteryTemp );
        }
    }
}
