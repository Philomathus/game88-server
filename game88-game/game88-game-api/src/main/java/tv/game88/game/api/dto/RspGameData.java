package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspGameData {
    @Schema( title = "单号" )
    private String     gameId;
    @Schema( title = "总投注" )
    private String     allBet;
    @Schema( title = "有效投注" )
    private String     cellScore;
    @Schema( title = "投注时间" )
    private String     createTime;
    @Schema( title = "盈利" )
    private BigDecimal profit;
    @Schema( title = "游戏代理号" )
    private String     agent;
    @Schema( title = "平台ID" )
    private Integer    platformId;
}
