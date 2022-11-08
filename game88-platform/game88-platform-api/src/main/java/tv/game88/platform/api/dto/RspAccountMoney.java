package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspAccountMoney {
    @Schema( title = "账户余额" )
    private BigDecimal balance;
}
