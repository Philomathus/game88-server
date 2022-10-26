package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspManUpdateVersion {
    @Schema( title = "下载地址" )
    private String downUrl       = "";
    @Schema( title = "人工更新版本" )
    private String manVersion    = "";
    @Schema( title = "更新内容" )
    private String updateContent = "";
}
