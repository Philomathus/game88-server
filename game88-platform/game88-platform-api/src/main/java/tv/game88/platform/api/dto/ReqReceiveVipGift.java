package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReqReceiveVipGift {
    @Schema( title = "类型(1晋级彩金2周俸禄3月俸禄)" )
    private Integer type;

}
