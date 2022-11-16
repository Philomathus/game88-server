package tv.game88.lottery.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LotteryGameVo {
    @Schema( title = "投注ID" )
    private String  id;
    @Schema( title = "投注信息" )
    private String  info;
    @Schema( title = "投注方式ID" )
    private Integer methodId;
    @Schema( title = "赔率" )
    private String  odds;
}
