package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspFundEnumType {
    @Schema( title = "资金类型名称" )
    private String  des;
    @Schema( title = "资金类型" )
    private String  name;
    @Schema( title = "资金类型id" )
    private Integer type;
}
