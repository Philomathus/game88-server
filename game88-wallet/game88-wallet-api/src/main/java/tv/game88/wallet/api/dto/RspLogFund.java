package tv.game88.wallet.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema( title = "用户资金明细" )
public class RspLogFund {

    @Schema( title = "账变时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Schema( title = "账变类型" )
    private String        des;
    @Schema( title = "账变金额" )
    private BigDecimal    amount;

    @Hidden
    @JsonIgnore
    private BigDecimal pay;
    @Hidden
    @JsonIgnore
    private BigDecimal income;
    @Hidden
    @JsonIgnore
    private Integer    type;
}
