package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RspGameTypes {
    @Schema( title = "游戏类型列表" )
    private List<RspGameType> rspGameTypes;
    @Schema( title = "类型排序第一的游戏信息列表" )
    private List<RspGameInfo> rspGameInfos;
}
