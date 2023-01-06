package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqGameInfo {
    @Schema( title = "游戏类型ID" )
    @NotNull( message = "游戏类型ID不能为空" )
    private Long id;
    @Schema( title = "游戏平台ID" )
    private Long platformId;
}
