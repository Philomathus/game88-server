package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspGameTypeWashCode {
    @Schema( title = "打码总额" )
    private BigDecimal codeAmountTotal;

    @Schema( title = "洗码比例" )
    private String washCodeRate;

    @Schema( title = "游戏类型" )
    private String gameTypeName;

    @Schema( title = "游戏类型ID" )
    private Long gameTypeId;

    @Schema( title = "洗码金额" )
    private BigDecimal washCodeAmount;
}
