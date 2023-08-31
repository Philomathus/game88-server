package tv.game88.general.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReqAgent {
    @Schema( title = "邀请码" )
    private String agent;
}
