package tv.game88.game.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class XiaFenResult {
    private boolean    ok        = false;
    private BigDecimal backMoney = BigDecimal.ZERO;
}
