package tv.game88.lottery.api.cache;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.lottery.api.dto.*;
import tv.game88.lottery.api.entity.LotteryTemp;
import tv.game88.lottery.api.mapper.*;
import tv.game88.lottery.api.utils.LotteryUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LotteryCacheUtils {
    public static LotteryCacheUtils me;

    @PostConstruct
    void init() {
        me = this;
    }

    private static final String LOTTERY_INFO_KEY   = Constants.LOTTERY_PREX + "info";
    private static final String LOTTERY_METHOD_KEY = Constants.LOTTERY_PREX + "method";
    private static final String LOTTERY_ODDS_KEY   = Constants.LOTTERY_PREX + "odds:";

    private static final String LOTTERY_TEMP_KEY = Constants.LOTTERY_PREX + "temp:";
    private static final String LOTTERY_RULE_KEY = Constants.LOTTERY_PREX + "rule:";

    private static final String LOTTERY_INFO_BASE_KEY   = Constants.LOTTERY_PREX + "infoBase";
    private static final String LOTTERY_KIND_METHOD_KEY = Constants.LOTTERY_PREX + "kindMethod";
    private static final String LOTTERY_METHOD_GAME_KEY = Constants.LOTTERY_PREX + "methodGame";


    @Resource
    private RedisUtils redisUtils;

    @Resource
    private LotteryGameMapper   lotteryGameMapper;
    @Resource
    private LotteryInfoMapper   lotteryInfoMapper;
    @Resource
    private LotteryMethodMapper lotteryMethodMapper;
    @Resource
    private LotteryTempMapper   lotteryTempMapper;
    @Resource
    private LotteryRuleMapper   lotteryRuleMapper;

    public Map<String, BigDecimal> getOddsMap( Integer kindId ) {
        if ( !redisUtils.exists( LOTTERY_ODDS_KEY + kindId ) ) {
            this.getMethodGames( null );
        }
        return redisUtils
                .hGetAll( LOTTERY_ODDS_KEY + kindId )
                .entrySet()
                .stream()
                .collect( Collectors.toMap( e -> e.getKey().toString(), e -> new BigDecimal( e.getValue().toString() ) ) );
    }

    public LocalMethod getLocalMethod( Integer methodId ) {
        if ( !redisUtils.exists( LOTTERY_METHOD_KEY ) ) {
            this.getMethodGames( null );
        }
        Object o = redisUtils.hGet( LOTTERY_METHOD_KEY, String.valueOf( methodId ) );
        return o == null ? null : JsonUtil.json2Object( o.toString(), LocalMethod.class );
    }

    public LotteryBase getLotteryBase( Integer infoId ) {
        if ( !redisUtils.exists( LOTTERY_INFO_BASE_KEY ) ) {
            // infoId:infoBse
            Map<Integer, LotteryBase> lotteryBaseMap = new HashMap<>();
            for ( RspLotteryInfo info : this.getRspLotteryInfo() ) {
                LotteryBase base = new LotteryBase();
                base.setId( info.getId() );
                base.setName( info.getName() );
                base.setCycle( info.getCycle() );
                if ( info.getId() == 2001 ) {
                    base.setBetBeginSec( Constants.BACCARAT_BEGIN_BET_TIME );
                }
                lotteryBaseMap.put( info.getId(), base );
            }
            redisUtils.unlink( LOTTERY_INFO_BASE_KEY );
            redisUtils.hMSet( LOTTERY_INFO_BASE_KEY, lotteryBaseMap
                    .entrySet()
                    .stream()
                    .collect( Collectors.toMap( e -> e.getKey().toString(), e -> JsonUtil.object2Json( e.getValue() ) ) ) );
            return lotteryBaseMap.get( infoId );
        }
        Object o = redisUtils.hGet( LOTTERY_INFO_BASE_KEY, infoId.toString() );
        return StringUtils.isNull( o ) ? null : JsonUtil.json2Object( o.toString(), LotteryBase.class );
    }

    public List<RspMethod> getKindMethods( Integer kindId ) {
        if ( !redisUtils.exists( LOTTERY_KIND_METHOD_KEY ) ) {
            Map<Integer, List<RspMethod>> kindsMethodsMap = new HashMap<>();
            for ( RspLotteryInfo info : this.getRspLotteryInfo() ) {
                int kindId_ = LotteryUtils.getKindId( info.getId() );
                if ( !kindsMethodsMap.containsKey( kindId_ ) ) {
                    kindsMethodsMap.put( kindId_, new QueryChainWrapper<>( lotteryMethodMapper )
                            .eq( "lottery_type", info.getType() )
                            .orderByAsc( "sort" )
                            .list()
                            .stream()
                            .map( lotteryMethod -> {
                                RspMethod rspMethod = new RspMethod();
                                rspMethod.setId( lotteryMethod.getId() );
                                rspMethod.setName( lotteryMethod.getName() );
                                return rspMethod;
                            } )
                            .toList() );
                }
            }
            redisUtils.unlink( LOTTERY_KIND_METHOD_KEY );
            redisUtils.hMSet( LOTTERY_KIND_METHOD_KEY, kindsMethodsMap
                    .entrySet()
                    .stream()
                    .collect( Collectors.toMap( e -> e.getKey().toString(), e -> JsonUtil.object2Json( e.getValue() ) ) ) );
            return kindsMethodsMap.get( kindId );
        }
        Object o = redisUtils.hGet( LOTTERY_KIND_METHOD_KEY, kindId.toString() );
        return StringUtils.isNull( o ) ? null : JsonUtil.json2Array( o.toString(), new TypeReference<>() {} );
    }

    public List<LotteryGameVo> getMethodGames( Integer methodId ) {
        if ( !redisUtils.exists( LOTTERY_METHOD_GAME_KEY ) ) {
            // methodId:gameList
            Map<Integer, List<LotteryGameVo>> methodsGamesMap = new HashMap<>();
            Map<String, String>               methodMap       = new HashMap<>();
            List<LotteryGameVo> gamelistall = new QueryChainWrapper<>( lotteryGameMapper )
                    .orderByAsc( "sort" )
                    .list()
                    .stream()
                    .map( game -> {
                        LotteryGameVo lotteryGameVo = new LotteryGameVo();
                        BeanUtils.copyProperties( game, lotteryGameVo );
                        return lotteryGameVo;
                    } )
                    .toList();

            for ( RspLotteryInfo info : this.getRspLotteryInfo() ) {
                int             kindId      = LotteryUtils.getKindId( info.getId() );
                List<RspMethod> kindMethods = this.getKindMethods( kindId );
                if ( CollectionUtils.isEmpty( kindMethods ) ) {
                    continue;
                }
                Map<String, String> oddsMap = new HashMap<>();
                for ( RspMethod md : kindMethods ) {
                    methodsGamesMap.put( md.getId(), gamelistall
                            .stream()
                            .filter( u -> md.getId().equals( u.getMethodId() ) )
                            .collect( Collectors.toList() ) );
                    methodMap.put( md.getId().toString(), JsonUtil.object2Json( md ) );
                    for ( LotteryGameVo g : methodsGamesMap.get( md.getId() ) ) {
                        oddsMap.put( g.getInfo(), g.getOdds().toString() );
                    }
                }
                redisUtils.unlink( LOTTERY_ODDS_KEY + kindId );
                redisUtils.hMSet( LOTTERY_ODDS_KEY + kindId, oddsMap );
            }
            redisUtils.unlink( LOTTERY_METHOD_KEY );
            redisUtils.hMSet( LOTTERY_METHOD_KEY, methodMap );
            redisUtils.unlink( LOTTERY_METHOD_GAME_KEY );
            redisUtils.hMSet( LOTTERY_METHOD_GAME_KEY, methodsGamesMap
                    .entrySet()
                    .stream()
                    .collect( Collectors.toMap( e -> e.getKey().toString(), e -> JsonUtil.object2Json( e.getValue() ) ) ) );
            return methodsGamesMap.get( methodId );
        }
        if ( methodId == null ) {
            return null;
        }
        Object o = redisUtils.hGet( LOTTERY_METHOD_GAME_KEY, methodId.toString() );
        return StringUtils.isNull( o ) ? null : JsonUtil.json2Array( o.toString(), new TypeReference<>() {} );
    }

    public List<RspLotteryInfo> getRspLotteryInfo() {
        String s = redisUtils.strGet( LOTTERY_INFO_KEY );
        if ( s == null || !redisUtils.exists( LOTTERY_INFO_KEY ) ) {
            List<RspLotteryInfo> rspLotteryInfos = new QueryChainWrapper<>( lotteryInfoMapper )
                    .eq( "effect", 1 )
                    .orderByAsc( "id" )
                    .list()
                    .stream()
                    .map( game -> {
                        RspLotteryInfo rspLotteryInfo = new RspLotteryInfo();
                        BeanUtils.copyProperties( game, rspLotteryInfo );
                        return rspLotteryInfo;
                    } )
                    .toList();
            redisUtils.strSet( LOTTERY_INFO_KEY, JsonUtil.object2Json( rspLotteryInfos ) );
            return rspLotteryInfos;
        }
        return JsonUtil.json2Array( s, new TypeReference<>() {} );
    }

    public LotteryTemp getLotteryTemp( Integer lotteryId ) {
        String s = redisUtils.strGet( LOTTERY_TEMP_KEY + lotteryId );
        if ( StringUtils.isBlank( s ) || !redisUtils.exists( LOTTERY_TEMP_KEY + lotteryId ) ) {
            LotteryTemp lotteryTemp = lotteryTempMapper.selectById( lotteryId );
            this.setLotteryTemp( lotteryTemp );
            return lotteryTemp;
        }
        return JsonUtil.json2Object( s, LotteryTemp.class );
    }

    public void setLotteryTemp( LotteryTemp lotteryTemp ) {
        if ( lotteryTemp != null ) {
            redisUtils.strSet( LOTTERY_TEMP_KEY + lotteryTemp.getId(), JsonUtil.object2Json( lotteryTemp ) );
        }
    }

    public List<RuleVo> getLotteryRule( Integer kindId ) {
        //        String s = redisUtils.strGet( LOTTERY_RULE_KEY + kindId );
        //        if ( StringUtils.isBlank( s ) || !redisUtils.exists( LOTTERY_RULE_KEY + kindId ) ) {
        List<RuleVo> ruleVos = new QueryChainWrapper<>( lotteryRuleMapper )
                .eq( "kind", kindId )
                .orderByAsc( "sort" )
                .select( "name", "des" )
                .list()
                .stream()
                .map( rule -> {
                    RuleVo ruleVo = new RuleVo();
                    BeanUtils.copyProperties( rule, ruleVo );
                    return ruleVo;
                } )
                .toList();
        redisUtils.strSet( LOTTERY_RULE_KEY + kindId, JsonUtil.object2Json( ruleVos ) );
        return ruleVos;
//    }
//        return JsonUtil.json2Array( ruleVos, new TypeReference<>() {} );
    }

    public void clear() {
        redisUtils.unlink( LOTTERY_INFO_KEY );
        redisUtils.unlink( LOTTERY_INFO_BASE_KEY );
        redisUtils.unlink( LOTTERY_METHOD_KEY );
        redisUtils.unlink( LOTTERY_METHOD_GAME_KEY );
        redisUtils.unlink( LOTTERY_KIND_METHOD_KEY );
    }
}
