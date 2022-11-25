package tv.game88.game.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.page.PageDomain;
import tv.game88.core.member.enums.EnumReqTime;

@Data
public class ReqGameData extends PageDomain {
    @Schema( title = "平台ID" )
    private Integer     platformId;
    @Schema( title = "时间设置" )
    private EnumReqTime enumReqTime;
}
