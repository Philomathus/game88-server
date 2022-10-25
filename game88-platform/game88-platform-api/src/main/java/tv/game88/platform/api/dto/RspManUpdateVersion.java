package tv.game88.platform.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RspManUpdateVersion {
    @ApiModelProperty( value = "下载地址" )
    private String downUrl       = "";
    @ApiModelProperty( value = "人工更新版本" )
    private String manVersion    = "";
    @ApiModelProperty( value = "更新内容" )
    private String updateContent = "";
}
