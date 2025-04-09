package tv.game88.platform.api.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@FieldDefaults( level = AccessLevel.PRIVATE )
public class RechargeStatsDto {
    Long       dailyRechargeCount;
    BigDecimal totalRecharge;
}
