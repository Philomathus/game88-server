package tv.game88.lottery.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RspLotteryInit {
    @Schema( title = "基本信息" )
    private LotteryBase     base;
    @Schema( title = "期数信息" )
    private IssueVo         issuevo;
    @Schema( title = "投注方式" )
    private List<RspMethod> methods;
}
