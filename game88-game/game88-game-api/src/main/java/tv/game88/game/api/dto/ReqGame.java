package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReqGame {
    @Schema( title = "游戏(或者游戏类型)ID" )
    private Long id;
}
