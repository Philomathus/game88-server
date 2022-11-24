package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RspGameCategory {
    @Schema( title = "交易名称" )
    private String                 des;
    @Schema( title = "交易类型" )
    private String                 name;
    @Schema( title = "游戏列表" )
    private List<RspCleanPlatform> platforms = new ArrayList<>();
}
