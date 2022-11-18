package tv.game88.lottery.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class KillRandomVo {
    private List<String> randomResult;
    private BigDecimal   randomResultPrize;
}
