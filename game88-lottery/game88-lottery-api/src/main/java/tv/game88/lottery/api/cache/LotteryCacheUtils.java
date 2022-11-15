package tv.game88.lottery.api.cache;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.lottery.api.dto.*;
import tv.game88.lottery.api.mapper.LotteryGameMapper;
import tv.game88.lottery.api.mapper.LotteryInfoMapper;
import tv.game88.lottery.api.mapper.LotteryMethodMapper;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LotteryCacheUtils {
    private static final String LOTTERY_INFO_KEY   = Constants.LOTTERY_PREX + "info";
    private static final String LOTTERY_METHOD_KEY = Constants.LOTTERY_PREX + "method";
    private static final String LOTTERY_ODDS_KEY   = Constants.LOTTERY_PREX + "odds:";

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

    private void initBase() {
        // infoId:infoBse
        Map<Integer, LotteryBase> lotteryBaseMap = new HashMap<>();
        // kindId:methodList
        Map<Integer, List<RspMethod>> kindsMethodsMap = new HashMap<>();
        // methodId:gameList
        Map<Integer, List<LotteryGameVo>> methodsGamesMap = new HashMap<>();

        Map<String, String> methodMap = new HashMap<>();

        List<LotteryGameVo> gamelistall = new QueryChainWrapper<>( lotteryGameMapper )
                .orderByAsc( "index" )
                .list()
                .stream()
                .map( game -> {
                    LotteryGameVo lotteryGameVo = new LotteryGameVo();
                    BeanUtils.copyProperties( game, lotteryGameVo );
                    return lotteryGameVo;
                } )
                .toList();
        for ( RspLotteryInfo info : this.getRspLotteryInfo() ) {
            LotteryBase base = new LotteryBase();
            base.setId( info.getId() );
            base.setName( info.getName() );
            base.setCycle( info.getCycle() );
            if ( info.getId() == 2001 ) {
                base.setBetBeginSec( 10 );
            }
            lotteryBaseMap.put( info.getId(), base );
            int kindId = base.getKind();
            if ( !kindsMethodsMap.containsKey( kindId ) ) {
                kindsMethodsMap.put( kindId, new QueryChainWrapper<>( lotteryMethodMapper )
                        .eq( "lottery_type", info.getType() )
                        .orderByAsc( "order" )
                        .list()
                        .stream()
                        .map( lotteryMethod -> {
                            RspMethod rspMethod = new RspMethod();
                            rspMethod.setId( lotteryMethod.getId() );
                            rspMethod.setName( lotteryMethod.getName() );
                            return rspMethod;
                        } )
                        .toList() );
                for ( LocalMethod md : kindsMethodsMap.get( kindId ) ) {
                    methodsGamesMap.put( md.getId(), gamelistall
                            .stream()
                            .filter( u -> md.getId().equals( u.getMethodId() ) )
                            .collect( Collectors.toList() ) );
                    methodMap.put( md.getId().toString(), JsonUtil.object2Json( md ) );
                    Map<String, String> oddsMap = new HashMap<>();
                    for ( LotteryGameVo g : methodsGamesMap.get( md.getId() ) ) {
                        oddsMap.put( g.getInfo(), g.getOdds() );
                    }
                    redisUtils.unlink( LOTTERY_ODDS_KEY + kindId );
                    redisUtils.hMSet( LOTTERY_ODDS_KEY + kindId, oddsMap );
                }
            }
        }
        redisUtils.unlink( LOTTERY_METHOD_KEY );
        redisUtils.hMSet( LOTTERY_METHOD_KEY, methodMap );
        redisUtils.unlink( LOTTERY_INFO_BASE_KEY );
        redisUtils.hMSet( LOTTERY_INFO_BASE_KEY, lotteryBaseMap
                .entrySet()
                .stream()
                .collect( Collectors.toMap( Object::toString, JsonUtil::object2Json ) ) );
        redisUtils.unlink( LOTTERY_KIND_METHOD_KEY );
        redisUtils.hMSet( LOTTERY_KIND_METHOD_KEY, kindsMethodsMap
                .entrySet()
                .stream()
                .collect( Collectors.toMap( Object::toString, JsonUtil::object2Json ) ) );
        redisUtils.unlink( LOTTERY_METHOD_GAME_KEY );
        redisUtils.hMSet( LOTTERY_METHOD_GAME_KEY, methodsGamesMap
                .entrySet()
                .stream()
                .collect( Collectors.toMap( Object::toString, JsonUtil::object2Json ) ) );
    }

    public List<RspLotteryInfo> getRspLotteryInfo() {
        if ( !redisUtils.exists( LOTTERY_INFO_KEY ) ) {
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
        String s = redisUtils.strGet( LOTTERY_INFO_KEY );
        return StringUtils.isBlank( s ) ? null : JsonUtil.json2Array( s, new TypeReference<>() {} );
    }
}
