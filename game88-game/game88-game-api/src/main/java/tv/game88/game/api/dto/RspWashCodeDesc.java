package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspWashCodeDesc {
    @Schema( title = "打码区间" )
    private String codeInterval;
    @Schema( title = "洗码比例" )
    private String washRate;
    @Schema( title = "打码比例" )
    private String beat;
}
