package tv.game88.lottery.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReqBet {
    @Schema( title = "彩票ID" )
    private Integer lotteryId;
    @Schema( title = "投注方式ID" )
    private Integer methodId;
    @Schema( title = "单注筹码" )
    private Integer chip;
    @Schema( title = "投注索引" )
    private String  betIds;
    @Schema( title = "主播ID(-1 直播间外 )" )
    private Integer anchor = -1;

}
