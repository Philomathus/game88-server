package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspAmount {
    @Schema( title = "余额" )
    private BigDecimal amount;
}
