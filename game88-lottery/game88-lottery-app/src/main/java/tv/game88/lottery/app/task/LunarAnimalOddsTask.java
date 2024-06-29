package tv.game88.lottery.app.task;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.lottery.api.cache.LotteryCacheUtils;
import tv.game88.lottery.api.entity.LotteryGame;
import tv.game88.lottery.api.entity.LotteryMethod;
import tv.game88.lottery.api.mapper.LotteryGameMapper;
import tv.game88.lottery.api.mapper.LotteryMethodMapper;
import tv.game88.lottery.api.utils.LunarAnimalUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 农历跨年替换处理赔率
 */
@Log4j2
@Component
public class LunarAnimalOddsTask {
    @Resource
    private RedisUtils          redisUtils;
    @Resource
    private LotteryGameMapper   lotteryGameMapper;
    @Resource
    private LotteryMethodMapper lotteryMethodMapper;

    @Scheduled( cron = "0 0 0 * * ?" )
    public void runTask() {
        if ( !redisUtils.lock( Constants.LOTTERY_PREX + "LunarAnimalOdds", 20 ) ) {
            return;
        }

        LocalDate today     = LocalDate.now();
        LocalDate yesterday = today.minusDays( 1 );

        String todayAnimal     = LunarAnimalUtils.getAnimal( LocalDateTimeUtils.format( today ) );
        String yesterdayAnimal = LunarAnimalUtils.getAnimal( LocalDateTimeUtils.format( yesterday ) );

        // 如果相同,那么就不是跨年,不用往下执行
        if ( todayAnimal.equals( yesterdayAnimal ) ) {
            return;
        }
        Set<Integer> methodIds = new QueryChainWrapper<>( lotteryMethodMapper )
                .like( "name", "生肖" )
                .or()
                .like( "name", "特肖" )
                .or()
                .like( "name", "连肖" )
                .select( "id" )
                .list()
                .stream()
                .map( LotteryMethod::getId )
                .collect( Collectors.toSet() );
        for ( Integer methodId : methodIds ) {
            List<LotteryGame> lotteryGames = new QueryChainWrapper<>( lotteryGameMapper )
                    .eq( "method_id", methodId )
                    .in( "info", todayAnimal, yesterdayAnimal )
                    .select( "id", "info", "odds" )
                    .list();
            if ( lotteryGames.size() != 2 ) {
                continue;
            }

            Integer    todayAnimalGameId     = null;
            BigDecimal todayAnimalOdds       = null;
            Integer    yesterdayAnimalGameId = null;
            BigDecimal yesterdayAnimalOdds   = null;
            for ( LotteryGame lotteryGame : lotteryGames ) {
                if ( todayAnimal.equals( lotteryGame.getInfo() ) ) {
                    todayAnimalGameId = lotteryGame.getId();
                    todayAnimalOdds   = lotteryGame.getOdds();
                } else {
                    yesterdayAnimalGameId = lotteryGame.getId();
                    yesterdayAnimalOdds   = lotteryGame.getOdds();
                }
            }

            if ( todayAnimalGameId != null && yesterdayAnimalOdds != null ) {
                LotteryGame update = new LotteryGame();
                update.setId( todayAnimalGameId );
                update.setOdds( yesterdayAnimalOdds );
                lotteryGameMapper.updateById( update );
            }
            if ( yesterdayAnimalGameId != null && todayAnimalOdds != null ) {
                LotteryGame update = new LotteryGame();
                update.setId( yesterdayAnimalGameId );
                update.setOdds( todayAnimalOdds );
                lotteryGameMapper.updateById( update );
            }
        }

        String[] animals = LunarAnimalUtils.getLeftOverAnimals( todayAnimal );
        for ( int i = 0; i < animals.length; i++ ) {
            String animal = animals[ i ];
            int[] codeIds = switch ( i ) {
                case 0 -> new int[] { 1, 13, 25, 37, 49 };
                case 1 -> new int[] { 2, 14, 26, 38 };
                case 2 -> new int[] { 3, 15, 27, 39 };
                case 3 -> new int[] { 4, 16, 28, 40 };
                case 4 -> new int[] { 5, 17, 29, 41 };
                case 5 -> new int[] { 6, 18, 30, 42 };
                case 6 -> new int[] { 7, 19, 31, 43 };
                case 7 -> new int[] { 8, 20, 32, 44 };
                case 8 -> new int[] { 9, 21, 33, 45 };
                case 9 -> new int[] { 10, 22, 34, 46 };
                case 10 -> new int[] { 11, 23, 35, 47 };
                case 11 -> new int[] { 12, 24, 36, 48 };
                default -> throw new IllegalStateException( "Unexpected value: " + i );
            };
            StringBuilder codeJoin = new StringBuilder();
            for ( int codeId : codeIds ) {
                if ( !codeJoin.isEmpty() ) {
                    codeJoin.append( "," );
                }
                codeJoin.append( "'" ).append( codeId >= 10 ? codeId : "0" + codeId ).append( "'" );
            }
            lotteryGameMapper.updateKillrateLiuheOne( codeJoin.toString(), animal );
        }
        LotteryCacheUtils.me.clear();
    }
}
