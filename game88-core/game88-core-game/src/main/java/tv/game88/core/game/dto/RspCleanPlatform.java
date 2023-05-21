package tv.game88.core.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspCleanPlatform {
    @Schema( title = "游戏平台ID" )
    private Long       id;
    @Schema( title = "游戏平台名称" )
    private String     name;
    @Schema( title = "洗码量" )
    private BigDecimal codeAmount  = BigDecimal.ZERO;
    @Schema( title = "洗码比例" )
    private BigDecimal rateClean   = BigDecimal.ZERO;
    @Schema( title = "洗码金额" )
    private BigDecimal cleanAmount = BigDecimal.ZERO;
}
