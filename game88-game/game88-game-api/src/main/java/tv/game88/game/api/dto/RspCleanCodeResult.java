package tv.game88.game.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspCleanCodeResult {
    private BigDecimal addCodeAmount  = BigDecimal.ZERO;
    private BigDecimal addCleanAmount = BigDecimal.ZERO;
}
