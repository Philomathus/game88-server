package tv.game88.core.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RspMemberRecommend {

    @Schema( title = "推广码" )
    private String        code;
    @Schema( title = "收益" )
    private BigDecimal    commission;
    @Schema( title = "日期" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Schema( title = "等级" )
    private Integer       level;
}
