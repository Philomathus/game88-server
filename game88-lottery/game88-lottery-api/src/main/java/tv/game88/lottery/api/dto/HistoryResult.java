package tv.game88.lottery.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HistoryResult {
    @Schema( title = "开奖ID" )
    private String id;
    @Schema( title = "开奖号码" )
    private String code;
    @Schema( title = "开奖分析" )
    private String analyse;

    @Schema( title = "中奖区域ID" )
    private String analyseGameId;
}
