package tv.game88.platform.api.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RspInit {
    @ApiModelProperty( value = "最新版本号" )
    private String  latestVersion = "";
    @ApiModelProperty( value = "1=强更0=不强更" )
    private String  latestFore    = "";
    @ApiModelProperty( value = "下载地址" )
    private String  downUrl       = "";
    @ApiModelProperty( value = "是否有新版本" )
    private boolean hasNew        = false;
    @ApiModelProperty( value = "更新内容" )
    private String  updateText;
    @ApiModelProperty( value = "客服地址" )
    private String  customerUrl;
    @ApiModelProperty( value = "客服地址2" )
    private String  customerUrl2;
    @ApiModelProperty( value = "官方网址" )
    private String  webUrl;
    @ApiModelProperty( value = "验证码id" )
    private String  captchaId;
    @ApiModelProperty( value = "产品编号" )
    private String  productId;
    @ApiModelProperty( value = "启动图地址(为空则不显示启动图)" )
    private String  starPic;

}
