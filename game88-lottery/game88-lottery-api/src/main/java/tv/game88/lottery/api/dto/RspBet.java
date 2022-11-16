package tv.game88.lottery.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RspBet {
    private BigDecimal     money;
    private Integer        lotteryId;
    private String         betId;
    private Integer        chip;
    private String         issue;
    private List<BetCount> totalData;
}
