package tv.game88.core.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspCodeFlow {
    @Schema( name = "充值时间" )
    private String     createTime;
    @Schema( name = "需求打码量" )
    private BigDecimal income;
    @Schema( name = "充值类型" )
    private String     des;
    @Schema( name = "实际打码量" )
    private BigDecimal cur;
    @Schema( name = "0=未打码 1=已打码" )
    private Integer    status;
    @Schema( name = "充值金额" )
    private BigDecimal charge;
}
