package tv.game88.lottery.api.base;

import org.springframework.util.CollectionUtils;
import tv.game88.lottery.api.dto.KillRandomVo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public abstract class AbstractExLottery implements ExLottery {
    public Map<String, Object> killResult( Map<String, BigDecimal> prizeMap, BigDecimal totalBet ) {
        Map<String, Object> resultMap = new HashMap<>();
        if ( !CollectionUtils.isEmpty( prizeMap ) && totalBet.compareTo( BigDecimal.ZERO ) > 0 ) {
            List<KillRandomVo> killRandomVoList = new ArrayList<>();
            // 随机10组
            for ( int i = 0; i < 10; i++ ) {
                KillRandomVo killRandomVo      = new KillRandomVo();
                List<String> randomResult      = randomResult();
                BigDecimal   randomResultPrize = coutPrize( randomResult, prizeMap );
                killRandomVo.setRandomResult( randomResult );
                killRandomVo.setRandomResultPrize( randomResultPrize );
                killRandomVoList.add( killRandomVo );
            }

            // 取派奖最小那组
            Optional<KillRandomVo> randomVoOptional = killRandomVoList
                    .stream()
                    .min( Comparator.comparing( KillRandomVo::getRandomResultPrize ) );
            if ( randomVoOptional.isPresent() ) {
                KillRandomVo killRandomVo = randomVoOptional.get();
                resultMap.put( "resultsList", killRandomVo.getRandomResult() );
                resultMap.put( "killRate", totalBet
                        .subtract( killRandomVo.getRandomResultPrize() )
                        .divide( totalBet, 2, RoundingMode.HALF_UP ) );
                resultMap.put( "totalPrize", killRandomVo.getRandomResultPrize() );
                return resultMap;
            }
        }
        resultMap.put( "resultsList", randomResult() );
        resultMap.put( "killRate", BigDecimal.ZERO );
        resultMap.put( "totalPrize", BigDecimal.ZERO );
        return resultMap;
    }
}
