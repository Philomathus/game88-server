package tv.game88.lottery.api.base;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ExLottery {
    BigDecimal handle( Integer methodId, String[] officialSpl, BigDecimal chip, String betSelect );

    String concatBetString( Map<String, BigDecimal> betMap );

    BigDecimal coutPrize( List<String> list, Map<String, BigDecimal> peiMap );

    List<String> randomResult();

    Map<String, Object> killResult( Map<String, BigDecimal> prizeMap, BigDecimal totalBet );
}
