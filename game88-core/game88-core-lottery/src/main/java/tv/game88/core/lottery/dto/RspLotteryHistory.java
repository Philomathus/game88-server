package tv.game88.core.lottery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspLotteryHistory {
    @Schema( title = "期数" )
    private String issue;
    @Schema( title = "开奖号码" )
    private String code;
    @Schema( title = "开奖分析" )
    private String analyse = "";
}
