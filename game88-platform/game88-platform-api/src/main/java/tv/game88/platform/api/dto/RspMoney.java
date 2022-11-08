package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspMoney {
    @Schema( title = "账户余额")
    private BigDecimal accountNow;
    @Schema( title = "保险箱余额")
    private BigDecimal boxAccount;
}
