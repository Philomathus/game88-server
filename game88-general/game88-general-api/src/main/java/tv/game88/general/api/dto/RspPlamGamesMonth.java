package tv.game88.general.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspPlamGamesMonth {

    private String gameplame;

    private BigDecimal gamecell;//投注
    private BigDecimal paicai;//派彩
    private BigDecimal gameprofit;//盈利

    private String date;
    private BigDecimal countBetMoney;//总投注
    private BigDecimal countPaiCai;//总派彩
    private BigDecimal countGameProfit;//总盈利



}
