package tv.game88.pay.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReqVipPayDeposit {
    @Schema( title = "充值金额" )
    private BigDecimal amount;
}
