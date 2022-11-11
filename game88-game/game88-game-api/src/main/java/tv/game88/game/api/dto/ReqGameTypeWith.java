package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReqGameTypeWith {
    @Schema( title = "游戏信息ID" )
    private Long gameInfoId;
    @Schema( title = "排序号" )
    private Long sort;
}