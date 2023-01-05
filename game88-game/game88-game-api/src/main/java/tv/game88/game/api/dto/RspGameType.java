package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspGameType {
    @Schema( title = "类型ID" )
    private Long    id;
    @Schema( title = "名称" )
    private String  name;
    @Schema( title = "图标" )
    private String  icon;
    @Schema( title = "显示类型" )
    private Integer type;
}
