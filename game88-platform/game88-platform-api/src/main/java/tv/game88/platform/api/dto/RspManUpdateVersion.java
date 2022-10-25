package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspManUpdateVersion {
    @Schema( name = "下载地址" )
    private String downUrl       = "";
    @Schema( name = "人工更新版本" )
    private String manVersion    = "";
    @Schema( name = "更新内容" )
    private String updateContent = "";
}
