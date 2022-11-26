package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspMessageCommonProblem {
    @Schema( title = "ID" )
    private Long   id;
    @Schema( title = "标题" )
    private String title;
    @Schema( title = "内容" )
    private String content;
}
