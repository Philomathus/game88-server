package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RspWashCodeRate {
    @Schema( title = "游戏类型ID" )
    private Long   id;
    @Schema( title = "游戏类型名称" )
    private String name;

    @Schema( title = "游戏列表" )
    private List<RspWashCodeDesc> platforms = new ArrayList<>();
}
