package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.page.PageDomain;
import tv.game88.core.member.enums.EnumReqTime;
import tv.game88.game.api.type.EnumGameCategory;

@Data
public class ReqGameData extends PageDomain {
    @Schema( title = "游戏平台类型" )
    private EnumGameCategory gameCategory;
    @Schema( title = "时间设置" )
    private EnumReqTime      enumReqTime;
}
