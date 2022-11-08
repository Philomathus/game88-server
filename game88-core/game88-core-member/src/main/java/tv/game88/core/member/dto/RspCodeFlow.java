package tv.game88.core.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RspCodeFlow {
    @Schema( title = "充值时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Schema( title = "需求打码量" )
    private BigDecimal    income;
    @Schema( title = "充值类型" )
    private String        des;
    @Schema( title = "实际打码量" )
    private BigDecimal    cur;
    @Schema( title = "0=未打码 1=已打码" )
    private Integer       status;
    @Schema( title = "充值金额" )
    private BigDecimal    charge;
}
