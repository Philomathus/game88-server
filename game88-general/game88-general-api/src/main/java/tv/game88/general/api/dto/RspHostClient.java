package tv.game88.general.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspHostClient {
    @Schema( title = "版本号" )
    private Integer version    = 1;
    @Schema( title = "版本号名称" )
    private String  versionName;
    @Schema( title = "下载地址" )
    private String  url;
    @Schema( title = "更新内容" )
    private String  updateText;
    @Schema( title = "1=强更0=不强更" )
    private Integer latestFore = 0;
}
