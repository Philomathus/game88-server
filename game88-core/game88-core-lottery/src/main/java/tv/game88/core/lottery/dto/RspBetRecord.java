package tv.game88.core.lottery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode( callSuper = true )
@Data
public class RspBetRecord extends RspLotteryHistory {
    @Schema( title = "ID" )
    private String     id;
    @Schema( title = "0= 待开奖 1= 已中奖 2=未中奖 3=退回" )
    private Integer    status;
    @Schema( title = "下注选择" )
    private String     betSelect;
    @Schema( title = "中奖金额" )
    private BigDecimal prize;
    @Schema( title = "投资" )
    private BigDecimal cost;
    @Schema( title = "下注时间" )
    private String     betTime;

    @Schema( title = "胜负结果" )
    private String winOrLoseResult = "";
    @Schema( title = "赢方" )
    private String playOrBank      = "";
}
