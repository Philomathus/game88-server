package tv.game88.lottery.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode( callSuper = true )
@Data
public class RspMethod extends LocalMethod {
    @Schema( title = "投注内容列表" )
    private List<LotteryGameVo> games;
}
