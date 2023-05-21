package tv.game88.game.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.BooleanUtils;
import tv.game88.core.game.type.EnumGameCategory;

@Data
public class RspGameInfo {
    @Schema( title = "系统编号" )
    private Long    id;
    @Schema( title = "游戏名称" )
    private String  name;
    @Schema( title = "图标" )
    private String  icon;
    @Schema( title = "是否维护" )
    private Boolean maintain;
    @Schema( title = "是否推荐" )
    private Boolean recommend;
    @Schema( title = "是否大图标" )
    private Boolean largeIcon;

    @Schema( title = "游戏类别" )
    private EnumGameCategory gameCategory;

    @Schema( title = "彩票ID gameCategory为LOTTERY时有值", description = "1001:一分11选5 1002:一分快三 1003:一分赛车 1004:一分六合彩 1005:一分时时彩 "
            + "2001:百家乐" )
    private Long lotteryId;

    @Schema( hidden = true )
    private String kindId;

    @Schema( hidden = true )
    private Integer platformId;

    @Schema( title = "平台维护", hidden = true )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private Boolean platformMaintain;

    public Boolean getMaintain() {
        return BooleanUtils.isTrue( maintain ) || BooleanUtils.isTrue( platformMaintain );
    }
}
