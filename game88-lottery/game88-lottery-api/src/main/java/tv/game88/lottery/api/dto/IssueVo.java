package tv.game88.lottery.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class IssueVo {
    @Schema( title = "本期期号")
    private String issue;
    @Schema( title = "上期开奖")
    private String codeJust;
    @Schema( title = "结束倒计时")
    private Long countdown;
}
