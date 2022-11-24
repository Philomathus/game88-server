package tv.game88.game.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RspCleanCodeLog {
    @Schema( title = "会员id" )
    private String        memberId;
    @Schema( title = "系统编号" )
    private String        id;
    @Schema( title = "洗码量" )
    private BigDecimal    codeAmount;
    @Schema( title = "洗码金额" )
    private BigDecimal    cleanAmount;
    @Schema( title = "洗码时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime cleanTime;
}
