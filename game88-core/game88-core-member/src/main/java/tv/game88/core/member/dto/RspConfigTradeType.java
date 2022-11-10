package tv.game88.core.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspConfigTradeType {
    @Schema( title = "交易名称" )
    private String  des;
    @Schema( title = "交易类型" )
    private String  name;
    @Schema( title = "交易类型id" )
    private Integer type;
}
