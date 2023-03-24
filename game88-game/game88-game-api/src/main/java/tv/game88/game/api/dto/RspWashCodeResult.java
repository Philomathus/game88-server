package tv.game88.game.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class RspWashCodeResult {
    private Map<Long, BigDecimal> gameTypeCodeMap = new HashMap<>();
    private BigDecimal            addWashAmount   = BigDecimal.ZERO;
}
