package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RspGamePlatform {
    private Long              id;
    @Schema( title = "平台名称" )
    private String            name;
    @Schema( title = "小图标" )
    private String            icon;
    @Schema( title = "卡片图标" )
    private String            cardIcon;
    @Schema( title = "游戏类型" )
    private String game_typeID;
    @Schema( title = "游戏信息列表" )
    private List<RspGameInfo> rspGameInfos = new ArrayList<>();
}
