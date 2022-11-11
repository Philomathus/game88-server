package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspGameCategory {
    @Schema( title = "交易名称" )
    private String  des;
    @Schema( title = "交易类型" )
    private String  name;
}
