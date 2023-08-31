package tv.game88.general.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspAgent {
    @Schema( title = "直播名称" )
    private String name;
    @Schema( title = "接口地址" )
    private String apiUrl;
}
