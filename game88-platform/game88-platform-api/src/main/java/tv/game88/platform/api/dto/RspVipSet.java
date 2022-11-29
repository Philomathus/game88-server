package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspVipSet {
    @Schema( title = "vip等级" )
    private Integer    level;
    @Schema( title = "晋级彩金" )
    private BigDecimal levelBonus;
    @Schema( title = "周俸禄)" )
    private BigDecimal weekBonus;
    /*@Schema( title = "月俸禄" )
    private BigDecimal monthBonus;*/
    @Schema( title = "打码量" )
    private BigDecimal bcode;
}
