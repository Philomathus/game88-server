package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RspWashCodeInfo {
    @Schema( title = "可领取洗码金额" )
    private BigDecimal washCodeAmount;
    @Schema( title = "上次结算时间" )
    private String     washCodeTime;
    @Schema( title = "当前余额" )
    private BigDecimal money;

    @Schema( title = "游戏类型洗码列表" )
    private List<RspGameTypeWashCode> rspGameTypeWashCodes;

    public String getWashCodeTime() {
        if ( washCodeTime == null ) {
            return "";
        }
        return washCodeTime;
    }
}
