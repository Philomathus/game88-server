package tv.game88.lottery.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LocalMethod {
    @Schema( title = "方法ID" )
    private Integer id;
    @Schema( title = "方法名称" )
    private String  name;
}
