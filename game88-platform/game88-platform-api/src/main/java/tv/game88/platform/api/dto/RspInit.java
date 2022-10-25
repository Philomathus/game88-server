package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspInit {
    @Schema( name = "最新版本号" )
    private String  latestVersion = "";
    @Schema( name = "1=强更0=不强更" )
    private String  latestFore    = "";
    @Schema( name = "下载地址" )
    private String  downUrl       = "";
    @Schema( name = "是否有新版本" )
    private boolean hasNew        = false;
    @Schema( name = "更新内容" )
    private String  updateText;
    @Schema( name = "客服地址" )
    private String  customerUrl;
    @Schema( name = "客服地址2" )
    private String  customerUrl2;
    @Schema( name = "官方网址" )
    private String  webUrl;
    @Schema( name = "验证码id" )
    private String  captchaId;
    @Schema( name = "产品编号" )
    private String  productId;
    @Schema( name = "启动图地址(为空则不显示启动图)" )
    private String  starPic;

}
