package tv.game88.lottery.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RuleVo {
    @Schema( title = "标题" )
    private String name;
    @Schema( title = "内容" )
    private String des;
}
