package tv.game88.core.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema( title = "账户明细" )
public class RspLogMoney {

    @Schema( title = "时间" )
    private String     createTime;
    @Schema( title = "状态" )
    private String     des;
    @Schema( title = "支出" )
    private BigDecimal pay;
    @Schema( title = "收入" )
    private BigDecimal income;
    @Schema( title = "余额" )
    private BigDecimal total;
    @Schema( title = "余额" )
    private BigDecimal totalBefore;

}
