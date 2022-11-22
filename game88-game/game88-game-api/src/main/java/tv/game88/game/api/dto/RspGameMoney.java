package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspGameMoney {
    @Schema( title = "余额" )
    private BigDecimal money;
    @Schema( title = "游戏平台id" )
    private Long       platformId;
    @Schema( title = "游戏平台名称" )
    private String     platformName;
}
