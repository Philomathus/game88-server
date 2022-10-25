package tv.game88.core.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema( name = "账户明细" )
public class RspLogMoney {

    @Schema( name = "时间" )
    private String     createTime;
    @Schema( name = "状态" )
    private String     des;
    @Schema( name = "支出" )
    private BigDecimal pay;
    @Schema( name = "收入" )
    private BigDecimal income;
    @Schema( name = "余额" )
    private BigDecimal total;
    @Schema( name = "余额" )
    private BigDecimal totalBefore;

}
