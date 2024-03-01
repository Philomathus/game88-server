package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspMemberWithdrawDetailInfo {
    @Schema( title = "可提现金额" )
    private BigDecimal canWithdrawMoney;
    @Schema( title = "余额" )
    private BigDecimal accountNow;
    @Schema( title = "还需打码" )
    private BigDecimal needBeat;
    @Schema( title = "USDT汇率" )
    private BigDecimal usdtWithdrawExchangeRate;
}
