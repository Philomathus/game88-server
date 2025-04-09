package tv.game88.platform.api.constant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

public class RecordConstants {
    @Builder
    public record RspMemberStats( @Schema( title = "Total Registrations" ) Long totalRegistration,
                                  @Schema( title = "Daily Recharge Count" ) Long dailyRechargeCount,
                                  @Schema( title = "Daily First Recharge Count" ) Long dailyFirstRechargeCount,
                                  @Schema( title = "Total Withdrawals" ) Long totalWithdrawCount,
                                  @Schema( title = "Total Recharge Amount" ) BigDecimal totalRechargeAmount ) {
    }
}
