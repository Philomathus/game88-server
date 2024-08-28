package tv.game88.platform.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RspInit {
    @Schema( title = "最新版本号" )
    private String      latestVersion = "";
    @Schema( title = "是否强更", description = "1=强更0=不强更" )
    private String      latestFore    = "";
    @Schema( title = "下载地址" )
    private String      downUrl       = "";
    @Schema( title = "是否有新版本" )
    private boolean     hasNew        = false;
    @Schema( title = "更新内容" )
    private String      updateText;
    @Schema( title = "客服地址" )
    private String      customerUrl;
    @Schema( title = "客服地址2" )
    private String      customerUrl2;
    @Schema( title = "官方网址" )
    private String      webUrl;
    @Schema( title = "启动图地址", description = "为空则不显示启动图" )
    private String      starPic;
    @Schema( title = "6合彩色" )
    private HeCai6Color heCai6        = new HeCai6Color();

    @Schema( title = "验证码id" )
    private String captchaId;
    @Schema( title = "滑动验证开关  1=开启0=关闭" )
    private String actionSwitch = "1";
    @Schema( title = "产品编号" )
    private String productId;
    @Schema( title = "首存活动URL" )
    private String firstRechargeUrl;

    @Schema( title = "appLink" )
    private String appLink;

    @Schema( title = "appLinkTechSpark" )
    private String appLinkTechSpark;

}
